package com.socommerce.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * A real-world influencer/creator who can be given a unique referral code to share with their
 * audience. Distinct from Post.influencerName (free-text display name on a post) -- an Influencer
 * record is what carries a trackable code, sales history, and tiered discount configuration.
 * A Post may optionally be linked to one via Post.influencer once the admin sets it up with a code.
 */
@Entity
@Table(name = "influencers", uniqueConstraints = {
        @UniqueConstraint(columnNames = "code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Influencer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    /** Unique referral/tracking code shared with the influencer's audience, e.g. "RIA20". */
    @NotBlank
    @Column(nullable = false, unique = true, length = 40)
    private String code;

    private String profileImageUrl;

    private String email;

    private String phone;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    /**
     * The admin-maintained "official" confirmed sales count for this influencer. Since actual
     * checkout happens on an external seller's site (see BuyNowService), the platform has no
     * automatic way to confirm a purchase completed -- this number is manually entered/updated
     * by an admin whenever confirmed purchase information is received, and is treated as the
     * authoritative sales figure for tier/discount decisions (see InfluencerDiscount).
     */
    @Builder.Default
    @Column(nullable = false)
    private int salesCount = 0;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
