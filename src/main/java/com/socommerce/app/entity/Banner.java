package com.socommerce.app.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * A promotional image the admin places on the home page or the Collections page.
 * Several active banners in one placement are shown as a carousel, in sortOrder.
 */
@Entity
@Table(name = "banners", indexes = @Index(name = "idx_banners_placement", columnList = "placement, active, sortOrder"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BannerPlacement placement;

    /** Wide image used on desktop and tablets. */
    @Column(nullable = false, length = 2048)
    private String imageUrl;

    /** Optional taller crop for phones; falls back to imageUrl. */
    @Column(length = 2048)
    private String mobileImageUrl;

    @Column(length = 120)
    private String title;

    @Column(length = 240)
    private String subtitle;

    /** Button text, e.g. "Shop now". No button is shown when empty. */
    @Column(length = 40)
    private String ctaLabel;

    /** Where a click goes: an in-store path like /categories?category=Fragrance, or a full URL. */
    @Column(length = 1024)
    private String linkUrl;

    @Builder.Default
    @Column(nullable = false)
    private int sortOrder = 0;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
