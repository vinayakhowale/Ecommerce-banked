package com.socommerce.app.repository;

import com.socommerce.app.entity.Post;
import com.socommerce.app.entity.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByStatusOrderByCreatedAtDesc(PostStatus status, Pageable pageable);
    Page<Post> findByStatus(PostStatus status, Pageable pageable);

    @Query("select p from Post p where p.status = 'PUBLISHED' and " +
           "(lower(p.caption) like lower(concat('%', :q, '%')) or lower(p.influencerName) like lower(concat('%', :q, '%')) " +
           "or lower(p.hashtags) like lower(concat('%', :q, '%')))")
    Page<Post> search(String q, Pageable pageable);

    @Query("select p from Post p order by p.viewCount desc")
    Page<Post> findMostViewed(Pageable pageable);

    @Query("select distinct pp.post from PostProduct pp where pp.product.id = :productId and pp.post.status = 'PUBLISHED'")
    java.util.List<Post> findPublishedPostsByProductId(Long productId);
}
