package com.socommerce.app.dto;

import java.math.BigDecimal;
import java.util.List;

public class ReportDtos {

    public record InfluencerSalesRow(
            Long influencerId, String influencerName, String code,
            long orderCount, long totalQuantity, BigDecimal totalRevenue, long codeClicks
    ) {}

    public record ProductSalesRow(
            Long productId, String productName,
            long orderCount, long totalQuantity, BigDecimal totalRevenue
    ) {}

    public record CampaignSalesRow(
            Long discountId, String discountName, long orderCount, BigDecimal totalRevenue
    ) {}

    public record CodeUsageRow(
            Long influencerId, String code, long clicks, long confirmedOrders
    ) {}

    public record ReportSummary(
            List<InfluencerSalesRow> influencerWiseSales,
            List<ProductSalesRow> productWiseSales,
            List<CampaignSalesRow> campaignWiseSales,
            List<CodeUsageRow> codeUsage
    ) {}
}
