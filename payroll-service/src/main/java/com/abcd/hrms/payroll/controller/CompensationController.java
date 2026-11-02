package com.abcd.hrms.payroll.controller;

import com.abcd.hrms.common.core.dto.ApiResponse;
import com.abcd.hrms.payroll.dto.EmployeeCompensationDto;
import com.abcd.hrms.payroll.dto.PayGroupDto;
import com.abcd.hrms.payroll.service.SalaryStructureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payroll/compensation")
@RequiredArgsConstructor
public class CompensationController {

    private final SalaryStructureService salaryStructureService;

    @PostMapping("/pay-groups")
    public ResponseEntity<ApiResponse<PayGroupDto.Response>> createPayGroup(
            @Valid @RequestBody PayGroupDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(salaryStructureService.createPayGroup(request)));
    }

    @GetMapping("/pay-groups")
    public ResponseEntity<ApiResponse<List<PayGroupDto.Response>>> listPayGroups(
            @RequestParam(required = false) UUID companyId) {
        return ResponseEntity.ok(ApiResponse.ok(salaryStructureService.listPayGroups(companyId)));
    }

    @PostMapping("/assignments")
    public ResponseEntity<ApiResponse<EmployeeCompensationDto.Response>> assignCompensation(
            @Valid @RequestBody EmployeeCompensationDto.AssignRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(salaryStructureService.assignCompensation(request)));
    }

    @GetMapping("/employees/{employeeId}")
    public ResponseEntity<ApiResponse<EmployeeCompensationDto.Response>> getCompensationByEmployee(
            @PathVariable UUID employeeId,
            @RequestParam(required = false) UUID companyId) {
        return ResponseEntity.ok(ApiResponse.ok(salaryStructureService.getCompensationByEmployee(employeeId, companyId)));
    }
}
