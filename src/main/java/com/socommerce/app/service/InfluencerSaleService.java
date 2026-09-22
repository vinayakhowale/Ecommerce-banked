package com.socommerce.app.service;

import com.socommerce.app.dto.InfluencerSaleDto;
import com.socommerce.app.entity.Discount;
import com.socommerce.app.entity.Influencer;
import com.socommerce.app.entity.InfluencerSale;
import com.socommerce.app.entity.Product;
import com.socommerce.app.exception.ResourceNotFoundException;
import com.socommerce.app.repository.DiscountRepository;
import com.socommerce.app.repository.InfluencerRepository;
import com.socommerce.app.repository.InfluencerSaleRepository;
import com.socommerce.app.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InfluencerSaleService {

    private final InfluencerSaleRepository influencerSaleRepository;
    private final InfluencerRepository influencerRepository;
    private final ProductRepository productRepository;
    private final DiscountRepository discountRepository;

    @Transactional(readOnly = true)
    public Page<InfluencerSale> list(Pageable pageable) {
        return influencerSaleRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<InfluencerSale> listForInfluencer(Long influencerId, Pageable pageable) {
        return influencerSaleRepository.findByInfluencerIdOrderByPurchaseDateDesc(influencerId, pageable);
    }

    /**
     * Records a confirmed sale attributed to an influencer's code. This is a manual admin action
     * (see class doc on InfluencerSale) rather than something triggered automatically by Buy Now,
     * since actual checkout happens on the external seller's site.
     */
    @Transactional
    public InfluencerSale record(InfluencerSaleDto.Request req) {
        Influencer influencer = influencerRepository.findById(req.influencerId())
                .orElseThrow(() -> new ResourceNotFoundException("Influencer not found"));
        Product product = productRepository.findById(req.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Discount discount = null;
        if (req.discountId() != null) {
            discount = discountRepository.findById(req.discountId())
                    .orElseThrow(() -> new ResourceNotFoundException("Discount not found"));
        }

        InfluencerSale sale = InfluencerSale.builder()
                .influencer(influencer)
                .influencerCodeUsed(influencer.getCode())
                .product(product)
                .orderId(req.orderId())
                .purchaseDate(req.purchaseDate())
                .quantity(req.quantity())
                .orderValue(req.orderValue())
                .discountApplied(req.discountApplied())
                .discount(discount)
                .build();
        return influencerSaleRepository.save(sale);
    }

    @Transactional
    public void delete(Long id) {
        InfluencerSale sale = influencerSaleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale record not found"));
        influencerSaleRepository.delete(sale);
    }
}
