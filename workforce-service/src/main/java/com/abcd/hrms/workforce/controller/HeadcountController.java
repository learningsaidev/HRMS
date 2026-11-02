package com.abcd.hrms.workforce.controller;

import com.abcd.hrms.common.core.dto.ApiResponse;
import com.abcd.hrms.workforce.dto.HeadcountSummaryDto;
import com.abcd.hrms.workforce.service.HeadcountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/headcount")
@RequiredArgsConstructor
@Tag(name = "Headcount Analytics", description = "Headcount summaries, distribution by entity, department, and location")
@SecurityRequirement(name = "bearerAuth")
public class HeadcountController {

    private final HeadcountService headcountService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ROLE_HR_ADMIN', 'ROLE_MANAGER', 'ROLE_PAYROLL_MAKER', 'ROLE_FINANCE_DIRECTOR', 'ROLE_SYSTEM_ADMIN')")
    @Operation(summary = "Get headcount summary by status, legal entity, department, location and employment type")
    public ResponseEntity<ApiResponse<HeadcountSummaryDto>> getHeadcountSummary() {
        HeadcountSummaryDto summary = headcountService.getHeadcountSummary();
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }
}
