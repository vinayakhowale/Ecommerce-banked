package com.socommerce.app.service;

import com.socommerce.app.dto.DiscountDto;
import com.socommerce.app.entity.Discount;
import com.socommerce.app.entity.DiscountType;
import com.socommerce.app.entity.Product;
import com.socommerce.app.exception.BadRequestException;
import com.socommerce.app.exception.ResourceNotFoundException;
import com.socommerce.app.repository.DiscountRepository;
import com.socommerce.app.repository.InfluencerSaleRepository;
import com.socommerce.app.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscountService {

    private final DiscountRepository discountRepository;
    private final ProductRepository productRepository;
    private final InfluencerSaleRepository influencerSaleRepository;

    /** The resolved outcome of applying whichever discount (if any) benefits the customer most. */
    public record EffectiveDiscount(BigDecimal finalPrice, BigDecimal discountAmount, String label, Long discountId) {
        public boolean hasDiscount() {
            return discountId != null && discountAmount.compareTo(BigDecimal.ZERO) > 0;
        }
    }

    /**
     * Resolves the single best-value discount currently applicable to a product (checking both
     * product-specific and category-wide campaigns), and returns the final price a customer would
     * see. If multiple discounts could apply simultaneously, the one yielding the larger absolute
     * saving wins -- this keeps pricing unambiguous rather than stacking promotions.
     */
    @Transactional(readOnly = true)
    public EffectiveDiscount computeEffectivePrice(Product product) {
        LocalDateTime now = LocalDateTime.now();
        List<Discount> candidates = new ArrayList<>(discountRepository.findActiveForProduct(product.getId(), now));
        if (product.getCategory() != null && !product.getCategory().isBlank()) {
            candidates.addAll(discountRepository.findActiveForCategory(product.getCategory(), now));
        }

        Discount best = null;
        BigDecimal bestAmount = BigDecimal.ZERO;
        for (Discount d : candidates) {
            BigDecimal amount = amountFor(d, product.getPrice());
            if (amount.compareTo(bestAmount) > 0) {
                bestAmount = amount;
                best = d;
            }
        }

        if (best == null) {
            return new EffectiveDiscount(product.getPrice(), BigDecimal.ZERO, null, null);
        }

        BigDecimal finalPrice = product.getPrice().subtract(bestAmount).setScale(2, RoundingMode.HALF_UP);
        String label = best.getType() == DiscountType.PERCENTAGE
                ? best.getName() + " (-" + best.getValue().stripTrailingZeros().toPlainString() + "%)"
                : best.getName() + " (-" + bestAmount.setScale(2, RoundingMode.HALF_UP) + ")";
        return new EffectiveDiscount(finalPrice, bestAmount.setScale(2, RoundingMode.HALF_UP), label, best.getId());
    }

    private BigDecimal amountFor(Discount d, BigDecimal price) {
        BigDecimal amount = d.getType() == DiscountType.PERCENTAGE
                ? price.multiply(d.getValue()).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
                : d.getValue();
        // Never discount past zero.
        return amount.compareTo(price) > 0 ? price : amount;
    }

    @Transactional(readOnly = true)
    public Page<Discount> list(Pageable pageable) {
        return discountRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Discount getAny(Long id) {
        return discountRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Discount not found"));
    }

    @Transactional
    public Discount create(DiscountDto.Request req) {
        validate(req);
        Discount discount = Discount.builder()
                .name(req.name())
                .type(req.type())
                .value(req.value())
                .scope(req.scope())
                .category(req.category())
                .startAt(req.startAt())
                .endAt(req.endAt())
                .conditions(req.conditions())
                .active(req.active() == null || req.active())
                .build();
        if (req.scope() == com.socommerce.app.entity.DiscountScope.PRODUCT) {
            if (req.productId() == null) throw new BadRequestException("productId is required when scope is PRODUCT");
            Product product = productRepository.findById(req.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
            discount.setProduct(product);
        }
        return discountRepository.save(discount);
    }

    @Transactional
    public Discount update(Long id, DiscountDto.Request req) {
        validate(req);
        Discount discount = getAny(id);
        discount.setName(req.name());
        discount.setType(req.type());
        discount.setValue(req.value());
        discount.setScope(req.scope());
        discount.setCategory(req.category());
        discount.setStartAt(req.startAt());
        discount.setEndAt(req.endAt());
        discount.setConditions(req.conditions());
        if (req.active() != null) discount.setActive(req.active());
        if (req.scope() == com.socommerce.app.entity.DiscountScope.PRODUCT) {
            if (req.productId() == null) throw new BadRequestException("productId is required when scope is PRODUCT");
            Product product = productRepository.findById(req.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
            discount.setProduct(product);
        } else {
            discount.setProduct(null);
        }
        return discountRepository.save(discount);
    }

    @Transactional
    public void setActive(Long id, boolean active) {
        Discount discount = getAny(id);
        discount.setActive(active);
        discountRepository.save(discount);
    }

    @Transactional
    public void delete(Long id) {
        Discount discount = getAny(id);
        // Force delete: unlink any confirmed-sale records that reference this discount for
        // campaign reporting, rather than blocking removal of the discount itself.
        influencerSaleRepository.detachFromDiscount(id);
        discountRepository.delete(discount);
    }

    private void validate(DiscountDto.Request req) {
        if (!req.endAt().isAfter(req.startAt())) {
            throw new BadRequestException("Discount end date/time must be after the start date/time");
        }
        if (req.scope() == com.socommerce.app.entity.DiscountScope.CATEGORY
                && (req.category() == null || req.category().isBlank())) {
            throw new BadRequestException("category is required when scope is CATEGORY");
        }
        if (req.type() == DiscountType.PERCENTAGE && req.value().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BadRequestException("Percentage discount cannot exceed 100");
        }
    }
}
