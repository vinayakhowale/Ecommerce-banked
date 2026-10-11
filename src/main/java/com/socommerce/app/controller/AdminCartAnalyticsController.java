package com.socommerce.app.controller;

import com.socommerce.app.dto.CartAnalyticsDto;
import com.socommerce.app.service.CartAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/cart-analytics")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminCartAnalyticsController {

    private final CartAnalyticsService cartAnalyticsService;

    @GetMapping
    public ResponseEntity<Page<CartAnalyticsDto.Row>> list(@RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(cartAnalyticsService.listAll(PageRequest.of(page, size)));
    }
}
