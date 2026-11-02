package com.abcd.hrms.workforce.controller;

import com.abcd.hrms.common.core.dto.ApiResponse;
import com.abcd.hrms.workforce.dto.CreateEmployeeRequest;
import com.abcd.hrms.workforce.dto.EmployeeDto;
import com.abcd.hrms.workforce.dto.UpdateEmploymentStatusRequest;
import com.abcd.hrms.workforce.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Master", description = "Employee lifecycle, personal profile, and employment records")
@SecurityRequirement(name = "bearerAuth")
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_HR_ADMIN') or hasRole('ROLE_SYSTEM_ADMIN')")
    @Operation(summary = "Create a new employee record and establish employment contract")
    public ResponseEntity<ApiResponse<EmployeeDto>> createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        EmployeeDto dto = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(dto));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_MANAGER', 'ROLE_HR_ADMIN', 'ROLE_PAYROLL_MAKER', 'ROLE_FINANCE_DIRECTOR')")
    @Operation(summary = "List workforce employees with pagination")
    public ResponseEntity<ApiResponse<Page<EmployeeDto>>> listEmployees(@PageableDefault(size = 20) Pageable pageable) {
        Page<EmployeeDto> page = employeeService.listEmployees(pageable);
        return ResponseEntity.ok(ApiResponse.ok(page));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_MANAGER', 'ROLE_HR_ADMIN', 'ROLE_PAYROLL_MAKER', 'ROLE_FINANCE_DIRECTOR')")
    @Operation(summary = "Get employee profile by ID with masked banking/tax details")
    public ResponseEntity<ApiResponse<EmployeeDto>> getEmployeeById(@PathVariable UUID id) {
        EmployeeDto dto = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_HR_ADMIN') or hasRole('ROLE_SYSTEM_ADMIN')")
    @Operation(summary = "Update employee lifecycle status (confirm, notice, terminate, on-leave)")
    public ResponseEntity<ApiResponse<EmployeeDto>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEmploymentStatusRequest request) {
        EmployeeDto dto = employeeService.updateStatus(id, request);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }
}
