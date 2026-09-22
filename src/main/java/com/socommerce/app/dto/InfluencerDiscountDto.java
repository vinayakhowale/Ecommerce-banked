package com.socommerce.app.dto;

import com.socommerce.app.entity.InfluencerDiscount;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public class InfluencerDiscountDto {

    public record Request(
            @NotNull Long influencerId,
            @NotNull @PositiveOrZero BigDecimal discountPercentage,
            String notes,
            Boolean active
    ) {}

    public record Response(
            Long id, Long influencerId, String influencerName, String influencerCode,
            BigDecimal discountPercentage, String notes, boolean active,
            long monthlySalesCount, java.time.LocalDateTime createdAt
    ) {
        public static Response from(InfluencerDiscount d, long monthlySalesCount) {
            return new Response(
                    d.getId(), d.getInfluencer().getId(), d.getInfluencer().getName(), d.getInfluencer().getCode(),
                    d.getDiscountPercentage(), d.getNotes(), d.isActive(), monthlySalesCount, d.getCreatedAt()
            );
        }
    }
}
