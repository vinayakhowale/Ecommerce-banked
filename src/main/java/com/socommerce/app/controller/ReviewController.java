package com.socommerce.app.controller;

import com.socommerce.app.dto.ReviewDto;
import com.socommerce.app.security.UserPrincipal;
import com.socommerce.app.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Reading reviews is public (GET under /api/public); writing one needs a signed-in customer.
 */
@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/api/public/products/{productId}/reviews")
    public ResponseEntity<Page<ReviewDto.Response>> list(@PathVariable Long productId,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.forProduct(productId, PageRequest.of(page, Math.min(size, 50))));
    }

    @GetMapping("/api/public/products/{productId}/reviews/summary")
    public ResponseEntity<ReviewDto.Detail> detail(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.detail(productId));
    }

    /** Ratings for a whole grid of product cards in one request, e.g. ?productIds=1,2,3 */
    @GetMapping("/api/public/reviews/summary")
    public ResponseEntity<List<ReviewDto.Summary>> summaries(@RequestParam String productIds) {
        List<Long> ids = Arrays.stream(productIds.split(","))
                .map(String::trim)
                .filter(s -> s.matches("\\d{1,18}"))
                .map(Long::valueOf)
                .distinct()
                .limit(100)
                .toList();
        return ResponseEntity.ok(reviewService.summaries(ids));
    }

    @GetMapping("/api/products/{productId}/reviews/mine")
    public ResponseEntity<ReviewDto.Response> mine(@PathVariable Long productId,
                                                   @AuthenticationPrincipal UserPrincipal principal) {
        return reviewService.mine(productId, principal.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/api/products/{productId}/reviews")
    public ResponseEntity<ReviewDto.Response> submit(@PathVariable Long productId,
                                                     @Valid @RequestBody ReviewDto.Request request,
                                                     @AuthenticationPrincipal UserPrincipal principal) {
        Objects.requireNonNull(principal);
        return ResponseEntity.ok(reviewService.upsert(productId, principal.getId(), request));
    }
}
