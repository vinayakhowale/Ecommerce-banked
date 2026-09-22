package com.socommerce.app.dto;

import com.socommerce.app.entity.CartItem;
import com.socommerce.app.service.DiscountService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CartDto {

    public record AddRequest(@NotNull Long productId, @Min(1) Integer quantity) {}

    public record UpdateQuantityRequest(@Min(1) int quantity) {}

    public record ItemResponse(
            Long id, Long productId, String productName, String productImageUrl,
            BigDecimal price, String currency, int quantity, BigDecimal subtotal,
            boolean productActive, BigDecimal discountedPrice, String discountLabel, boolean hasDiscount,
            boolean payInApp
    ) {
        /**
         * Builds the cart line using whatever discount currently applies to this product, so the
         * price/subtotal shown here always matches what the product page shows -- computed fresh
         * server-side each time (never trusting a stored/cached price), same integrity guarantee
         * as the rest of the cart.
         */
        public static ItemResponse from(CartItem ci, DiscountService.EffectiveDiscount discount) {
            BigDecimal unitPrice = discount.hasDiscount() ? discount.finalPrice() : ci.getProduct().getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(ci.getQuantity())).setScale(2, RoundingMode.HALF_UP);
            String accountId = ci.getProduct().getRazorpayAccountId();
            boolean payInApp = accountId != null && !accountId.isBlank();
            return new ItemResponse(ci.getId(), ci.getProduct().getId(), ci.getProduct().getName(),
                    ci.getProduct().getProductImageUrl(), ci.getProduct().getPrice(), ci.getProduct().getCurrency(),
                    ci.getQuantity(), subtotal, ci.getProduct().isActive(),
                    discount.finalPrice(), discount.label(), discount.hasDiscount(), payInApp);
        }
    }

    public record CartResponse(java.util.List<ItemResponse> items, BigDecimal total, int itemCount) {}
}
