package com.socommerce.app.service;

import com.socommerce.app.dto.BuyNowDto;
import com.socommerce.app.entity.Influencer;
import com.socommerce.app.entity.Post;
import com.socommerce.app.entity.Product;
import com.socommerce.app.entity.ProductClick;
import com.socommerce.app.entity.User;
import com.socommerce.app.exception.BadRequestException;
import com.socommerce.app.exception.ResourceNotFoundException;
import com.socommerce.app.repository.PostRepository;
import com.socommerce.app.repository.ProductClickRepository;
import com.socommerce.app.repository.ProductRepository;
import com.socommerce.app.repository.UserRepository;
import com.socommerce.app.util.RedirectUrlValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements the "Buy Now" flow end to end:
 * 1) Look up the product server-side (never trust a client-supplied URL).
 * 2) Verify it exists, is active, and its stored URL(s) are approved & safe.
 * 3) Record the click for analytics / affiliate tracking -- including which influencer's
 *    referral code (if any) was active for this click, for automatic code-usage reporting.
 * 4) Return only the validated URL for the frontend to redirect to.
 */
@Service
@RequiredArgsConstructor
public class BuyNowService {

    private final ProductRepository productRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ProductClickRepository productClickRepository;
    private final InfluencerService influencerService;
    private final RedirectUrlValidator redirectUrlValidator;

    @Transactional
    public BuyNowDto.Response buyNow(BuyNowDto.Request req, Long userId, String ipAddress, String userAgent) {
        Product product = productRepository.findById(req.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!product.isActive()) {
            throw new BadRequestException("This product is no longer available for purchase");
        }
        if (!product.isUrlApproved()) {
            throw new BadRequestException("This product's link has not been approved");
        }

        String safeUrl = redirectUrlValidator.resolveSafeRedirect(product.getAffiliateUrl(), product.getProductUrl());
        if (safeUrl == null) {
            throw new BadRequestException("This product's link is currently unavailable");
        }

        Post post = null;
        if (req.postId() != null) {
            post = postRepository.findById(req.postId()).orElse(null);
        }

        // An unrecognized or inactive code is silently ignored rather than rejected -- a broken
        // referral link should never block a customer from completing their purchase.
        Influencer influencer = influencerService.findActiveByCode(req.influencerCode()).orElse(null);

        User user = userId != null ? userRepository.getReferenceById(userId) : null;

        ProductClick click = ProductClick.builder()
                .product(product)
                .post(post)
                .user(user)
                .influencer(influencer)
                .redirectedUrl(safeUrl)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
        click = productClickRepository.save(click);

        return new BuyNowDto.Response(safeUrl, click.getId());
    }
}
