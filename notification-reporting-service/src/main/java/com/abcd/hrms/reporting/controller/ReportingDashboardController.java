package com.abcd.hrms.reporting.controller;

import com.abcd.hrms.common.core.dto.ApiResponse;
import com.abcd.hrms.reporting.dto.ExecutiveDashboardDto;
import com.abcd.hrms.reporting.service.ExecutiveDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reporting")
@RequiredArgsConstructor
public class ReportingDashboardController {

    private final ExecutiveDashboardService dashboardService;

    @GetMapping("/dashboard/executive")
    public ResponseEntity<ApiResponse<ExecutiveDashboardDto>> getExecutiveDashboard(
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) String period) {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getExecutiveDashboard(companyId, period)));
    }
}
