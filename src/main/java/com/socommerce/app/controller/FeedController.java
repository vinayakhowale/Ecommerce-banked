package com.socommerce.app.controller;

import com.socommerce.app.dto.PostDto;
import com.socommerce.app.entity.Post;
import com.socommerce.app.service.DiscountService;
import com.socommerce.app.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class FeedController {

    private final PostService postService;
    private final DiscountService discountService;

    @GetMapping("/feed")
    public ResponseEntity<Page<PostDto.Response>> feed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Post> posts = postService.feed(PageRequest.of(page, size));
        return ResponseEntity.ok(posts.map(p -> PostDto.Response.from(p, discountService)));
    }

    @GetMapping("/explore")
    public ResponseEntity<Page<PostDto.Response>> explore(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(postService.mostViewed(PageRequest.of(page, size))
                .map(p -> PostDto.Response.from(p, discountService)));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PostDto.Response>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(postService.search(q, PageRequest.of(page, size))
                .map(p -> PostDto.Response.from(p, discountService)));
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<PostDto.Response> get(@PathVariable Long id) {
        Post post = postService.getPublishedOrThrow(id);
        return ResponseEntity.ok(PostDto.Response.from(post, discountService));
    }

    @PostMapping("/posts/{id}/view")
    public ResponseEntity<Void> recordView(@PathVariable Long id) {
        postService.recordView(id);
        return ResponseEntity.noContent().build();
    }
}
