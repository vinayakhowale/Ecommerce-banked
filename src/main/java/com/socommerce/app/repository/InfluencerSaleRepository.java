package com.socommerce.app.repository;

import com.socommerce.app.entity.InfluencerSale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface InfluencerSaleRepository extends JpaRepository<InfluencerSale, Long> {
    Page<InfluencerSale> findByInfluencerIdOrderByPurchaseDateDesc(Long influencerId, Pageable pageable);

    @Query("select count(s) from InfluencerSale s where s.influencer.id = :influencerId and s.purchaseDate >= :since")
    long countByInfluencerSince(Long influencerId, LocalDateTime since);

    @Query("select coalesce(sum(s.quantity), 0) from InfluencerSale s where s.influencer.id = :influencerId and s.purchaseDate >= :since")
    long sumQuantityByInfluencerSince(Long influencerId, LocalDateTime since);

    @Query("select s.influencer.id as influencerId, s.influencer.name as influencerName, s.influencer.code as code, " +
           "count(s) as orderCount, coalesce(sum(s.quantity),0) as totalQuantity, coalesce(sum(s.orderValue),0) as totalRevenue " +
           "from InfluencerSale s group by s.influencer.id, s.influencer.name, s.influencer.code order by totalRevenue desc")
    List<Object[]> influencerWiseSales();

    @Query("select s.product.id as productId, s.product.name as productName, " +
           "count(s) as orderCount, coalesce(sum(s.quantity),0) as totalQuantity, coalesce(sum(s.orderValue),0) as totalRevenue " +
           "from InfluencerSale s group by s.product.id, s.product.name order by totalRevenue desc")
    List<Object[]> productWiseSales();

    @Query("select s.discount.id as discountId, s.discount.name as discountName, " +
           "count(s) as orderCount, coalesce(sum(s.orderValue),0) as totalRevenue " +
           "from InfluencerSale s where s.discount is not null group by s.discount.id, s.discount.name order by totalRevenue desc")
    List<Object[]> campaignWiseSales();

    List<InfluencerSale> findAllByOrderByPurchaseDateDesc();

    void deleteByProductId(Long productId);
    void deleteByInfluencerId(Long influencerId);

    @Modifying
    @Query("update InfluencerSale s set s.discount = null where s.discount.id = :discountId")
    void detachFromDiscount(@Param("discountId") Long discountId);
}
