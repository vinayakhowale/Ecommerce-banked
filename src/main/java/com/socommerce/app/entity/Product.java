package com.socommerce.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(length = 4000)
    private String description;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Builder.Default
    @Column(nullable = false, length = 8)
    private String currency = "INR";

    private String brand;

    private String category;

    private String seller;

    private String productImageUrl;

    /** The canonical original product page the user is redirected to. */
    @NotBlank
    @Column(nullable = false, length = 2048)
    private String productUrl;

    /** Optional affiliate-tagged URL, used preferentially for redirect + tracking if present and approved. */
    @Column(length = 2048)
    private String affiliateUrl;

    /**
     * The seller's Razorpay Linked Account ID (e.g. "acc_IRQWUleX4BqvYn"), set up via Razorpay
     * Route on the platform's dashboard. When present, "Buy Now" opens an in-app Razorpay
     * checkout that pays this product's seller directly. When absent, "Buy Now" falls back to
     * the external redirect flow (productUrl/affiliateUrl) as before -- so products belonging to
     * sellers who haven't been onboarded to Razorpay Route keep working exactly as they do today.
     */
    private String razorpayAccountId;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    /** Domain allow-list flag: only URLs whose host has been validated/approved can be redirected to. */
    @Builder.Default
    @Column(nullable = false)
    private boolean urlApproved = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
