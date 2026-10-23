package com.socommerce.app.controller;

import com.socommerce.app.dto.PostDto;
import com.socommerce.app.entity.Influencer;
import com.socommerce.app.security.UserPrincipal;
import com.socommerce.app.service.CreatorService;
import com.socommerce.app.service.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/** Creator studio: signed-in influencers upload videos promoting products. */
@RestController
@RequestMapping("/api/creator")
@RequiredArgsConstructor
public class CreatorController {

    private final CreatorService creatorService;
    private final FileStorageService fileStorageService;

    public record CreatorProfile(Long influencerId, String name, String code, String profileImageUrl, int salesCount) {}

    /** 200 with the creator profile, or 403 if this account isn't linked to an influencer. */
    @GetMapping("/me")
    public ResponseEntity<CreatorProfile> me(@AuthenticationPrincipal UserPrincipal principal) {
        Influencer i = creatorService.requireCreator(principal.getId());
        return ResponseEntity.ok(new CreatorProfile(i.getId(), i.getName(), i.getCode(), i.getProfileImageUrl(), i.getSalesCount()));
    }

    @GetMapping("/videos")
    public ResponseEntity<List<PostDto.Response>> myVideos(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(creatorService.myVideos(principal.getId()).stream().map(PostDto.Response::from).toList());
    }

    @PostMapping("/videos")
    public ResponseEntity<PostDto.Response> submit(@AuthenticationPrincipal UserPrincipal principal,
                                                   @Valid @RequestBody PostDto.CreatorVideoRequest request) {
        return ResponseEntity.ok(PostDto.Response.from(creatorService.submit(principal.getId(), request)));
    }

    @DeleteMapping("/videos/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        creatorService.delete(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }

    /** type=VIDEO for the clip itself, type=IMAGE for an optional cover picture. */
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, String>> upload(@AuthenticationPrincipal UserPrincipal principal,
                                                      @RequestParam("file") MultipartFile file,
                                                      @RequestParam(value = "type", defaultValue = "VIDEO") String type) {
        creatorService.requireCreator(principal.getId());
        String url = "IMAGE".equalsIgnoreCase(type) ? fileStorageService.storeImage(file) : fileStorageService.storeVideo(file);
        return ResponseEntity.ok(Map.of("url", url));
    }
}
