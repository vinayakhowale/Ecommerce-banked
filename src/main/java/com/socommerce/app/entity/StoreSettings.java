package com.socommerce.app.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Store-wide branding and contact details, editable by an admin.
 *
 * There is exactly one row, pinned to id = 1. SettingsService creates it on first read with
 * sensible defaults, so a fresh database never leaves the storefront without a name.
 */
@Entity
@Table(name = "store_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreSettings {

    /** Always 1 -- this table holds a single row. */
    @Id
    private Long id;

    @Column(nullable = false, length = 60)
    private String storeName;

    /** One line used on the sign-in and sign-up screens. */
    @Column(length = 160)
    private String tagline;

    /** Messages in the scrolling bar at the top of every storefront page. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "store_announcements", joinColumns = @JoinColumn(name = "settings_id"))
    @OrderColumn(name = "sort_order")
    @Column(name = "message", length = 160)
    @Builder.Default
    private List<String> announcements = new ArrayList<>();

    @Column(length = 160)
    private String supportEmail;

    @Column(length = 40)
    private String supportPhone;

    /** Shown in the footer, e.g. "10am-7pm, Mon-Fri". */
    @Column(length = 120)
    private String supportHours;

    // ---- About Us page ----
    @Column(length = 120)
    private String aboutHeading;

    @Column(length = 4000)
    private String aboutBody;

    @Column(length = 2048)
    private String aboutImageUrl;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
