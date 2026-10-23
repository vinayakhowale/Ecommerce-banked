package com.socommerce.app.controller;

import com.socommerce.app.dto.InfluencerDto;
import com.socommerce.app.service.InfluencerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/influencers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminInfluencerController {

    private final InfluencerService influencerService;

    @GetMapping
    public ResponseEntity<Page<InfluencerDto.Response>> list(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(influencerService.list(PageRequest.of(page, size)).map(InfluencerDto.Response::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InfluencerDto.Response> get(@PathVariable Long id) {
        return ResponseEntity.ok(InfluencerDto.Response.from(influencerService.getAny(id)));
    }

    @PostMapping
    public ResponseEntity<InfluencerDto.Response> create(@Valid @RequestBody InfluencerDto.Request request) {
        return ResponseEntity.ok(InfluencerDto.Response.from(influencerService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InfluencerDto.Response> update(@PathVariable Long id, @Valid @RequestBody InfluencerDto.Request request) {
        return ResponseEntity.ok(InfluencerDto.Response.from(influencerService.update(id, request)));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long id) {
        influencerService.setActive(id, true);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        influencerService.setActive(id, false);
        return ResponseEntity.noContent().build();
    }

    /** Dedicated "Influencer Sales Management" action: sets the official manually-maintained sales count. */
    @PatchMapping("/{id}/sales-count")
    public ResponseEntity<InfluencerDto.Response> setSalesCount(@PathVariable Long id,
                                                                 @Valid @RequestBody InfluencerDto.SalesCountRequest request) {
        return ResponseEntity.ok(InfluencerDto.Response.from(influencerService.setSalesCount(id, request.salesCount())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        influencerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
