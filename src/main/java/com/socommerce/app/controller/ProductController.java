package com.socommerce.app.controller;

import com.socommerce.app.dto.PostDto;
import com.socommerce.app.dto.ProductDto;
import com.socommerce.app.entity.Product;
import com.socommerce.app.service.DiscountService;
import com.socommerce.app.service.PostService;
import com.socommerce.app.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final PostService postService;
    private final DiscountService discountService;

    @GetMapping
    public ResponseEntity<Page<ProductDto.Response>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category) {
        Page<Product> products = category != null && !category.isBlank()
                ? productService.listByCategory(category, PageRequest.of(page, size))
                : productService.listActive(PageRequest.of(page, size));
        return ResponseEntity.ok(products.map(p -> ProductDto.Response.from(p, discountService.computeEffectivePrice(p))));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductDto.Response>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(productService.search(q, PageRequest.of(page, size))
                .map(p -> ProductDto.Response.from(p, discountService.computeEffectivePrice(p))));
    }

    /** Real "New Launches": products actually added within the configured recent window, not just "the last N added". */
    @GetMapping("/new-launches")
    public ResponseEntity<Page<ProductDto.Response>> newLaunches(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(productService.listNewLaunches(PageRequest.of(page, size))
                .map(p -> ProductDto.Response.from(p, discountService.computeEffectivePrice(p))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto.Response> get(@PathVariable Long id) {
        Product product = productService.getActiveOrThrow(id);
        return ResponseEntity.ok(ProductDto.Response.from(product, discountService.computeEffectivePrice(product)));
    }

    @GetMapping("/{id}/posts")
    public ResponseEntity<java.util.List<PostDto.Response>> postsForProduct(@PathVariable Long id) {
        return ResponseEntity.ok(postService.postsForProduct(id).stream().map(PostDto.Response::from).toList());
    }
}
