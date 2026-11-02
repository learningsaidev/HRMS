package com.abcd.hrms.timeleave.controller;

import com.abcd.hrms.common.core.dto.ApiResponse;
import com.abcd.hrms.timeleave.dto.LeaveApplicationDto;
import com.abcd.hrms.timeleave.dto.LeaveApplicationRequest;
import com.abcd.hrms.timeleave.dto.LeaveBalanceDto;
import com.abcd.hrms.timeleave.service.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/leaves")
@RequiredArgsConstructor
@Tag(name = "Leave Management", description = "Leave applications, manager approvals, and immutable leave ledger balances")
@SecurityRequirement(name = "bearerAuth")
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping("/apply")
    @PreAuthorize("hasRole('ROLE_EMPLOYEE') or hasRole('ROLE_MANAGER') or hasRole('ROLE_HR_ADMIN')")
    @Operation(summary = "Submit a new leave application with overlap and balance validation")
    public ResponseEntity<ApiResponse<LeaveApplicationDto>> applyLeave(@Valid @RequestBody LeaveApplicationRequest request) {
        LeaveApplicationDto dto = leaveService.applyLeave(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(dto));
    }

    @PatchMapping("/{applicationId}/approve")
    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_HR_ADMIN')")
    @Operation(summary = "Approve leave application and debit from immutable leave ledger")
    public ResponseEntity<ApiResponse<LeaveApplicationDto>> approveLeave(
            @PathVariable UUID applicationId,
            @RequestParam(required = false) UUID approverId) {
        LeaveApplicationDto dto = leaveService.approveLeave(applicationId, approverId);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @GetMapping("/balances/{employeeId}")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_MANAGER', 'ROLE_HR_ADMIN', 'ROLE_PAYROLL_MAKER')")
    @Operation(summary = "Get live leave balances from immutable leave transaction ledger")
    public ResponseEntity<ApiResponse<List<LeaveBalanceDto>>> getBalances(@PathVariable UUID employeeId) {
        List<LeaveBalanceDto> balances = leaveService.getEmployeeBalances(employeeId);
        return ResponseEntity.ok(ApiResponse.ok(balances));
    }
}
