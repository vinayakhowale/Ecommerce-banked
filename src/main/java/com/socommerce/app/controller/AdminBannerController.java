package com.socommerce.app.controller;

import com.socommerce.app.dto.BannerDto;
import com.socommerce.app.service.BannerService;
import com.socommerce.app.service.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/banners")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminBannerController {

    private final BannerService bannerService;
    private final FileStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<List<BannerDto.Response>> list() {
        return ResponseEntity.ok(bannerService.listAll().stream().map(BannerDto.Response::from).toList());
    }

    @PostMapping
    public ResponseEntity<BannerDto.Response> create(@Valid @RequestBody BannerDto.Request request) {
        return ResponseEntity.ok(BannerDto.Response.from(bannerService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BannerDto.Response> update(@PathVariable Long id, @Valid @RequestBody BannerDto.Request request) {
        return ResponseEntity.ok(BannerDto.Response.from(bannerService.update(id, request)));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<Void> setActive(@PathVariable Long id, @RequestParam boolean value) {
        bannerService.setActive(id, value);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bannerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/upload-image", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(Map.of("url", fileStorageService.storeImage(file)));
    }
}
