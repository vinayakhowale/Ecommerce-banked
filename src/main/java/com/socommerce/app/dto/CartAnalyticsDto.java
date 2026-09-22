package com.socommerce.app.dto;

import com.socommerce.app.entity.CartItem;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

public class CartAnalyticsDto {

    public record Row(
            Long cartItemId,
            Long userId,
            String userName,
            String userEmail,
            Long productId,
            String productName,
            String productImageUrl,
            int quantity,
            BigDecimal unitPrice,
            String currency,
            LocalDateTime addedAt,
            long minutesInCart,
            String durationLabel
    ) {
        public static Row from(CartItem ci) {
            LocalDateTime addedAt = ci.getCreatedAt();
            long minutes = Duration.between(addedAt, LocalDateTime.now()).toMinutes();
            return new Row(
                    ci.getId(),
                    ci.getUser().getId(), ci.getUser().getName(), ci.getUser().getEmail(),
                    ci.getProduct().getId(), ci.getProduct().getName(), ci.getProduct().getProductImageUrl(),
                    ci.getQuantity(), ci.getProduct().getPrice(), ci.getProduct().getCurrency(),
                    addedAt, minutes, formatDuration(minutes)
            );
        }

        private static String formatDuration(long minutes) {
            if (minutes < 1) return "Just now";
            if (minutes < 60) return minutes + "m";
            long hours = minutes / 60;
            if (hours < 24) return hours + "h " + (minutes % 60) + "m";
            long days = hours / 24;
            return days + "d " + (hours % 24) + "h";
        }
    }
}
