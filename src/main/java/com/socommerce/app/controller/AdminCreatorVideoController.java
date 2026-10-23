package com.socommerce.app.controller;

import com.socommerce.app.dto.PostDto;
import com.socommerce.app.service.CreatorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Approval queue for videos creators uploaded themselves. */
@RestController
@RequestMapping("/api/admin/creator-videos")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminCreatorVideoController {

    private final CreatorService creatorService;

    @GetMapping("/pending")
    public ResponseEntity<Page<PostDto.Response>> pending(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "30") int size) {
        return ResponseEntity.ok(creatorService.pending(PageRequest.of(page, size)).map(PostDto.Response::from));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<PostDto.Response> approve(@PathVariable Long id) {
        return ResponseEntity.ok(PostDto.Response.from(creatorService.approve(id)));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<PostDto.Response> reject(@PathVariable Long id, @Valid @RequestBody(required = false) PostDto.ReviewDecision body) {
        return ResponseEntity.ok(PostDto.Response.from(creatorService.reject(id, body == null ? null : body.note())));
    }
}
