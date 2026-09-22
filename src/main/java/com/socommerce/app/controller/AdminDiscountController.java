package com.socommerce.app.controller;

import com.socommerce.app.dto.DiscountDto;
import com.socommerce.app.service.DiscountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/discounts")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminDiscountController {

    private final DiscountService discountService;

    @GetMapping
    public ResponseEntity<Page<DiscountDto.Response>> list(@RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(discountService.list(PageRequest.of(page, size)).map(DiscountDto.Response::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiscountDto.Response> get(@PathVariable Long id) {
        return ResponseEntity.ok(DiscountDto.Response.from(discountService.getAny(id)));
    }

    @PostMapping
    public ResponseEntity<DiscountDto.Response> create(@Valid @RequestBody DiscountDto.Request request) {
        return ResponseEntity.ok(DiscountDto.Response.from(discountService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DiscountDto.Response> update(@PathVariable Long id, @Valid @RequestBody DiscountDto.Request request) {
        return ResponseEntity.ok(DiscountDto.Response.from(discountService.update(id, request)));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long id) {
        discountService.setActive(id, true);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        discountService.setActive(id, false);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        discountService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
