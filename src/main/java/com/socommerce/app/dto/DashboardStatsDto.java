package com.socommerce.app.dto;

import java.util.List;

public record DashboardStatsDto(
        long totalProducts,
        long totalPosts,
        long totalUsers,
        long totalProductClicks,
        long totalAddToCartEvents,
        List<PostDto.Response> mostViewedPosts,
        List<ProductClickStat> mostClickedProducts,
        List<PostDto.Response> recentUploads
) {
    public record ProductClickStat(Long productId, String productName, long clicks) {}
}
