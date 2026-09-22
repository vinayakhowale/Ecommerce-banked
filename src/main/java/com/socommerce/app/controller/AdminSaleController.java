package com.socommerce.app.controller;

import com.socommerce.app.dto.InfluencerSaleDto;
import com.socommerce.app.service.InfluencerSaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/sales")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminSaleController {

    private final InfluencerSaleService influencerSaleService;

    @GetMapping
    public ResponseEntity<Page<InfluencerSaleDto.Response>> list(@RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(influencerSaleService.list(PageRequest.of(page, size)).map(InfluencerSaleDto.Response::from));
    }

    @GetMapping("/influencer/{influencerId}")
    public ResponseEntity<Page<InfluencerSaleDto.Response>> listForInfluencer(@PathVariable Long influencerId,
                                                                               @RequestParam(defaultValue = "0") int page,
                                                                               @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(influencerSaleService.listForInfluencer(influencerId, PageRequest.of(page, size))
                .map(InfluencerSaleDto.Response::from));
    }

    @PostMapping
    public ResponseEntity<InfluencerSaleDto.Response> record(@Valid @RequestBody InfluencerSaleDto.Request request) {
        return ResponseEntity.ok(InfluencerSaleDto.Response.from(influencerSaleService.record(request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        influencerSaleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
