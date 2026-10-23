package com.socommerce.app.repository;

import com.socommerce.app.entity.Banner;
import com.socommerce.app.entity.BannerPlacement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {
    List<Banner> findByPlacementAndActiveTrueOrderBySortOrderAscIdAsc(BannerPlacement placement);
    List<Banner> findAllByOrderByPlacementAscSortOrderAscIdAsc();
}
