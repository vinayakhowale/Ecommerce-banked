package com.socommerce.app.dto;

import com.socommerce.app.entity.Order;
import com.socommerce.app.entity.OrderItem;
import com.socommerce.app.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDto {

    /** Checkout: the client sends only an address and an optional code -- never an amount. */
    public record CheckoutRequest(
            @NotNull Long addressId,
            String influencerCode
    ) {}

    /** Everything the Razorpay checkout popup needs, plus our own order reference. */
    public record CheckoutResponse(
            Long orderId,
            String orderNumber,
            String razorpayOrderId,
            String razorpayKeyId,
            long amountInPaise,
            String currency,
            String customerName,
            String customerEmail,
            String customerPhone
    ) {}

    public record VerifyRequest(
            @NotNull String razorpayOrderId,
            @NotNull String razorpayPaymentId,
            @NotNull String razorpaySignature
    ) {}

    public record ItemResponse(
            Long id, Long productId, String productName, String productImageUrl, String brand,
            int quantity, BigDecimal unitPrice, BigDecimal mrp, BigDecimal lineTotal
    ) {
        public static ItemResponse from(OrderItem i) {
            return new ItemResponse(
                    i.getId(),
                    i.getProduct() == null ? null : i.getProduct().getId(),
                    i.getProductName(), i.getProductImageUrl(), i.getBrand(),
                    i.getQuantity(), i.getUnitPrice(), i.getMrp(), i.getLineTotal());
        }
    }

    public record Response(
            Long id, String orderNumber, OrderStatus status, String statusLabel,
            List<ItemResponse> items,
            String shipName, String shipPhone, String shipLine1, String shipLine2,
            String shipLandmark, String shipCity, String shipState, String shipPincode,
            BigDecimal subtotal, BigDecimal shippingFee, BigDecimal total, String currency,
            String courierName, String trackingNumber, String trackingUrl, String statusNote,
            String customerName, String customerEmail,
            LocalDateTime placedAt, LocalDateTime paidAt, LocalDateTime shippedAt, LocalDateTime deliveredAt
    ) {
        public static Response from(Order o) {
            return new Response(
                    o.getId(), o.getOrderNumber(), o.getStatus(), label(o.getStatus()),
                    o.getItems().stream().map(ItemResponse::from).toList(),
                    o.getShipName(), o.getShipPhone(), o.getShipLine1(), o.getShipLine2(),
                    o.getShipLandmark(), o.getShipCity(), o.getShipState(), o.getShipPincode(),
                    o.getSubtotal(), o.getShippingFee(), o.getTotal(), o.getCurrency(),
                    o.getCourierName(), o.getTrackingNumber(), o.getTrackingUrl(), o.getStatusNote(),
                    o.getUser() == null ? null : o.getUser().getName(),
                    o.getUser() == null ? null : o.getUser().getEmail(),
                    o.getCreatedAt(), o.getPaidAt(), o.getShippedAt(), o.getDeliveredAt());
        }

        /** Wording the customer sees, so the UI never has to translate an enum. */
        private static String label(OrderStatus s) {
            return switch (s) {
                case PENDING_PAYMENT -> "Awaiting payment";
                case PLACED -> "Order placed";
                case CONFIRMED -> "Confirmed";
                case PACKED -> "Packed";
                case SHIPPED -> "Shipped";
                case OUT_FOR_DELIVERY -> "Out for delivery";
                case DELIVERED -> "Delivered";
                case CANCELLED -> "Cancelled";
                case RETURNED -> "Returned";
            };
        }
    }

    /** Admin: move an order along, optionally attaching courier details or a note. */
    public record UpdateStatusRequest(
            @NotNull OrderStatus status,
            String courierName,
            String trackingNumber,
            String trackingUrl,
            String statusNote
    ) {}
}
