package com.socommerce.app.repository;

import com.socommerce.app.entity.PaymentOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {
    Optional<PaymentOrder> findByRazorpayOrderId(String razorpayOrderId);
    Page<PaymentOrder> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Detach rather than delete: a PaymentOrder is a real financial record and should survive
    // the catalog product/influencer it referenced being removed later.
    @Modifying
    @Query("update PaymentOrder p set p.product = null where p.product.id = :productId")
    void detachFromProduct(@Param("productId") Long productId);

    @Modifying
    @Query("update PaymentOrder p set p.influencer = null where p.influencer.id = :influencerId")
    void detachFromInfluencer(@Param("influencerId") Long influencerId);
}
