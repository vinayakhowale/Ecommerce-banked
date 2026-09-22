package com.socommerce.app.controller;

import com.socommerce.app.dto.PaymentDto;
import com.socommerce.app.security.UserPrincipal;
import com.socommerce.app.service.PaymentOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Real in-app payment via Razorpay. Requires authentication (unlike Buy Now, which also works
 * for guests) since a payment must be tied to a real account for order history/support.
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentOrderService paymentOrderService;

    @PostMapping("/create-order")
    public ResponseEntity<PaymentDto.CreateOrderResponse> createOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody PaymentDto.CreateOrderRequest request) {
        return ResponseEntity.ok(paymentOrderService.createOrder(request, principal.getId()));
    }

    @PostMapping("/verify")
    public ResponseEntity<PaymentDto.VerifyResponse> verify(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody PaymentDto.VerifyRequest request) {
        return ResponseEntity.ok(paymentOrderService.verify(request, principal.getId()));
    }
}
