package com.socommerce.app.service;

import com.socommerce.app.dto.CategoryDto;
import com.socommerce.app.entity.Product;
import com.socommerce.app.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Categories come from the products themselves, so a new category appears in the Shop menu as
 * soon as the admin saves a product in it, with that product's photo as the cover.
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<CategoryDto> activeCategories() {
        List<CategoryDto> out = new ArrayList<>();
        for (Object[] row : productRepository.countActiveByCategory()) {
            String name = (String) row[0];
            long count = ((Number) row[1]).longValue();
            out.add(new CategoryDto(name, count, coverImage(name)));
        }
        return out;
    }

    private String coverImage(String category) {
        for (Product p : productRepository.findTop5ByActiveTrueAndCategoryIgnoreCaseOrderByCreatedAtDesc(category)) {
            if (p.getImageUrls() != null && !p.getImageUrls().isEmpty()) return p.getImageUrls().get(0);
            if (p.getProductImageUrl() != null && !p.getProductImageUrl().isBlank()) return p.getProductImageUrl();
        }
        return null;
    }
}
