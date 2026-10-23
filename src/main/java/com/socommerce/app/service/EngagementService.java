package com.socommerce.app.service;

import com.socommerce.app.entity.Post;
import com.socommerce.app.entity.PostLike;
import com.socommerce.app.entity.PostSave;
import com.socommerce.app.entity.User;
import com.socommerce.app.exception.ResourceNotFoundException;
import com.socommerce.app.repository.PostLikeRepository;
import com.socommerce.app.repository.PostRepository;
import com.socommerce.app.repository.PostSaveRepository;
import com.socommerce.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EngagementService {

    private final PostLikeRepository postLikeRepository;
    private final PostSaveRepository postSaveRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public boolean toggleLike(Long postId, Long userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        var existing = postLikeRepository.findByPostIdAndUserId(postId, userId);
        if (existing.isPresent()) {
            postLikeRepository.delete(existing.get());
            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
            postRepository.save(post);
            return false;
        } else {
            User userRef = userRepository.getReferenceById(userId);
            postLikeRepository.save(PostLike.builder().post(post).user(userRef).build());
            post.setLikeCount(post.getLikeCount() + 1);
            postRepository.save(post);
            return true;
        }
    }

    @Transactional
    public boolean toggleSave(Long postId, Long userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        var existing = postSaveRepository.findByPostIdAndUserId(postId, userId);
        if (existing.isPresent()) {
            postSaveRepository.delete(existing.get());
            post.setSaveCount(Math.max(0, post.getSaveCount() - 1));
            postRepository.save(post);
            return false;
        } else {
            User userRef = userRepository.getReferenceById(userId);
            postSaveRepository.save(PostSave.builder().post(post).user(userRef).build());
            post.setSaveCount(post.getSaveCount() + 1);
            postRepository.save(post);
            return true;
        }
    }
}
