package com.socommerce.app.dto;

import com.socommerce.app.entity.Banner;
import com.socommerce.app.entity.BannerPlacement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class BannerDto {

    public record Request(
            @NotNull(message = "Choose where the banner appears") BannerPlacement placement,
            @NotBlank(message = "Upload a banner image") String imageUrl,
            String mobileImageUrl,
            @Size(max = 120) String title,
            @Size(max = 240) String subtitle,
            @Size(max = 40) String ctaLabel,
            @Size(max = 1024) String linkUrl,
            Integer sortOrder,
            Boolean active
    ) {}

    public record Response(
            Long id, BannerPlacement placement, String imageUrl, String mobileImageUrl,
            String title, String subtitle, String ctaLabel, String linkUrl,
            int sortOrder, boolean active, LocalDateTime createdAt
    ) {
        public static Response from(Banner b) {
            return new Response(b.getId(), b.getPlacement(), b.getImageUrl(), b.getMobileImageUrl(),
                    b.getTitle(), b.getSubtitle(), b.getCtaLabel(), b.getLinkUrl(),
                    b.getSortOrder(), b.isActive(), b.getCreatedAt());
        }
    }
}
