package com.abcd.hrms.workforce.controller;

import com.abcd.hrms.common.core.dto.ApiResponse;
import com.abcd.hrms.workforce.dto.OrgNodeDto;
import com.abcd.hrms.workforce.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organization")
@RequiredArgsConstructor
@Tag(name = "Organization Structure", description = "Org charts, reporting trees, and hierarchy")
@SecurityRequirement(name = "bearerAuth")
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping("/tree")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_MANAGER', 'ROLE_HR_ADMIN', 'ROLE_PAYROLL_MAKER', 'ROLE_FINANCE_DIRECTOR')")
    @Operation(summary = "Get organizational reporting hierarchy tree")
    public ResponseEntity<ApiResponse<List<OrgNodeDto>>> getOrganizationTree() {
        List<OrgNodeDto> tree = organizationService.getOrganizationTree();
        return ResponseEntity.ok(ApiResponse.ok(tree));
    }
}
