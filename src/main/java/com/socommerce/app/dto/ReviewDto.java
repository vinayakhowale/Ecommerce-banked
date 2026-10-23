package com.socommerce.app.dto;

import com.socommerce.app.entity.ProductReview;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Map;

public class ReviewDto {

    public record Request(
            @Min(value = 1, message = "Choose a rating from 1 to 5 stars")
            @Max(value = 5, message = "Choose a rating from 1 to 5 stars") int rating,
            @Size(max = 120, message = "Keep the title under 120 characters") String title,
            @Size(max = 2000, message = "Keep the review under 2000 characters") String comment
    ) {}

    public record Response(
            Long id, Long productId, String productName, String reviewerName, int rating,
            String title, String comment, boolean verifiedPurchase, LocalDateTime createdAt
    ) {
        public static Response from(ProductReview r) {
            return new Response(r.getId(), r.getProduct().getId(), r.getProduct().getName(),
                    r.getReviewerName(), r.getRating(), r.getTitle(), r.getComment(),
                    r.isVerifiedPurchase(), r.getCreatedAt());
        }
    }

    /** Average and count for one product, used on product cards. */
    public record Summary(Long productId, double averageRating, long reviewCount) {}

    /** Summary plus the count for each star level (keys 1-5), used on the product page. */
    public record Detail(Long productId, double averageRating, long reviewCount, Map<Integer, Long> distribution) {}
}
