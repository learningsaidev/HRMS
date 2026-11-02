package com.abcd.hrms.taxbenefits.controller;

import com.abcd.hrms.common.core.dto.ApiResponse;
import com.abcd.hrms.taxbenefits.dto.LoanMasterDto;
import com.abcd.hrms.taxbenefits.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tax/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping
    public ResponseEntity<ApiResponse<LoanMasterDto.Response>> applyLoan(
            @Valid @RequestBody LoanMasterDto.ApplyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(loanService.applyLoan(request)));
    }

    @PostMapping("/{loanId}/disburse")
    public ResponseEntity<ApiResponse<LoanMasterDto.Response>> disburseLoan(
            @PathVariable UUID loanId,
            @Valid @RequestBody LoanMasterDto.DisburseRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(loanService.disburseLoan(loanId, request)));
    }

    @GetMapping("/{loanId}")
    public ResponseEntity<ApiResponse<LoanMasterDto.Response>> getLoanById(@PathVariable UUID loanId) {
        return ResponseEntity.ok(ApiResponse.ok(loanService.getLoanById(loanId)));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<LoanMasterDto.Response>>> listLoansForEmployee(
            @PathVariable UUID employeeId,
            @RequestParam(required = false) UUID companyId) {
        return ResponseEntity.ok(ApiResponse.ok(loanService.listLoansForEmployee(employeeId, companyId)));
    }
}

