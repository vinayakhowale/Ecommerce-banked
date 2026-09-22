package com.socommerce.app.controller;

import com.socommerce.app.dto.InfluencerDiscountDto;
import com.socommerce.app.entity.InfluencerDiscount;
import com.socommerce.app.service.InfluencerDiscountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/influencer-discounts")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminInfluencerDiscountController {

    private final InfluencerDiscountService influencerDiscountService;

    @GetMapping
    public ResponseEntity<List<InfluencerDiscountDto.Response>> list() {
        List<InfluencerDiscount> discounts = influencerDiscountService.list();
        List<InfluencerDiscountDto.Response> response = discounts.stream()
                .map(d -> InfluencerDiscountDto.Response.from(d, influencerDiscountService.monthlySalesCountFor(d.getInfluencer().getId())))
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<InfluencerDiscountDto.Response> upsert(@Valid @RequestBody InfluencerDiscountDto.Request request) {
        InfluencerDiscount saved = influencerDiscountService.upsert(request);
        long monthlySales = influencerDiscountService.monthlySalesCountFor(saved.getInfluencer().getId());
        return ResponseEntity.ok(InfluencerDiscountDto.Response.from(saved, monthlySales));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        influencerDiscountService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
