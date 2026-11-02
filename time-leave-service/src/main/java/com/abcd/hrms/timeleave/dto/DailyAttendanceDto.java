package com.abcd.hrms.timeleave.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyAttendanceDto {

    private UUID id;

    private UUID employeeId;

    private LocalDate attendanceDate;

    private Instant firstPunchIn;

    private Instant lastPunchOut;

    private BigDecimal totalWorkedHours;

    private BigDecimal overtimeHours;

    private String status;

    private boolean isRegularized;

    private String regularizationReason;
}
