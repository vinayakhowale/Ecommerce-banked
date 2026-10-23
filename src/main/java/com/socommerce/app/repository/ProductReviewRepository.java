package com.socommerce.app.repository;

import com.socommerce.app.entity.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    Page<ProductReview> findByProductIdOrderByCreatedAtDesc(Long productId, Pageable pageable);

    Page<ProductReview> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Optional<ProductReview> findByProductIdAndUserId(Long productId, Long userId);

    /** Rows of [productId, average rating, review count] for the given products. */
    @Query("select r.product.id, avg(r.rating), count(r) from ProductReview r " +
           "where r.product.id in :productIds group by r.product.id")
    List<Object[]> summarize(@Param("productIds") Collection<Long> productIds);

    /** Rows of [stars, count] for one product, used for the rating breakdown bars. */
    @Query("select r.rating, count(r) from ProductReview r where r.product.id = :productId group by r.rating")
    List<Object[]> distribution(@Param("productId") Long productId);

    @Modifying
    @Query("delete from ProductReview r where r.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);
}
