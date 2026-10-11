package com.socommerce.app.repository;

import com.socommerce.app.entity.ProductClick;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductClickRepository extends JpaRepository<ProductClick, Long> {
    long countByProductId(Long productId);
    long countByInfluencerId(Long influencerId);

    @Query("select pc.product.id as productId, count(pc) as clicks from ProductClick pc group by pc.product.id order by count(pc) desc")
    List<Object[]> mostClickedProducts();

    @Query("select pc.influencer.id as influencerId, pc.influencer.code as code, count(pc) as clicks " +
           "from ProductClick pc where pc.influencer is not null group by pc.influencer.id, pc.influencer.code order by count(pc) desc")
    List<Object[]> codeUsageByInfluencer();

    void deleteByPostId(Long postId);
    void deleteByProductId(Long productId);
    void deleteByInfluencerId(Long influencerId);
}
