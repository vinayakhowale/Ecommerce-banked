package com.socommerce.app.dto;

import com.socommerce.app.entity.InfluencerSale;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InfluencerSaleDto {

    public record Request(
            @NotNull Long influencerId,
            @NotNull Long productId,
            @NotBlank String orderId,
            @NotNull LocalDateTime purchaseDate,
            @NotNull @Positive Integer quantity,
            @NotNull BigDecimal orderValue,
            String discountApplied,
            Long discountId
    ) {}

    public record Response(
            Long id, Long influencerId, String influencerName, String influencerCodeUsed,
            Long productId, String productName, String orderId, LocalDateTime purchaseDate,
            Integer quantity, BigDecimal orderValue, String discountApplied,
            Long discountId, String discountName, LocalDateTime createdAt
    ) {
        public static Response from(InfluencerSale s) {
            return new Response(
                    s.getId(), s.getInfluencer().getId(), s.getInfluencer().getName(), s.getInfluencerCodeUsed(),
                    s.getProduct().getId(), s.getProduct().getName(), s.getOrderId(), s.getPurchaseDate(),
                    s.getQuantity(), s.getOrderValue(), s.getDiscountApplied(),
                    s.getDiscount() != null ? s.getDiscount().getId() : null,
                    s.getDiscount() != null ? s.getDiscount().getName() : null,
                    s.getCreatedAt()
            );
        }
    }
}
