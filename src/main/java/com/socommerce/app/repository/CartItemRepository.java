package com.socommerce.app.repository;

import com.socommerce.app.entity.CartItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);
    void deleteByUserId(Long userId);
    boolean existsByProductId(Long productId);
    void deleteByProductId(Long productId);

    /** Admin-wide view across every user's cart, oldest-added-first -- surfaces likely-abandoned items first. */
    Page<CartItem> findAllByOrderByCreatedAtAsc(Pageable pageable);
}
