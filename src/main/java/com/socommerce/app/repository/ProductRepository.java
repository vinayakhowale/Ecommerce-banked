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

    /** Rows of [category, active product count], biggest first. Products without a category are skipped. */
    @Query("select p.category, count(p) from Product p where p.active = true and p.category is not null " +
           "and p.category <> '' group by p.category order by count(p) desc")
    java.util.List<Object[]> countActiveByCategory();

    /** Newest products in a category, used to pick a cover image for the Shop menu. */
    java.util.List<Product> findTop5ByActiveTrueAndCategoryIgnoreCaseOrderByCreatedAtDesc(String category);
}
