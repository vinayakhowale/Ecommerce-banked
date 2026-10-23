package com.socommerce.app.controller;

import com.socommerce.app.dto.BannerDto;
import com.socommerce.app.dto.CategoryDto;
import com.socommerce.app.dto.PostDto;
import com.socommerce.app.entity.BannerPlacement;
import com.socommerce.app.service.BannerService;
import com.socommerce.app.service.CategoryService;
import com.socommerce.app.service.DiscountService;
import com.socommerce.app.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Public storefront content: banners, the Shop menu, and the creator video rail. */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class StorefrontController {

    private final BannerService bannerService;
    private final CategoryService categoryService;
    private final PostService postService;
    private final DiscountService discountService;

    @GetMapping("/banners")
    public ResponseEntity<List<BannerDto.Response>> banners(@RequestParam(defaultValue = "HOME") BannerPlacement placement) {
        return ResponseEntity.ok(bannerService.activeFor(placement).stream().map(BannerDto.Response::from).toList());
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDto>> categories() {
        return ResponseEntity.ok(categoryService.activeCategories());
    }

    /** Published creator videos with the products they promote, newest first. */
    @GetMapping("/videos")
    public ResponseEntity<Page<PostDto.Response>> videos(@RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(postService.publishedVideos(PageRequest.of(page, Math.min(size, 30)))
                .map(p -> PostDto.Response.from(p, discountService)));
    }
}
