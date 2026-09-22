package com.socommerce.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String influencerName;

    private String influencerProfileImageUrl;

    /**
     * Optional link to a real Influencer record with a trackable referral code. Kept separate
     * from influencerName (free-text display name) so existing posts keep working unlinked, and
     * an admin can attach a code-bearing Influencer to a post once that influencer is set up.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "influencer_id")
    private Influencer influencer;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaType mediaType;

    @NotBlank
    @Column(nullable = false, length = 2048)
    private String mediaUrl;

    @Column(length = 2048)
    private String thumbnailUrl;

    @Column(length = 4000)
    private String caption;

    /** Stored as comma separated tags */
    @Column(length = 1000)
    private String hashtags;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private PostStatus status = PostStatus.DRAFT;

    @Builder.Default
    @Column(nullable = false)
    private long viewCount = 0L;

    @Builder.Default
    @Column(nullable = false)
    private long likeCount = 0L;

    @Builder.Default
    @Column(nullable = false)
    private long saveCount = 0L;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<PostProduct> products = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
