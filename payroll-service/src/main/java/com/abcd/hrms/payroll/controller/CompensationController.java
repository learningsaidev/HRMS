package com.abcd.hrms.payroll.controller;

import com.abcd.hrms.common.core.dto.ApiResponse;
import com.abcd.hrms.payroll.dto.EmployeeCompensationDto;
import com.abcd.hrms.payroll.service.SalaryStructureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payroll/compensations")
@RequiredArgsConstructor
public class CompensationController {

    private final SalaryStructureService salaryStructureService;

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeCompensationDto.Response>> assignCompensation(
            @Valid @RequestBody EmployeeCompensationDto.AssignRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(salaryStructureService.assignCompensation(request)));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<EmployeeCompensationDto.Response>> getEmployeeCompensation(
            @PathVariable UUID employeeId,
            @RequestParam(required = false) UUID companyId) {
        return ResponseEntity.ok(ApiResponse.ok(salaryStructureService.getEmployeeCompensation(employeeId, companyId)));
    }
}
