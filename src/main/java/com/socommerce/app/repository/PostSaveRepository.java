package com.socommerce.app.repository;

import com.socommerce.app.entity.PostSave;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostSaveRepository extends JpaRepository<PostSave, Long> {
    Optional<PostSave> findByPostIdAndUserId(Long postId, Long userId);
    long countByPostId(Long postId);
    java.util.List<PostSave> findByUserId(Long userId);
    void deleteByPostId(Long postId);
}
