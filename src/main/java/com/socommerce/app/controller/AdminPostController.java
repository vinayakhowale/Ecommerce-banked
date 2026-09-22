package com.socommerce.app.controller;

import com.socommerce.app.dto.PostDto;
import com.socommerce.app.entity.PostStatus;
import com.socommerce.app.service.FileStorageService;
import com.socommerce.app.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/posts")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminPostController {

    private final PostService postService;
    private final FileStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<Page<PostDto.Response>> list(@RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(postService.adminList(PageRequest.of(page, size)).map(PostDto.Response::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDto.Response> get(@PathVariable Long id) {
        return ResponseEntity.ok(PostDto.Response.from(postService.getAny(id)));
    }

    @PostMapping
    public ResponseEntity<PostDto.Response> create(@Valid @RequestBody PostDto.Request request) {
        return ResponseEntity.ok(PostDto.Response.from(postService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostDto.Response> update(@PathVariable Long id, @Valid @RequestBody PostDto.Request request) {
        return ResponseEntity.ok(PostDto.Response.from(postService.update(id, request)));
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<PostDto.Response> publish(@PathVariable Long id) {
        return ResponseEntity.ok(PostDto.Response.from(postService.setStatus(id, PostStatus.PUBLISHED)));
    }

    @PatchMapping("/{id}/unpublish")
    public ResponseEntity<PostDto.Response> unpublish(@PathVariable Long id) {
        return ResponseEntity.ok(PostDto.Response.from(postService.setStatus(id, PostStatus.UNPUBLISHED)));
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<PostDto.Response> archive(@PathVariable Long id) {
        return ResponseEntity.ok(PostDto.Response.from(postService.setStatus(id, PostStatus.ARCHIVED)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        postService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/upload-media", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, String>> uploadMedia(@RequestParam("file") MultipartFile file,
                                                             @RequestParam("type") String type) {
        String url = "VIDEO".equalsIgnoreCase(type) ? fileStorageService.storeVideo(file) : fileStorageService.storeImage(file);
        return ResponseEntity.ok(Map.of("url", url));
    }
}
