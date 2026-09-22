package com.socommerce.app.repository;

import com.socommerce.app.entity.AddToCartEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddToCartEventRepository extends JpaRepository<AddToCartEvent, Long> {
    long count();
    void deleteByProductId(Long productId);
}
