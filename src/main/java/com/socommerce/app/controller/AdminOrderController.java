package com.socommerce.app.controller;

import com.socommerce.app.dto.OrderTrackingDto;
import com.socommerce.app.entity.FulfillmentStatus;
import com.socommerce.app.service.OrderTrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminOrderController {

    private final OrderTrackingService trackingService;

    @GetMapping
    public ResponseEntity<Page<OrderTrackingDto.Response>> list(@RequestParam(required = false) FulfillmentStatus status,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "30") int size) {
        return ResponseEntity.ok(trackingService.adminList(status, PageRequest.of(page, size)));
    }

    @PatchMapping("/{id}/fulfillment")
    public ResponseEntity<OrderTrackingDto.Response> update(@PathVariable Long id,
                                                            @Valid @RequestBody OrderTrackingDto.FulfillmentUpdate request) {
        return ResponseEntity.ok(trackingService.updateFulfillment(id, request));
    }
}
