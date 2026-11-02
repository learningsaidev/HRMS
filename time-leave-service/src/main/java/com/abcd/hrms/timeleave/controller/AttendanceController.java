package com.abcd.hrms.timeleave.controller;

import com.abcd.hrms.common.core.dto.ApiResponse;
import com.abcd.hrms.timeleave.dto.AttendanceLockRequest;
import com.abcd.hrms.timeleave.dto.DailyAttendanceDto;
import com.abcd.hrms.timeleave.dto.PayrollAttendanceSummaryDto;
import com.abcd.hrms.timeleave.dto.PunchRequest;
import com.abcd.hrms.timeleave.entity.AttendanceLock;
import com.abcd.hrms.timeleave.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance & Working Hours", description = "Biometric punches, daily attendance, regularization, and payroll lock")
@SecurityRequirement(name = "bearerAuth")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/punch")
    @PreAuthorize("hasRole('ROLE_EMPLOYEE') or hasRole('ROLE_MANAGER') or hasRole('ROLE_HR_ADMIN')")
    @Operation(summary = "Submit clock-in or clock-out punch")
    public ResponseEntity<ApiResponse<DailyAttendanceDto>> punch(@Valid @RequestBody PunchRequest request) {
        DailyAttendanceDto dto = attendanceService.recordPunch(request);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @PostMapping("/lock")
    @PreAuthorize("hasRole('ROLE_PAYROLL_MAKER') or hasRole('ROLE_HR_ADMIN') or hasRole('ROLE_SYSTEM_ADMIN')")
    @Operation(summary = "Lock monthly attendance before triggering payroll calculation")
    public ResponseEntity<ApiResponse<AttendanceLock>> lockPeriod(@Valid @RequestBody AttendanceLockRequest request) {
        AttendanceLock lock = attendanceService.lockAttendancePeriod(request);
        return ResponseEntity.ok(ApiResponse.ok(lock));
    }

    @GetMapping("/summary/{employeeId}")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_MANAGER', 'ROLE_HR_ADMIN', 'ROLE_PAYROLL_MAKER', 'ROLE_FINANCE_DIRECTOR')")
    @Operation(summary = "Get payroll-ready attendance summary for an employee")
    public ResponseEntity<ApiResponse<PayrollAttendanceSummaryDto>> getPayrollSummary(
            @PathVariable UUID employeeId,
            @RequestParam int year,
            @RequestParam int month) {
        PayrollAttendanceSummaryDto summary = attendanceService.getPayrollAttendanceSummary(employeeId, year, month);
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }
}
