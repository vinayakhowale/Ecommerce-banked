package com.socommerce.app.dto;

import com.socommerce.app.entity.Discount;
import com.socommerce.app.entity.DiscountScope;
import com.socommerce.app.entity.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DiscountDto {

    public record Request(
            @NotBlank String name,
            @NotNull DiscountType type,
            @NotNull @PositiveOrZero BigDecimal value,
            @NotNull DiscountScope scope,
            Long productId,
            String category,
            @NotNull LocalDateTime startAt,
            @NotNull LocalDateTime endAt,
            String conditions,
            Boolean active
    ) {}

    public record Response(
            Long id, String name, DiscountType type, BigDecimal value, DiscountScope scope,
            Long productId, String productName, String category,
            LocalDateTime startAt, LocalDateTime endAt, String conditions, boolean active,
            boolean currentlyLive, LocalDateTime createdAt
    ) {
        public static Response from(Discount d) {
            boolean live = d.isActive()
                    && !d.getStartAt().isAfter(LocalDateTime.now())
                    && !d.getEndAt().isBefore(LocalDateTime.now());
            return new Response(
                    d.getId(), d.getName(), d.getType(), d.getValue(), d.getScope(),
                    d.getProduct() != null ? d.getProduct().getId() : null,
                    d.getProduct() != null ? d.getProduct().getName() : null,
                    d.getCategory(), d.getStartAt(), d.getEndAt(), d.getConditions(), d.isActive(),
                    live, d.getCreatedAt()
            );
        }
    }
}
