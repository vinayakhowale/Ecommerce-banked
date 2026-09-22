package com.socommerce.app.service;

import com.socommerce.app.dto.CartAnalyticsDto;
import com.socommerce.app.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin-facing visibility into every user's live cart contents -- who added what, when, and how
 * long it's been sitting there. Built directly from CartItem (no separate event log needed):
 * a row exists for exactly as long as the product remains in that user's cart, which is exactly
 * the "currently in cart" duration this feature is meant to show.
 */
@Service
@RequiredArgsConstructor
public class CartAnalyticsService {

    private final CartItemRepository cartItemRepository;

    @Transactional(readOnly = true)
    public Page<CartAnalyticsDto.Row> listAll(Pageable pageable) {
        return cartItemRepository.findAllByOrderByCreatedAtAsc(pageable).map(CartAnalyticsDto.Row::from);
    }
}
