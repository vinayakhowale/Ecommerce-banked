package com.socommerce.app.controller;

import com.socommerce.app.dto.BuyNowDto;
import com.socommerce.app.security.UserPrincipal;
import com.socommerce.app.service.BuyNowService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BuyNowController {

    private final BuyNowService buyNowService;

    /**
     * The single authoritative endpoint for "Buy Now". The frontend sends only a productId
     * (and optional postId for attribution) — it can never dictate the redirect target.
     * Works for both authenticated and anonymous users (guest browsing/discovery).
     */
    @PostMapping("/buy-now")
    public ResponseEntity<BuyNowDto.Response> buyNow(@AuthenticationPrincipal UserPrincipal principal,
                                                       @Valid @RequestBody BuyNowDto.Request request,
                                                       HttpServletRequest httpRequest) {
        Long userId = principal != null ? principal.getId() : null;
        String ip = httpRequest.getRemoteAddr();
        String ua = httpRequest.getHeader("User-Agent");
        return ResponseEntity.ok(buyNowService.buyNow(request, userId, ip, ua));
    }
}
