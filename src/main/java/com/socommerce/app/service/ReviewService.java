package com.socommerce.app.service;

import com.socommerce.app.dto.ReviewDto;
import com.socommerce.app.entity.PaymentStatus;
import com.socommerce.app.entity.Product;
import com.socommerce.app.entity.ProductReview;
import com.socommerce.app.entity.User;
import com.socommerce.app.exception.ResourceNotFoundException;
import com.socommerce.app.repository.PaymentOrderRepository;
import com.socommerce.app.repository.ProductRepository;
import com.socommerce.app.repository.ProductReviewRepository;
import com.socommerce.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ProductReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PaymentOrderRepository paymentOrderRepository;

    @Transactional(readOnly = true)
    public Page<ReviewDto.Response> forProduct(Long productId, Pageable pageable) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable).map(ReviewDto.Response::from);
    }

    @Transactional(readOnly = true)
    public Page<ReviewDto.Response> listAll(Pageable pageable) {
        return reviewRepository.findAllByOrderByCreatedAtDesc(pageable).map(ReviewDto.Response::from);
    }

    @Transactional(readOnly = true)
    public List<ReviewDto.Summary> summaries(Collection<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) return List.of();
        List<ReviewDto.Summary> out = new ArrayList<>();
        for (Object[] row : reviewRepository.summarize(productIds)) {
            out.add(new ReviewDto.Summary((Long) row[0], round1(((Number) row[1]).doubleValue()), ((Number) row[2]).longValue()));
        }
        return out;
    }

    @Transactional(readOnly = true)
    public ReviewDto.Detail detail(Long productId) {
        Map<Integer, Long> dist = new LinkedHashMap<>();
        for (int s = 5; s >= 1; s--) dist.put(s, 0L);
        long count = 0;
        long sum = 0;
        for (Object[] row : reviewRepository.distribution(productId)) {
            int stars = ((Number) row[0]).intValue();
            long n = ((Number) row[1]).longValue();
            dist.put(stars, n);
            count += n;
            sum += stars * n;
        }
        double avg = count == 0 ? 0 : round1((double) sum / count);
        return new ReviewDto.Detail(productId, avg, count, dist);
    }

    @Transactional(readOnly = true)
    public Optional<ReviewDto.Response> mine(Long productId, Long userId) {
        return reviewRepository.findByProductIdAndUserId(productId, userId).map(ReviewDto.Response::from);
    }

    /** Creates the customer's review, or updates it if they have already reviewed this product. */
    @Transactional
    public ReviewDto.Response upsert(Long productId, Long userId, ReviewDto.Request req) {
        Product product = productRepository.findById(productId)
                .filter(Product::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ProductReview review = reviewRepository.findByProductIdAndUserId(productId, userId)
                .orElseGet(() -> ProductReview.builder().product(product).user(user).build());

        review.setReviewerName(displayName(user.getName()));
        review.setRating(req.rating());
        review.setTitle(trimToNull(req.title()));
        review.setComment(trimToNull(req.comment()));
        review.setVerifiedPurchase(paymentOrderRepository
                .existsByUserIdAndProductIdAndStatus(userId, productId, PaymentStatus.PAID));
        return ReviewDto.Response.from(reviewRepository.save(review));
    }

    @Transactional
    public void delete(Long reviewId) {
        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        reviewRepository.delete(review);
    }

    /** "Shivam Chaube" becomes "Shivam C." so reviews never publish a full name. */
    private String displayName(String fullName) {
        if (fullName == null || fullName.isBlank()) return "Customer";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) return parts[0];
        return parts[0] + " " + Character.toUpperCase(parts[parts.length - 1].charAt(0)) + ".";
    }

    private double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    private String trimToNull(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }
}
