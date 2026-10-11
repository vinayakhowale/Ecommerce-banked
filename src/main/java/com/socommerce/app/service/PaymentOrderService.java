package com.socommerce.app.service;

import com.razorpay.Order;
import com.socommerce.app.dto.PaymentDto;
import com.socommerce.app.entity.*;
import com.socommerce.app.exception.BadRequestException;
import com.socommerce.app.exception.ResourceNotFoundException;
import com.socommerce.app.repository.InfluencerSaleRepository;
import com.socommerce.app.repository.PaymentOrderRepository;
import com.socommerce.app.repository.ProductRepository;
import com.socommerce.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Orchestrates the real in-app Razorpay checkout flow: create a payment-pending order, then
 * confirm it once Razorpay's callback signature has been verified. This is entirely separate
 * from BuyNowService (the external-redirect flow) -- a product only goes through this path once
 * its seller has a Razorpay Linked Account configured (see Product.razorpayAccountId).
 */
@Service
@RequiredArgsConstructor
public class PaymentOrderService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final DiscountService discountService;
    private final RazorpayService razorpayService;
    private final InfluencerService influencerService;
    private final InfluencerSaleRepository influencerSaleRepository;

    @Transactional
    public PaymentDto.CreateOrderResponse createOrder(PaymentDto.CreateOrderRequest req, Long userId) {
        Product product = productRepository.findById(req.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!product.isActive()) {
            throw new BadRequestException("This product is no longer available for purchase");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Server computes the actual price -- exactly the same integrity guarantee as the cart:
        // the client only ever supplies a product ID and quantity, never an amount.
        DiscountService.EffectiveDiscount discount = discountService.computeEffectivePrice(product);
        BigDecimal unitPrice = discount.hasDiscount() ? discount.finalPrice() : product.getPrice();
        BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(req.quantity())).setScale(2, RoundingMode.HALF_UP);

        Influencer influencer = influencerService.findActiveByCode(req.influencerCode()).orElse(null);

        String receipt = "trendly_" + UUID.randomUUID().toString().substring(0, 24);
        Order razorpayOrder = razorpayService.createOrder(
                totalAmount, product.getCurrency(), receipt, product.getRazorpayAccountId());

        String razorpayOrderId = razorpayOrder.get("id");

        PaymentOrder paymentOrder = PaymentOrder.builder()
                .user(user)
                .product(product)
                .productNameSnapshot(product.getName())
                .influencer(influencer)
                .quantity(req.quantity())
                .unitPrice(unitPrice)
                .totalAmount(totalAmount)
                .currency(product.getCurrency())
                .razorpayOrderId(razorpayOrderId)
                .status(PaymentStatus.CREATED)
                .build();
        paymentOrder = paymentOrderRepository.save(paymentOrder);

        long amountInPaise = totalAmount.multiply(BigDecimal.valueOf(100)).longValueExact();

        return new PaymentDto.CreateOrderResponse(
                paymentOrder.getId(), razorpayOrderId, razorpayService.getPublicKeyId(),
                amountInPaise, product.getCurrency(), product.getName(), product.getProductImageUrl()
        );
    }

    @Transactional
    public PaymentDto.VerifyResponse verify(PaymentDto.VerifyRequest req, Long userId) {
        PaymentOrder paymentOrder = paymentOrderRepository.findByRazorpayOrderId(req.razorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment order not found"));

        if (!paymentOrder.getUser().getId().equals(userId)) {
            throw new BadRequestException("This payment order does not belong to you");
        }
        if (paymentOrder.getStatus() == PaymentStatus.PAID) {
            // Idempotent: verifying an already-confirmed order just returns its current state.
            return toVerifyResponse(paymentOrder);
        }

        boolean valid = razorpayService.verifySignature(
                req.razorpayOrderId(), req.razorpayPaymentId(), req.razorpaySignature());

        if (!valid) {
            paymentOrder.setStatus(PaymentStatus.FAILED);
            paymentOrderRepository.save(paymentOrder);
            throw new BadRequestException("Payment verification failed. If money was deducted, it will be refunded automatically.");
        }

        paymentOrder.setRazorpayPaymentId(req.razorpayPaymentId());
        paymentOrder.setRazorpaySignature(req.razorpaySignature());
        paymentOrder.setStatus(PaymentStatus.PAID);
        paymentOrderRepository.save(paymentOrder);

        // Unlike the external Buy Now flow, a real Razorpay payment IS a confirmed sale --
        // record it automatically instead of requiring the admin to enter it manually.
        if (paymentOrder.getInfluencer() != null) {
            InfluencerSale sale = InfluencerSale.builder()
                    .influencer(paymentOrder.getInfluencer())
                    .influencerCodeUsed(paymentOrder.getInfluencer().getCode())
                    .product(paymentOrder.getProduct())
                    .orderId("PAY-" + paymentOrder.getId())
                    .purchaseDate(LocalDateTime.now())
                    .quantity(paymentOrder.getQuantity())
                    .orderValue(paymentOrder.getTotalAmount())
                    .discountApplied(null)
                    .build();
            influencerSaleRepository.save(sale);
        }

        return toVerifyResponse(paymentOrder);
    }

    private PaymentDto.VerifyResponse toVerifyResponse(PaymentOrder paymentOrder) {
        String productName = paymentOrder.getProduct() != null
                ? paymentOrder.getProduct().getName()
                : paymentOrder.getProductNameSnapshot();
        return new PaymentDto.VerifyResponse(
                paymentOrder.getId(), paymentOrder.getStatus().name(),
                productName, paymentOrder.getTotalAmount(), paymentOrder.getCurrency()
        );
    }
}
