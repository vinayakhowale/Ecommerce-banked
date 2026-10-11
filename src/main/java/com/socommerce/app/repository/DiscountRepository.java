package com.socommerce.app.repository;

import com.socommerce.app.entity.Discount;
import com.socommerce.app.entity.DiscountScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface DiscountRepository extends JpaRepository<Discount, Long> {
    Page<Discount> findByActiveTrue(Pageable pageable);

    @Query("select d from Discount d where d.active = true and d.scope = com.socommerce.app.entity.DiscountScope.PRODUCT " +
           "and d.product.id = :productId and d.startAt <= :now and d.endAt >= :now")
    List<Discount> findActiveForProduct(Long productId, LocalDateTime now);

    @Query("select d from Discount d where d.active = true and d.scope = com.socommerce.app.entity.DiscountScope.CATEGORY " +
           "and lower(d.category) = lower(:category) and d.startAt <= :now and d.endAt >= :now")
    List<Discount> findActiveForCategory(String category, LocalDateTime now);

    @Query("select d from Discount d where d.active = true and d.startAt <= :now and d.endAt >= :now")
    List<Discount> findAllCurrentlyActive(LocalDateTime now);

    void deleteByProductId(Long productId);
}
