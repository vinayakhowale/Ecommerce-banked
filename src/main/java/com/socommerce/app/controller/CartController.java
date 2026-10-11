package com.socommerce.app.controller;

import com.socommerce.app.dto.CartDto;
import com.socommerce.app.security.UserPrincipal;
import com.socommerce.app.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartDto.CartResponse> getCart(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(cartService.getCart(principal.getId()));
    }

    @PostMapping("/items")
    public ResponseEntity<CartDto.CartResponse> addItem(@AuthenticationPrincipal UserPrincipal principal,
                                                          @Valid @RequestBody CartDto.AddRequest request) {
        return ResponseEntity.ok(cartService.addItem(principal.getId(), request));
    }

    @PatchMapping("/items/{itemId}")
    public ResponseEntity<CartDto.CartResponse> updateQuantity(@AuthenticationPrincipal UserPrincipal principal,
                                                                @PathVariable Long itemId,
                                                                @Valid @RequestBody CartDto.UpdateQuantityRequest request) {
        return ResponseEntity.ok(cartService.updateQuantity(principal.getId(), itemId, request.quantity()));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartDto.CartResponse> removeItem(@AuthenticationPrincipal UserPrincipal principal,
                                                             @PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItem(principal.getId(), itemId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserPrincipal principal) {
        cartService.clearCart(principal.getId());
        return ResponseEntity.noContent().build();
    }
}
