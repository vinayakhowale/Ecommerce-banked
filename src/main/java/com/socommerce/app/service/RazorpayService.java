package com.socommerce.app.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.socommerce.app.exception.BadRequestException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;


@Slf4j
@Service
public class RazorpayService {

    @Value("${app.razorpay.key-id}")
    private String keyId;

    @Value("${app.razorpay.key-secret}")
    private String keySecret;

    @PostConstruct
    void logActiveMode() {
        String mode = isLiveMode() ? "LIVE — real money will move" : "TEST — safe, no real money moves";
        log.info("Razorpay initialized in {} mode (key: {}...)", mode,
                keyId.length() > 12 ? keyId.substring(0, 12) : keyId);
    }

    public boolean isLiveMode() {
        return keyId != null && keyId.startsWith("rzp_live_");
    }

    public String getPublicKeyId() {
        return keyId;
    }

    /**
     * Creates a Razorpay order for the given amount. If a seller Linked Account ID is provided,
     * the order is created with a Route "transfer" so the full amount automatically moves to
     * that seller's own Razorpay account once payment is captured -- this is what makes "a
     * different account for every product" work. Without an account ID, the full amount simply
     * stays in the platform's main account (useful for products whose seller hasn't been
     * onboarded to Route yet).
     */
    public Order createOrder(BigDecimal totalAmount, String currency, String receipt, String sellerAccountId) {
        try {
            RazorpayClient client = new RazorpayClient(keyId, keySecret);

            long amountInSmallestUnit = totalAmount
                    .setScale(2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .longValueExact();

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInSmallestUnit);
            orderRequest.put("currency", currency);
            orderRequest.put("receipt", receipt);
            orderRequest.put("partial_payment", false);

            if (sellerAccountId != null && !sellerAccountId.isBlank()) {
                JSONObject transfer = new JSONObject();
                transfer.put("account", sellerAccountId);
                transfer.put("amount", amountInSmallestUnit);
                transfer.put("currency", currency);
                transfer.put("on_hold", false);

                JSONArray transfers = new JSONArray();
                transfers.put(transfer);
                orderRequest.put("transfers", transfers);
            }

            return client.orders.create(orderRequest);
        } catch (RazorpayException e) {
            throw new BadRequestException("Could not create payment order: " + e.getMessage());
        }
    }

    /** Verifies that a payment callback genuinely came from Razorpay and matches the given order. */
    public boolean verifySignature(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", razorpayOrderId);
            options.put("razorpay_payment_id", razorpayPaymentId);
            options.put("razorpay_signature", razorpaySignature);
            return Utils.verifyPaymentSignature(options, keySecret);
        } catch (RazorpayException e) {
            return false;
        }
    }
}
