package com.socommerce.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class PaymentDto {

    public record CreateOrderRequest(
            @NotNull Long productId,
            @Positive int quantity,
            String influencerCode
    ) {}

    /** Everything the frontend needs to open the Razorpay Checkout modal. */
    public record CreateOrderResponse(
            Long paymentOrderId,
            String razorpayOrderId,
            String razorpayKeyId,
            long amountInPaise,
            String currency,
            String productName,
            String productImageUrl
    ) {}

    public record VerifyRequest(
            @NotBlank String razorpayOrderId,
            @NotBlank String razorpayPaymentId,
            @NotBlank String razorpaySignature
    ) {}

    public record VerifyResponse(
            Long paymentOrderId,
            String status,
            String productName,
            BigDecimal totalAmount,
            String currency
    ) {}
}
