package com.abcd.hrms.timeleave.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollAttendanceSummaryDto {

    private UUID employeeId;

    private int periodYear;

    private int periodMonth;

    private BigDecimal totalCalendarDays;

    private BigDecimal payableDays;

    private BigDecimal lossOfPayDays; // Unpaid leave days

    private BigDecimal paidLeaveDays;

    private BigDecimal overtimeHours;
}
