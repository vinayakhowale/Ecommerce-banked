package com.socommerce.app.service;

import com.socommerce.app.dto.ProductDto;
import com.socommerce.app.entity.Product;
import com.socommerce.app.exception.BadRequestException;
import com.socommerce.app.exception.ResourceNotFoundException;
import com.socommerce.app.repository.AddToCartEventRepository;
import com.socommerce.app.repository.CartItemRepository;
import com.socommerce.app.repository.DiscountRepository;
import com.socommerce.app.repository.InfluencerSaleRepository;
import com.socommerce.app.repository.PaymentOrderRepository;
import com.socommerce.app.repository.PostProductRepository;
import com.socommerce.app.repository.ProductClickRepository;
import com.socommerce.app.repository.ProductRepository;
import com.socommerce.app.util.RedirectUrlValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final PostProductRepository postProductRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductClickRepository productClickRepository;
    private final AddToCartEventRepository addToCartEventRepository;
    private final DiscountRepository discountRepository;
    private final InfluencerSaleRepository influencerSaleRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final RedirectUrlValidator redirectUrlValidator;

    @Value("${app.catalog.new-launch-window-days}")
    private int newLaunchWindowDays;

    @Transactional(readOnly = true)
    public Page<Product> listActive(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> listByCategory(String category, Pageable pageable) {
        return productRepository.findByActiveTrueAndCategoryIgnoreCase(category, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> search(String q, Pageable pageable) {
        return productRepository.search(q, pageable);
    }

    /**
     * Every currently-active product added within the configured window (default 21 days),
     * newest first -- surfaced automatically the instant an admin publishes a new product, no
     * manual "feature this" step required. See app.catalog.new-launch-window-days.
     */
    @Transactional(readOnly = true)
    public Page<Product> listNewLaunches(Pageable pageable) {
        LocalDateTime since = LocalDateTime.now().minusDays(newLaunchWindowDays);
        return productRepository.findByActiveTrueAndCreatedAtAfterOrderByCreatedAtDesc(since, pageable);
    }

    @Transactional(readOnly = true)
    public Product getActiveOrThrow(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (!p.isActive()) {
            throw new BadRequestException("This product is no longer available");
        }
        return p;
    }

    @Transactional(readOnly = true)
    public Product getAny(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    @Transactional(readOnly = true)
    public Page<Product> adminList(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Transactional
    public Product create(ProductDto.Request req) {
        validateUrls(req.productUrl(), req.affiliateUrl());
        Product product = Product.builder()
                .name(req.name())
                .description(req.description())
                .price(req.price())
                .currency(req.currency() == null || req.currency().isBlank() ? "INR" : req.currency())
                .brand(req.brand())
                .category(req.category())
                .seller(req.seller())
                .productImageUrl(req.productImageUrl())
                .productUrl(req.productUrl())
                .affiliateUrl(req.affiliateUrl())
                .razorpayAccountId(blankToNull(req.razorpayAccountId()))
                .active(req.active() == null || req.active())
                .urlApproved(true)
                .build();
        return productRepository.save(product);
    }

    @Transactional
    public Product update(Long id, ProductDto.Request req) {
        Product product = getAny(id);
        validateUrls(req.productUrl(), req.affiliateUrl());
        product.setName(req.name());
        product.setDescription(req.description());
        product.setPrice(req.price());
        if (req.currency() != null && !req.currency().isBlank()) product.setCurrency(req.currency());
        product.setBrand(req.brand());
        product.setCategory(req.category());
        product.setSeller(req.seller());
        product.setProductImageUrl(req.productImageUrl());
        product.setProductUrl(req.productUrl());
        product.setAffiliateUrl(req.affiliateUrl());
        product.setRazorpayAccountId(blankToNull(req.razorpayAccountId()));
        if (req.active() != null) product.setActive(req.active());
        product.setUrlApproved(true);
        return productRepository.save(product);
    }

    @Transactional
    public void deactivate(Long id) {
        Product product = getAny(id);
        product.setActive(false);
        productRepository.save(product);
    }

    @Transactional
    public void activate(Long id) {
        Product product = getAny(id);
        product.setActive(true);
        productRepository.save(product);
    }

    @Transactional
    public void delete(Long id) {
        Product product = getAny(id);

        // Admin delete is unconditional: the product is removed no matter what else references it.
        // Any tags on posts, cart copies, running discounts, and confirmed-sale history are cleaned
        // up automatically rather than blocking the delete -- so a post keeps its other tagged
        // products (just loses this one), the item quietly disappears from carts instead of
        // pointing at nothing, and any discount scoped to this product goes with it.
        postProductRepository.deleteByProductId(id);
        cartItemRepository.deleteByProductId(id);
        productClickRepository.deleteByProductId(id);
        addToCartEventRepository.deleteByProductId(id);
        discountRepository.deleteByProductId(id);
        influencerSaleRepository.deleteByProductId(id);
        paymentOrderRepository.detachFromProduct(id);

        productRepository.delete(product);
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private void validateUrls(String productUrl, String affiliateUrl) {
        if (!redirectUrlValidator.isSafe(productUrl)) {
            throw new BadRequestException("Product URL is invalid or not allowed");
        }
        if (affiliateUrl != null && !affiliateUrl.isBlank() && !redirectUrlValidator.isSafe(affiliateUrl)) {
            throw new BadRequestException("Affiliate URL is invalid or not allowed");
        }
    }
}
