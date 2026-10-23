package com.socommerce.app.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A customer order: what was bought, where it goes, what was paid, and where it has reached.
 *
 * The delivery address is copied in field by field rather than referenced, so editing or
 * deleting an address later cannot change the shipping details of an order already placed.
 */
@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_user", columnList = "user_id"),
        @Index(name = "idx_orders_status", columnList = "status"),
        @Index(name = "idx_orders_number", columnList = "orderNumber", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Human-readable reference shown to the customer, e.g. TRD-2026-0001. */
    @Column(nullable = false, unique = true, length = 32)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    // ---- Delivery address, snapshotted at checkout ----
    @Column(nullable = false) private String shipName;
    @Column(nullable = false, length = 20) private String shipPhone;
    @Column(nullable = false, length = 255) private String shipLine1;
    @Column(length = 255) private String shipLine2;
    @Column(length = 120) private String shipLandmark;
    @Column(nullable = false, length = 120) private String shipCity;
    @Column(nullable = false, length = 120) private String shipState;
    @Column(nullable = false, length = 10) private String shipPincode;

    // ---- Money ----
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal subtotal;
    @Builder.Default
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal shippingFee = BigDecimal.ZERO;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal total;
    @Builder.Default
    @Column(nullable = false, length = 8) private String currency = "INR";

    // ---- Payment ----
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private OrderStatus status = OrderStatus.PENDING_PAYMENT;

    @Column(length = 64) private String razorpayOrderId;
    @Column(length = 64) private String razorpayPaymentId;
    @Column(length = 256) private String razorpaySignature;
    private LocalDateTime paidAt;

    // ---- Shipping ----
    @Column(length = 120) private String courierName;
    @Column(length = 64) private String trackingNumber;
    @Column(length = 512) private String trackingUrl;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;

    /** Why an order was cancelled or returned, shown to the customer. */
    @Column(length = 512) private String statusNote;

    /** Influencer referral code used at checkout, for attribution. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "influencer_id")
    private Influencer influencer;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /** Keeps both sides of the relationship in step when building an order. */
    public void addItem(OrderItem item) {
        item.setOrder(this);
        this.items.add(item);
    }
}
