package com.socommerce.app.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/** A customer's star rating and written review of a product. One per customer per product. */
@Entity
@Table(name = "product_reviews",
        uniqueConstraints = @UniqueConstraint(name = "uk_review_user_product", columnNames = {"user_id", "product_id"}),
        indexes = @Index(name = "idx_reviews_product", columnList = "product_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Shown publicly instead of the account email, e.g. "Shivam C." */
    @Column(nullable = false, length = 80)
    private String reviewerName;

    /** 1 to 5 stars. */
    @Column(nullable = false)
    private int rating;

    @Column(length = 120)
    private String title;

    @Column(length = 2000)
    private String comment;

    /** True when the reviewer has a paid order for this product at the time of review. */
    @Builder.Default
    @Column(nullable = false)
    private boolean verifiedPurchase = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
