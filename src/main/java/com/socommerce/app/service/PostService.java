package com.socommerce.app.service;

import com.socommerce.app.dto.PostDto;
import com.socommerce.app.entity.Influencer;
import com.socommerce.app.entity.Post;
import com.socommerce.app.entity.PostProduct;
import com.socommerce.app.entity.PostStatus;
import com.socommerce.app.entity.Product;
import com.socommerce.app.exception.ResourceNotFoundException;
import com.socommerce.app.repository.InfluencerRepository;
import com.socommerce.app.repository.PostLikeRepository;
import com.socommerce.app.repository.PostRepository;
import com.socommerce.app.repository.PostSaveRepository;
import com.socommerce.app.repository.ProductClickRepository;
import com.socommerce.app.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final ProductRepository productRepository;
    private final ProductClickRepository productClickRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostSaveRepository postSaveRepository;
    private final InfluencerRepository influencerRepository;

    @Transactional(readOnly = true)
    public Page<Post> feed(Pageable pageable) {
        return postRepository.findByStatusOrderByCreatedAtDesc(PostStatus.PUBLISHED, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Post> search(String q, Pageable pageable) {
        return postRepository.search(q, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Post> mostViewed(Pageable pageable) {
        return postRepository.findMostViewed(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Post> adminList(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Post getPublishedOrThrow(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        if (post.getStatus() != PostStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Post not found");
        }
        return post;
    }

    @Transactional(readOnly = true)
    public Post getAny(Long id) {
        return postRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post not found"));
    }

    @Transactional
    public void recordView(Long id) {
        Post post = getAny(id);
        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);
    }

    @Transactional
    public Post create(PostDto.Request req) {
        Post post = Post.builder()
                .influencerName(req.influencerName())
                .influencerProfileImageUrl(req.influencerProfileImageUrl())
                .influencer(resolveInfluencer(req.influencerId()))
                .mediaType(req.mediaType())
                .mediaUrl(req.mediaUrl())
                .thumbnailUrl(req.thumbnailUrl())
                .caption(req.caption())
                .hashtags(req.hashtags())
                .status(req.status() == null ? PostStatus.DRAFT : req.status())
                .build();
        attachProducts(post, req.productIds());
        return postRepository.save(post);
    }

    @Transactional
    public Post update(Long id, PostDto.Request req) {
        Post post = getAny(id);
        post.setInfluencerName(req.influencerName());
        post.setInfluencerProfileImageUrl(req.influencerProfileImageUrl());
        post.setInfluencer(resolveInfluencer(req.influencerId()));
        post.setMediaType(req.mediaType());
        post.setMediaUrl(req.mediaUrl());
        post.setThumbnailUrl(req.thumbnailUrl());
        post.setCaption(req.caption());
        post.setHashtags(req.hashtags());
        if (req.status() != null) post.setStatus(req.status());

        post.getProducts().clear();
        attachProducts(post, req.productIds());
        return postRepository.save(post);
    }

    private Influencer resolveInfluencer(Long influencerId) {
        if (influencerId == null) return null;
        return influencerRepository.findById(influencerId)
                .orElseThrow(() -> new ResourceNotFoundException("Influencer not found: " + influencerId));
    }

    private void attachProducts(Post post, List<Long> productIds) {
        if (productIds == null) return;
        List<PostProduct> links = new ArrayList<>();
        int position = 0;
        for (Long productId : productIds) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
            links.add(PostProduct.builder().post(post).product(product).position(position++).build());
        }
        post.getProducts().addAll(links);
    }

    @Transactional
    public Post setStatus(Long id, PostStatus status) {
        Post post = getAny(id);
        post.setStatus(status);
        return postRepository.save(post);
    }

    @Transactional
    public void delete(Long id) {
        Post post = getAny(id);
        // Post.products already cascades via orphanRemoval, but ProductClick/PostLike/PostSave
        // are independent history/analytics tables that reference this post with no cascade
        // configured -- deleting the post directly would otherwise fail with a foreign-key
        // constraint violation the moment anyone had ever clicked, liked, or saved it.
        productClickRepository.deleteByPostId(id);
        postLikeRepository.deleteByPostId(id);
        postSaveRepository.deleteByPostId(id);
        postRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public List<Post> postsForProduct(Long productId) {
        return postRepository.findPublishedPostsByProductId(productId);
    }
}
