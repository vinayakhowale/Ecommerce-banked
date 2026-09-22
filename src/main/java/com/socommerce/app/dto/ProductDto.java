package com.socommerce.app.dto;

import com.socommerce.app.entity.Product;
import com.socommerce.app.service.DiscountService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public class ProductDto {

    public record Request(
            @NotBlank String name,
            String description,
            @NotNull @PositiveOrZero BigDecimal price,
            String currency,
            String brand,
            String category,
            String seller,
            String productImageUrl,
            @NotBlank String productUrl,
            String affiliateUrl,
            String razorpayAccountId,
            Boolean active
    ) {}

    public record Response(
            Long id, String name, String description, BigDecimal price, String currency,
            String brand, String category, String seller, String productImageUrl,
            String productUrl, String affiliateUrl, String razorpayAccountId, boolean payInApp,
            boolean active, boolean urlApproved,
            BigDecimal discountedPrice, String discountLabel, boolean hasDiscount,
            java.time.LocalDateTime createdAt
    ) {
        /** Admin response: includes the raw Razorpay account ID so the edit form can show/change it. No discount computed. */
        public static Response from(Product p) {
            return new Response(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getCurrency(),
                    p.getBrand(), p.getCategory(), p.getSeller(), p.getProductImageUrl(),
                    p.getProductUrl(), p.getAffiliateUrl(), p.getRazorpayAccountId(), hasAccount(p),
                    p.isActive(), p.isUrlApproved(), p.getPrice(), null, false, p.getCreatedAt());
        }

        /**
         * Public/customer-facing response: the best currently-applicable discount is resolved,
         * and only a boolean "payInApp" flag is exposed for Razorpay -- the raw linked account ID
         * is an internal detail and is never sent to the storefront.
         */
        public static Response from(Product p, DiscountService.EffectiveDiscount discount) {
            return new Response(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getCurrency(),
                    p.getBrand(), p.getCategory(), p.getSeller(), p.getProductImageUrl(),
                    p.getProductUrl(), p.getAffiliateUrl(), null, hasAccount(p),
                    p.isActive(), p.isUrlApproved(),
                    discount.finalPrice(), discount.label(), discount.hasDiscount(), p.getCreatedAt());
        }

        private static boolean hasAccount(Product p) {
            return p.getRazorpayAccountId() != null && !p.getRazorpayAccountId().isBlank();
        }
    }
}
