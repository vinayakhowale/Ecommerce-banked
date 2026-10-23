package com.socommerce.app.dto;

import com.socommerce.app.entity.FulfillmentStatus;
import com.socommerce.app.entity.PaymentOrder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderTrackingDto {

    /** Public lookup: the order number plus the email used to buy, so strangers can't browse orders. */
    public record TrackRequest(
            @NotBlank(message = "Enter your order ID") String orderNumber,
            @NotBlank(message = "Enter the email you ordered with") String email
    ) {}

    /** Admin update. Any field left null keeps its current value. */
    public record FulfillmentUpdate(
            @NotNull(message = "Choose a status") FulfillmentStatus status,
            @Size(max = 120) String courierName,
            @Size(max = 64) String trackingNumber,
            @Size(max = 1024) String trackingUrl,
            LocalDate expectedDeliveryDate,
            @Size(max = 512) String note
    ) {}

    /** One step on the timeline shown to the customer. */
    public record Step(String key, String label, boolean done, boolean current, LocalDateTime at) {}

    public record Response(
            Long id, String orderNumber, String productName, Long productId, String productImageUrl,
            int quantity, BigDecimal totalAmount, String currency,
            String customerName, String customerEmail,
            FulfillmentStatus status, String statusLabel,
            String courierName, String trackingNumber, String trackingUrl,
            LocalDate expectedDeliveryDate, String note,
            LocalDateTime placedAt, LocalDateTime deliveredAt,
            List<Step> steps
    ) {
        private static final FulfillmentStatus[] FLOW = {
                FulfillmentStatus.CONFIRMED, FulfillmentStatus.PACKED, FulfillmentStatus.SHIPPED,
                FulfillmentStatus.OUT_FOR_DELIVERY, FulfillmentStatus.DELIVERED
        };

        public static Response from(PaymentOrder o) {
            FulfillmentStatus status = o.getEffectiveFulfillmentStatus();
            List<Step> steps = new ArrayList<>();
            boolean ended = status == FulfillmentStatus.CANCELLED || status == FulfillmentStatus.RETURNED;
            int reached = indexOf(status);
            for (int i = 0; i < FLOW.length; i++) {
                FulfillmentStatus s = FLOW[i];
                boolean done = !ended && i <= reached;
                steps.add(new Step(s.name(), s.label(), done, !ended && i == reached, timeOf(o, s)));
            }
            if (ended) {
                steps.add(new Step(status.name(), status.label(), true, true, o.getUpdatedAt()));
            }
            String image = o.getProduct() != null ? o.getProduct().getProductImageUrl() : null;
            if (image == null && o.getProduct() != null && o.getProduct().getImageUrls() != null
                    && !o.getProduct().getImageUrls().isEmpty()) {
                image = o.getProduct().getImageUrls().get(0);
            }
            return new Response(
                    o.getId(), o.getOrderNumber(),
                    o.getProductNameSnapshot() != null ? o.getProductNameSnapshot()
                            : (o.getProduct() != null ? o.getProduct().getName() : "Product"),
                    o.getProduct() != null ? o.getProduct().getId() : null, image,
                    o.getQuantity(), o.getTotalAmount(), o.getCurrency(),
                    o.getUser() != null ? o.getUser().getName() : null,
                    o.getUser() != null ? o.getUser().getEmail() : null,
                    status, status.label(),
                    o.getCourierName(), o.getTrackingNumber(), o.getTrackingUrl(),
                    o.getExpectedDeliveryDate(), o.getFulfillmentNote(),
                    o.getCreatedAt(), o.getDeliveredAt(), steps);
        }

        private static int indexOf(FulfillmentStatus s) {
            for (int i = 0; i < FLOW.length; i++) if (FLOW[i] == s) return i;
            return 0;
        }

        private static LocalDateTime timeOf(PaymentOrder o, FulfillmentStatus s) {
            return switch (s) {
                case CONFIRMED -> o.getCreatedAt();
                case PACKED -> o.getPackedAt();
                case SHIPPED -> o.getShippedAt();
                case OUT_FOR_DELIVERY -> o.getOutForDeliveryAt();
                case DELIVERED -> o.getDeliveredAt();
                default -> null;
            };
        }
    }
}
