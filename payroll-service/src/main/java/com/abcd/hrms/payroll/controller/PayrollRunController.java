package com.abcd.hrms.payroll.controller;

import com.abcd.hrms.common.core.dto.ApiResponse;
import com.abcd.hrms.payroll.dto.PayrollRunDto;
import com.abcd.hrms.payroll.dto.PayslipDto;
import com.abcd.hrms.payroll.service.PayrollRunService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payroll/runs")
@RequiredArgsConstructor
public class PayrollRunController {

    private final PayrollRunService payrollRunService;

    @PostMapping
    public ResponseEntity<ApiResponse<PayrollRunDto.Response>> initiateRun(
            @Valid @RequestBody PayrollRunDto.InitiateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(payrollRunService.initiateRun(request)));
    }

    @PostMapping("/{runId}/calculate")
    public ResponseEntity<ApiResponse<PayrollRunDto.Response>> calculateRun(@PathVariable UUID runId) {
        return ResponseEntity.ok(ApiResponse.ok(payrollRunService.calculateRun(runId)));
    }

    @PostMapping("/{runId}/review")
    public ResponseEntity<ApiResponse<PayrollRunDto.Response>> reviewRun(@PathVariable UUID runId) {
        return ResponseEntity.ok(ApiResponse.ok(payrollRunService.reviewRun(runId)));
    }

    @PostMapping("/{runId}/lock")
    public ResponseEntity<ApiResponse<PayrollRunDto.Response>> lockRun(@PathVariable UUID runId) {
        return ResponseEntity.ok(ApiResponse.ok(payrollRunService.lockRun(runId)));
    }

    @GetMapping("/{runId}")
    public ResponseEntity<ApiResponse<PayrollRunDto.Response>> getRunById(@PathVariable UUID runId) {
        return ResponseEntity.ok(ApiResponse.ok(payrollRunService.getRunById(runId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PayrollRunDto.Response>>> listRuns(
            @RequestParam(required = false) UUID companyId) {
        return ResponseEntity.ok(ApiResponse.ok(payrollRunService.listRuns(companyId)));
    }

    @GetMapping("/{runId}/payslips")
    public ResponseEntity<ApiResponse<List<PayslipDto.Response>>> getPayslipsByRun(@PathVariable UUID runId) {
        return ResponseEntity.ok(ApiResponse.ok(payrollRunService.getPayslipsByRun(runId)));
    }
}

