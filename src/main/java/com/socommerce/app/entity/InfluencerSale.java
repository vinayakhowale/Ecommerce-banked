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

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A confirmed sale attributed to an influencer's code. Since checkout happens on the external
 * seller's site (see BuyNowService), Trendly cannot automatically know a sale completed -- this
 * record is created by an admin after the fact (e.g. once the seller confirms/reports the order),
 * carrying the real order details. ProductClick already captures automatic click-level
 * attribution; this table captures actual confirmed revenue.
 */
@Entity
@Table(name = "influencer_sales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InfluencerSale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "influencer_id", nullable = false)
    private Influencer influencer;

    /** Denormalized snapshot of the code at time of sale, in case the influencer's code changes later. */
    @NotBlank
    @Column(nullable = false, length = 40)
    private String influencerCodeUsed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String orderId;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime purchaseDate;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer quantity;

    @NotNull
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal orderValue;

    /** Free-text description of the discount applied, e.g. "Diwali Offer -15%" or "Influencer tier -20%". */
    private String discountApplied;

    /** Optional link to a campaign/festival Discount, for campaign-wise reporting. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_id")
    private Discount discount;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
