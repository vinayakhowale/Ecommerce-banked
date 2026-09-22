package com.socommerce.app.service;

import com.socommerce.app.dto.DashboardStatsDto;
import com.socommerce.app.dto.PostDto;
import com.socommerce.app.entity.Post;
import com.socommerce.app.entity.PostStatus;
import com.socommerce.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProductRepository productRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ProductClickRepository productClickRepository;
    private final AddToCartEventRepository addToCartEventRepository;

    @Transactional(readOnly = true)
    public DashboardStatsDto getStats() {
        long totalProducts = productRepository.count();
        long totalPosts = postRepository.count();
        long totalUsers = userRepository.count();
        long totalClicks = productClickRepository.count();
        long totalAddToCart = addToCartEventRepository.count();

        List<PostDto.Response> mostViewed = postRepository.findMostViewed(PageRequest.of(0, 5))
                .stream().map(PostDto.Response::from).toList();

        List<Object[]> clickRows = productClickRepository.mostClickedProducts();
        List<DashboardStatsDto.ProductClickStat> mostClicked = clickRows.stream()
                .limit(5)
                .map(row -> {
                    Long productId = (Long) row[0];
                    Long clicks = (Long) row[1];
                    String name = productRepository.findById(productId).map(p -> p.getName()).orElse("Unknown");
                    return new DashboardStatsDto.ProductClickStat(productId, name, clicks);
                }).toList();

        List<PostDto.Response> recent = postRepository.findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream().map(PostDto.Response::from).toList();

        return new DashboardStatsDto(totalProducts, totalPosts, totalUsers, totalClicks, totalAddToCart,
                mostViewed, mostClicked, recent);
    }
}
