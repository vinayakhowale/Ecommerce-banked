package com.socommerce.app.controller;

import com.socommerce.app.dto.ReportDtos;
import com.socommerce.app.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminReportController {

    private final ReportService reportService;

    @GetMapping("/summary")
    public ResponseEntity<ReportDtos.ReportSummary> summary() {
        return ResponseEntity.ok(reportService.getSummary());
    }
}
