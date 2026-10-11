package com.socommerce.app.dto;

import com.socommerce.app.entity.MediaType;
import com.socommerce.app.entity.Post;
import com.socommerce.app.entity.PostStatus;
import com.socommerce.app.service.DiscountService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class PostDto {

    public record Request(
            @NotBlank String influencerName,
            String influencerProfileImageUrl,
            Long influencerId,
            @NotNull MediaType mediaType,
            @NotBlank String mediaUrl,
            String thumbnailUrl,
            String caption,
            String hashtags,
            List<Long> productIds,
            PostStatus status
    ) {}

    public record Response(
            Long id, String influencerName, String influencerProfileImageUrl,
            Long influencerId, String influencerCode,
            MediaType mediaType, String mediaUrl, String thumbnailUrl,
            String caption, String hashtags, PostStatus status,
            long viewCount, long likeCount, long saveCount,
            List<ProductDto.Response> products,
            java.time.LocalDateTime createdAt
    ) {
        /** Plain response with no discount computed on tagged products -- used by admin contexts. */
        public static Response from(Post p) {
            List<ProductDto.Response> products = p.getProducts().stream()
                    .sorted((a, b) -> Integer.compare(a.getPosition(), b.getPosition()))
                    .map(pp -> ProductDto.Response.from(pp.getProduct()))
                    .toList();
            return build(p, products);
        }

        /** Enriched response with discounts resolved on each tagged product -- used by public/customer-facing endpoints. */
        public static Response from(Post p, DiscountService discountService) {
            List<ProductDto.Response> products = p.getProducts().stream()
                    .sorted((a, b) -> Integer.compare(a.getPosition(), b.getPosition()))
                    .map(pp -> ProductDto.Response.from(pp.getProduct(), discountService.computeEffectivePrice(pp.getProduct())))
                    .toList();
            return build(p, products);
        }

        private static Response build(Post p, List<ProductDto.Response> products) {
            return new Response(p.getId(), p.getInfluencerName(), p.getInfluencerProfileImageUrl(),
                    p.getInfluencer() != null ? p.getInfluencer().getId() : null,
                    p.getInfluencer() != null ? p.getInfluencer().getCode() : null,
                    p.getMediaType(), p.getMediaUrl(), p.getThumbnailUrl(), p.getCaption(), p.getHashtags(),
                    p.getStatus(), p.getViewCount(), p.getLikeCount(), p.getSaveCount(), products, p.getCreatedAt());
        }
    }
}
