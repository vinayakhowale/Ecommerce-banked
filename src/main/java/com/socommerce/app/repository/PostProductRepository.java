package com.socommerce.app.repository;

import com.socommerce.app.entity.PostProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostProductRepository extends JpaRepository<PostProduct, Long> {
    boolean existsByProductId(Long productId);
    void deleteByProductId(Long productId);
}
