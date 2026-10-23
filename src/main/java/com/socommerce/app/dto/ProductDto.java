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
            java.util.List<String> imageUrls,
            String videoUrl,
            java.util.List<String> videoUrls,
            // Optional: checkout is in-app via Razorpay, so no external URL is required any more.
            String productUrl,
            String affiliateUrl,
            String razorpayAccountId,
            Boolean active
    ) {}

    public record Response(
            Long id, String name, String description, BigDecimal price, String currency,
            String brand, String category, String seller, String productImageUrl,
            java.util.List<String> imageUrls,
            String videoUrl, java.util.List<String> videoUrls,
            String productUrl, String affiliateUrl, String razorpayAccountId, boolean payInApp,
            boolean active, boolean urlApproved,
            BigDecimal discountedPrice, String discountLabel, boolean hasDiscount,
            java.time.LocalDateTime createdAt, java.time.LocalDateTime updatedAt
    ) {
        /** Admin response: includes the raw Razorpay account ID so the edit form can show/change it. No discount computed. */
        public static Response from(Product p) {
            return new Response(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getCurrency(),
                    p.getBrand(), p.getCategory(), p.getSeller(), p.getProductImageUrl(),
                    images(p), p.getVideoUrl(), videos(p),
                    p.getProductUrl(), p.getAffiliateUrl(), p.getRazorpayAccountId(), hasAccount(p),
                    p.isActive(), p.isUrlApproved(), p.getPrice(), null, false,
                    p.getCreatedAt(), p.getUpdatedAt());
        }

        /**
         * Public/customer-facing response: the best currently-applicable discount is resolved,
         * and only a boolean "payInApp" flag is exposed for Razorpay -- the raw linked account ID
         * is an internal detail and is never sent to the storefront.
         */
        public static Response from(Product p, DiscountService.EffectiveDiscount discount) {
            return new Response(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getCurrency(),
                    p.getBrand(), p.getCategory(), p.getSeller(), p.getProductImageUrl(),
                    images(p), p.getVideoUrl(), videos(p),
                    p.getProductUrl(), p.getAffiliateUrl(), null, hasAccount(p),
                    p.isActive(), p.isUrlApproved(),
                    discount.finalPrice(), discount.label(), discount.hasDiscount(),
                    p.getCreatedAt(), p.getUpdatedAt());
        }

        /** Never returns null, so the storefront can always iterate the gallery safely. */
        private static java.util.List<String> images(Product p) {
            if (p.getImageUrls() != null && !p.getImageUrls().isEmpty()) {
                return java.util.List.copyOf(p.getImageUrls());
            }
            // Fall back to the single legacy image so products created before galleries existed
            // still render a gallery of one instead of an empty carousel.
            return p.getProductImageUrl() == null ? java.util.List.of() : java.util.List.of(p.getProductImageUrl());
        }

        /** Never null, so the storefront can iterate the video gallery safely. */
        private static java.util.List<String> videos(Product p) {
            if (p.getVideoUrls() != null && !p.getVideoUrls().isEmpty()) {
                return java.util.List.copyOf(p.getVideoUrls());
            }
            return p.getVideoUrl() == null ? java.util.List.of() : java.util.List.of(p.getVideoUrl());
        }

        private static boolean hasAccount(Product p) {
            return p.getRazorpayAccountId() != null && !p.getRazorpayAccountId().isBlank();
        }
    }
}
