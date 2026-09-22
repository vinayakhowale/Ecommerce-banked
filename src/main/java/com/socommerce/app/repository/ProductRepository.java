package com.socommerce.app.repository;

import com.socommerce.app.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByActiveTrue(Pageable pageable);
    Page<Product> findByActiveTrueAndCategoryIgnoreCase(String category, Pageable pageable);

    @Query("select p from Product p where p.active = true and " +
           "(lower(p.name) like lower(concat('%', :q, '%')) or lower(p.brand) like lower(concat('%', :q, '%')) " +
           "or lower(p.category) like lower(concat('%', :q, '%')))")
    Page<Product> search(String q, Pageable pageable);

    /** Products actually added within a rolling window, newest first -- a real "New Launches" list, not just "the most recent N regardless of age". */
    Page<Product> findByActiveTrueAndCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime since, Pageable pageable);
}
