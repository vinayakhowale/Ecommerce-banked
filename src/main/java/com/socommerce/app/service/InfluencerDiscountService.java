package com.socommerce.app.service;

import com.socommerce.app.dto.InfluencerDiscountDto;
import com.socommerce.app.entity.Influencer;
import com.socommerce.app.entity.InfluencerDiscount;
import com.socommerce.app.exception.BadRequestException;
import com.socommerce.app.exception.ResourceNotFoundException;
import com.socommerce.app.repository.InfluencerDiscountRepository;
import com.socommerce.app.repository.InfluencerRepository;
import com.socommerce.app.repository.InfluencerSaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InfluencerDiscountService {

    private final InfluencerDiscountRepository influencerDiscountRepository;
    private final InfluencerRepository influencerRepository;
    private final InfluencerSaleRepository influencerSaleRepository;

    @Transactional(readOnly = true)
    public List<InfluencerDiscount> list() {
        return influencerDiscountRepository.findAll();
    }

    @Transactional(readOnly = true)
    public long monthlySalesCountFor(Long influencerId) {
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        return influencerSaleRepository.sumQuantityByInfluencerSince(influencerId, since);
    }

    @Transactional
    public InfluencerDiscount upsert(InfluencerDiscountDto.Request req) {
        Influencer influencer = influencerRepository.findById(req.influencerId())
                .orElseThrow(() -> new ResourceNotFoundException("Influencer not found"));

        InfluencerDiscount discount = influencerDiscountRepository.findByInfluencerId(req.influencerId())
                .orElseGet(() -> InfluencerDiscount.builder().influencer(influencer).build());

        discount.setDiscountPercentage(req.discountPercentage());
        discount.setNotes(req.notes());
        if (req.active() != null) discount.setActive(req.active());
        else if (discount.getId() == null) discount.setActive(true);

        return influencerDiscountRepository.save(discount);
    }

    @Transactional
    public void delete(Long id) {
        InfluencerDiscount discount = influencerDiscountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Influencer discount not found"));
        influencerDiscountRepository.delete(discount);
    }
}
