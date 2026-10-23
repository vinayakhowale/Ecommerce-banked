package com.socommerce.app.service;

import com.socommerce.app.dto.BannerDto;
import com.socommerce.app.entity.Banner;
import com.socommerce.app.entity.BannerPlacement;
import com.socommerce.app.exception.BadRequestException;
import com.socommerce.app.exception.ResourceNotFoundException;
import com.socommerce.app.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerRepository bannerRepository;

    @Transactional(readOnly = true)
    public List<Banner> activeFor(BannerPlacement placement) {
        return bannerRepository.findByPlacementAndActiveTrueOrderBySortOrderAscIdAsc(placement);
    }

    @Transactional(readOnly = true)
    public List<Banner> listAll() {
        return bannerRepository.findAllByOrderByPlacementAscSortOrderAscIdAsc();
    }

    @Transactional
    public Banner create(BannerDto.Request req) {
        Banner banner = new Banner();
        apply(banner, req);
        return bannerRepository.save(banner);
    }

    @Transactional
    public Banner update(Long id, BannerDto.Request req) {
        Banner banner = getAny(id);
        apply(banner, req);
        return bannerRepository.save(banner);
    }

    @Transactional
    public void setActive(Long id, boolean active) {
        Banner banner = getAny(id);
        banner.setActive(active);
        bannerRepository.save(banner);
    }

    @Transactional
    public void delete(Long id) {
        bannerRepository.delete(getAny(id));
    }

    private Banner getAny(Long id) {
        return bannerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Banner not found"));
    }

    private void apply(Banner banner, BannerDto.Request req) {
        String link = trimToNull(req.linkUrl());
        // Only in-store paths or http(s) links; this blocks javascript: and similar schemes.
        if (link != null && !(link.startsWith("/") || link.startsWith("https://") || link.startsWith("http://"))) {
            throw new BadRequestException("The banner link must start with / (a page in your store) or https://");
        }
        banner.setPlacement(req.placement());
        banner.setImageUrl(req.imageUrl().trim());
        banner.setMobileImageUrl(trimToNull(req.mobileImageUrl()));
        banner.setTitle(trimToNull(req.title()));
        banner.setSubtitle(trimToNull(req.subtitle()));
        banner.setCtaLabel(trimToNull(req.ctaLabel()));
        banner.setLinkUrl(link);
        banner.setSortOrder(req.sortOrder() == null ? 0 : req.sortOrder());
        banner.setActive(req.active() == null || req.active());
    }

    private String trimToNull(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }
}
