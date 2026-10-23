package com.socommerce.app.controller;

import com.socommerce.app.dto.OrderTrackingDto;
import com.socommerce.app.security.UserPrincipal;
import com.socommerce.app.service.OrderTrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderTrackingController {

    private final OrderTrackingService trackingService;

    /** Public: anyone with the order number and matching email can see where it is. */
    @PostMapping("/api/public/track-order")
    public ResponseEntity<OrderTrackingDto.Response> track(@Valid @RequestBody OrderTrackingDto.TrackRequest request) {
        return ResponseEntity.ok(trackingService.track(request));
    }

    /** The signed-in customer's own paid orders, newest first. */
    @GetMapping("/api/orders/mine")
    public ResponseEntity<List<OrderTrackingDto.Response>> mine(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(trackingService.mine(principal.getId()));
    }
}
