package com.socommerce.app.controller;

import com.socommerce.app.dto.StoreSettingsDto;
import com.socommerce.app.service.StoreSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminSettingsController {

    private final StoreSettingsService settingsService;

    @GetMapping
    public ResponseEntity<StoreSettingsDto.Response> get() {
        return ResponseEntity.ok(StoreSettingsDto.Response.from(settingsService.get()));
    }

    @PutMapping
    public ResponseEntity<StoreSettingsDto.Response> update(@Valid @RequestBody StoreSettingsDto.Request request) {
        return ResponseEntity.ok(StoreSettingsDto.Response.from(settingsService.update(request)));
    }
}
