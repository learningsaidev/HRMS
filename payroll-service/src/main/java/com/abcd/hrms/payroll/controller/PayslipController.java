package com.abcd.hrms.payroll.controller;

import com.abcd.hrms.common.core.context.TenantContext;
import com.abcd.hrms.common.core.dto.ApiResponse;
import com.abcd.hrms.payroll.dto.PayslipDto;
import com.abcd.hrms.payroll.service.PayrollRunService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payroll/payslips")
@RequiredArgsConstructor
public class PayslipController {

    private final PayrollRunService payrollRunService;

    @GetMapping("/my-payslips")
    public ResponseEntity<ApiResponse<List<PayslipDto.Response>>> getMyPayslips(
            @RequestParam(required = false) UUID companyId) {
        UUID employeeId = TenantContext.getUserId();
        return ResponseEntity.ok(ApiResponse.ok(payrollRunService.getPayslipsForEmployee(employeeId, companyId)));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<PayslipDto.Response>>> getEmployeePayslips(
            @PathVariable UUID employeeId,
            @RequestParam(required = false) UUID companyId) {
        return ResponseEntity.ok(ApiResponse.ok(payrollRunService.getPayslipsForEmployee(employeeId, companyId)));
    }
}
