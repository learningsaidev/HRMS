package com.abcd.hrms.payroll.controller;

import com.abcd.hrms.common.core.dto.ApiResponse;
import com.abcd.hrms.payroll.dto.PayGroupDto;
import com.abcd.hrms.payroll.dto.SalaryComponentDto;
import com.abcd.hrms.payroll.service.SalaryStructureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payroll")
@RequiredArgsConstructor
public class SalaryComponentController {

    private final SalaryStructureService salaryStructureService;

    @GetMapping("/components")
    public ResponseEntity<ApiResponse<List<SalaryComponentDto.Response>>> listComponents(
            @RequestParam(required = false) UUID companyId) {
        return ResponseEntity.ok(ApiResponse.ok(salaryStructureService.listSalaryComponents(companyId)));
    }

    @PostMapping("/components")
    public ResponseEntity<ApiResponse<SalaryComponentDto.Response>> createComponent(
            @Valid @RequestBody SalaryComponentDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(salaryStructureService.createSalaryComponent(request)));
    }

    @GetMapping("/pay-groups")
    public ResponseEntity<ApiResponse<List<PayGroupDto.Response>>> listPayGroups(
            @RequestParam(required = false) UUID companyId) {
        return ResponseEntity.ok(ApiResponse.ok(salaryStructureService.listPayGroups(companyId)));
    }

    @PostMapping("/pay-groups")
    public ResponseEntity<ApiResponse<PayGroupDto.Response>> createPayGroup(
            @Valid @RequestBody PayGroupDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(salaryStructureService.createPayGroup(request)));
    }
}
