package com.socommerce.app.repository;

import com.socommerce.app.entity.InfluencerDiscount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InfluencerDiscountRepository extends JpaRepository<InfluencerDiscount, Long> {
    Optional<InfluencerDiscount> findByInfluencerId(Long influencerId);
    void deleteByInfluencerId(Long influencerId);
}
