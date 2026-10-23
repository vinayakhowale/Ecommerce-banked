package com.socommerce.app.service;

import com.socommerce.app.dto.PostDto;
import com.socommerce.app.entity.*;
import com.socommerce.app.exception.ForbiddenException;
import com.socommerce.app.exception.ResourceNotFoundException;
import com.socommerce.app.repository.InfluencerRepository;
import com.socommerce.app.repository.PostRepository;
import com.socommerce.app.repository.ProductRepository;
import com.socommerce.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * The creator side of influencer videos. A customer account becomes a creator account when the
 * admin saves that account's email on an active influencer profile (Admin -> Influencers).
 * Creators upload videos and tag the products they promote; every upload waits for the admin
 * to approve it before it appears on the storefront.
 */
@Service
@RequiredArgsConstructor
public class CreatorService {

    private static final String NOT_A_CREATOR =
            "This account isn't set up as a creator yet. Ask the store team to add your email to your influencer profile.";

    private final InfluencerRepository influencerRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ProductRepository productRepository;
    private final PostService postService;

    @Transactional(readOnly = true)
    public Influencer requireCreator(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return influencerRepository.findFirstByEmailIgnoreCaseAndActiveTrue(user.getEmail())
                .orElseThrow(() -> new ForbiddenException(NOT_A_CREATOR));
    }

    @Transactional(readOnly = true)
    public List<Post> myVideos(Long userId) {
        Influencer me = requireCreator(userId);
        return postRepository.findByInfluencerIdOrderByCreatedAtDesc(me.getId());
    }

    @Transactional
    public Post submit(Long userId, PostDto.CreatorVideoRequest req) {
        Influencer me = requireCreator(userId);
        User user = userRepository.getReferenceById(userId);

        Post post = Post.builder()
                .influencerName(me.getName())
                .influencerProfileImageUrl(me.getProfileImageUrl())
                .influencer(me)
                .mediaType(MediaType.VIDEO)
                .mediaUrl(req.mediaUrl().trim())
                .thumbnailUrl(blankToNull(req.thumbnailUrl()))
                .caption(blankToNull(req.caption()))
                .hashtags(blankToNull(req.hashtags()))
                .status(PostStatus.DRAFT)          // hidden until approved
                .reviewStatus(PostReviewStatus.PENDING)
                .submittedBy(user)
                .build();

        int position = 0;
        for (Long productId : new LinkedHashSet<>(req.productIds())) {
            Product product = productRepository.findById(productId)
                    .filter(Product::isActive)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
            post.getProducts().add(PostProduct.builder().post(post).product(product).position(position++).build());
        }
        return postRepository.save(post);
    }

    /** Creators can withdraw their own uploads; published ones are taken down too. */
    @Transactional
    public void delete(Long userId, Long postId) {
        Influencer me = requireCreator(userId);
        Post post = postService.getAny(postId);
        if (post.getInfluencer() == null || !post.getInfluencer().getId().equals(me.getId())) {
            throw new ForbiddenException("You can only remove your own videos");
        }
        postService.delete(postId);
    }

    // ---- admin moderation ----

    @Transactional(readOnly = true)
    public Page<Post> pending(Pageable pageable) {
        return postRepository.findByReviewStatusOrderByCreatedAtAsc(PostReviewStatus.PENDING, pageable);
    }

    @Transactional
    public Post approve(Long postId) {
        Post post = postService.getAny(postId);
        post.setReviewStatus(PostReviewStatus.APPROVED);
        post.setReviewNote(null);
        post.setStatus(PostStatus.PUBLISHED);
        return postRepository.save(post);
    }

    @Transactional
    public Post reject(Long postId, String note) {
        Post post = postService.getAny(postId);
        post.setReviewStatus(PostReviewStatus.REJECTED);
        post.setReviewNote(blankToNull(note));
        post.setStatus(PostStatus.UNPUBLISHED);
        return postRepository.save(post);
    }

    private String blankToNull(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }
}
