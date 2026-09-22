package com.socommerce.app.controller;

import com.socommerce.app.dto.ProductDto;
import com.socommerce.app.service.FileStorageService;
import com.socommerce.app.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminProductController {

    private final ProductService productService;
    private final FileStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<Page<ProductDto.Response>> list(@RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(productService.adminList(PageRequest.of(page, size)).map(ProductDto.Response::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto.Response> get(@PathVariable Long id) {
        return ResponseEntity.ok(ProductDto.Response.from(productService.getAny(id)));
    }

    @PostMapping
    public ResponseEntity<ProductDto.Response> create(@Valid @RequestBody ProductDto.Request request) {
        return ResponseEntity.ok(ProductDto.Response.from(productService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto.Response> update(@PathVariable Long id, @Valid @RequestBody ProductDto.Request request) {
        return ResponseEntity.ok(ProductDto.Response.from(productService.update(id, request)));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        productService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long id) {
        productService.activate(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/upload-image", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(Map.of("url", fileStorageService.storeImage(file)));
    }
}
