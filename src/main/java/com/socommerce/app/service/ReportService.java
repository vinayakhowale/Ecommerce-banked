package com.socommerce.app.service;

import com.socommerce.app.dto.ReportDtos;
import com.socommerce.app.repository.InfluencerSaleRepository;
import com.socommerce.app.repository.ProductClickRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final InfluencerSaleRepository influencerSaleRepository;
    private final ProductClickRepository productClickRepository;

    @Transactional(readOnly = true)
    public ReportDtos.ReportSummary getSummary() {
        // Click counts per influencer, to blend into the influencer-wise sales rows and to
        // report code usage even for influencers who haven't had a confirmed sale recorded yet.
        Map<Long, Long> clicksByInfluencer = new HashMap<>();
        for (Object[] row : productClickRepository.codeUsageByInfluencer()) {
            Long influencerId = (Long) row[0];
            Long clicks = (Long) row[1];
            clicksByInfluencer.put(influencerId, clicks);
        }

        List<ReportDtos.InfluencerSalesRow> influencerRows = influencerSaleRepository.influencerWiseSales().stream()
                .map(row -> new ReportDtos.InfluencerSalesRow(
                        (Long) row[0],
                        (String) row[1],
                        (String) row[2],
                        (Long) row[3],
                        (Long) row[4],
                        (BigDecimal) row[5],
                        clicksByInfluencer.getOrDefault((Long) row[0], 0L)
                ))
                .toList();

        List<ReportDtos.ProductSalesRow> productRows = influencerSaleRepository.productWiseSales().stream()
                .map(row -> new ReportDtos.ProductSalesRow(
                        (Long) row[0], (String) row[1], (Long) row[2], (Long) row[3], (BigDecimal) row[4]
                ))
                .toList();

        List<ReportDtos.CampaignSalesRow> campaignRows = influencerSaleRepository.campaignWiseSales().stream()
                .map(row -> new ReportDtos.CampaignSalesRow(
                        (Long) row[0], (String) row[1], (Long) row[2], (BigDecimal) row[3]
                ))
                .toList();

        // Code usage: every influencer with either clicks or confirmed orders, not just those with sales.
        Map<Long, long[]> ordersByInfluencer = new HashMap<>(); // [orderCount]
        for (ReportDtos.InfluencerSalesRow r : influencerRows) {
            ordersByInfluencer.put(r.influencerId(), new long[]{r.orderCount()});
        }
        List<ReportDtos.CodeUsageRow> codeUsageRows = productClickRepository.codeUsageByInfluencer().stream()
                .map(row -> {
                    Long influencerId = (Long) row[0];
                    String code = (String) row[1];
                    Long clicks = (Long) row[2];
                    long orders = ordersByInfluencer.containsKey(influencerId) ? ordersByInfluencer.get(influencerId)[0] : 0L;
                    return new ReportDtos.CodeUsageRow(influencerId, code, clicks, orders);
                })
                .toList();

        return new ReportDtos.ReportSummary(influencerRows, productRows, campaignRows, codeUsageRows);
    }
}
