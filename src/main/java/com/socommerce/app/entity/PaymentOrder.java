package com.socommerce.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A real in-app Razorpay payment attempt/transaction. Unlike the external "Buy Now" redirect
 * flow (where checkout happens on the seller's own site and we can never confirm the sale
 * automatically -- see ProductClick / InfluencerSale), this entity represents money that
 * actually moved through Razorpay on this platform, so its status is authoritative and
 * automatically confirmed via signature verification -- no manual admin entry needed.
 */
@Entity
@Table(name = "payment_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Nullable, unlike most product references elsewhere in this app: a PaymentOrder is a real
     * financial record. If the catalog product is later deleted, this reference is detached
     * (set to null) rather than deleting the payment record itself -- the transaction happened
     * and should remain in history regardless of the product's current catalog status.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    /** Snapshot of the product name at time of purchase, so history reads correctly even after the product/link above is gone. */
    @Column(length = 255)
    private String productNameSnapshot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "influencer_id")
    private Influencer influencer;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer quantity;

    /** Unit price actually charged (after any active discount) -- computed server-side, never trusted from the client. */
    @NotNull
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @NotNull
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Builder.Default
    @Column(nullable = false, length = 8)
    private String currency = "INR";

    @NotBlank
    @Column(nullable = false, unique = true, length = 60)
    private String razorpayOrderId;

    @Column(length = 60)
    private String razorpayPaymentId;

    @Column(length = 200)
    private String razorpaySignature;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.CREATED;

    // ---- Fulfilment / delivery tracking (set by the admin after payment) ----

    /** Null on orders paid before tracking existed; read as CONFIRMED. */
    @Enumerated(EnumType.STRING)
    @Column(length = 24)
    private FulfillmentStatus fulfillmentStatus;

    @Column(length = 120)
    private String courierName;

    @Column(length = 64)
    private String trackingNumber;

    /** The courier's own tracking page, if they have one. */
    @Column(length = 1024)
    private String trackingUrl;

    private java.time.LocalDate expectedDeliveryDate;

    private LocalDateTime packedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime outForDeliveryAt;
    private LocalDateTime deliveredAt;

    /** Shown to the customer, e.g. the reason for a cancellation. */
    @Column(length = 512)
    private String fulfillmentNote;

    /** Customer-facing reference, e.g. TRD-000042. Derived from the id so it never needs storing. */
    @Transient
    public String getOrderNumber() {
        return id == null ? null : String.format("TRD-%06d", id);
    }

    /** The effective status: CONFIRMED until the admin moves it on. */
    @Transient
    public FulfillmentStatus getEffectiveFulfillmentStatus() {
        return fulfillmentStatus == null ? FulfillmentStatus.CONFIRMED : fulfillmentStatus;
    }

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
