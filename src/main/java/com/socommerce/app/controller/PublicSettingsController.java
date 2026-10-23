package com.socommerce.app.controller;

import com.socommerce.app.dto.StoreSettingsDto;
import com.socommerce.app.service.StoreSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Branding the storefront needs before anyone signs in, so this stays public. */
@RestController
@RequestMapping("/api/public/settings")
@RequiredArgsConstructor
public class PublicSettingsController {

    private final StoreSettingsService settingsService;

    @GetMapping
    public ResponseEntity<StoreSettingsDto.Response> get() {
        return ResponseEntity.ok(StoreSettingsDto.Response.from(settingsService.get()));
    }
}
