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
import java.util.ArrayList;
import java.util.List;

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

    /** Primary image, kept as the single thumbnail used across listings, cart and analytics. */
    private String productImageUrl;

    /**
     * Full image gallery, in display order. The first entry is mirrored into productImageUrl so
     * every existing screen that reads a single image keeps working untouched.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @OrderColumn(name = "sort_order")
    @Column(name = "image_url", length = 2048)
    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();

    /** Primary video, kept as a single field so older rows and simple reads keep working. */
    @Column(length = 2048)
    private String videoUrl;

    /**
     * Full video gallery, in display order. The first entry is mirrored into videoUrl, exactly
     * as the image gallery mirrors into productImageUrl.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_videos", joinColumns = @JoinColumn(name = "product_id"))
    @OrderColumn(name = "sort_order")
    @Column(name = "video_url", length = 2048)
    @Builder.Default
    private List<String> videoUrls = new ArrayList<>();

    /**
     * Optional external product page. Checkout now always happens in-app through Razorpay, so this
     * is no longer captured in the admin form and is left null for products created there. It is
     * kept on the entity (nullable) so existing rows and the legacy redirect flow keep working.
     */
    @Column(length = 2048)
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
