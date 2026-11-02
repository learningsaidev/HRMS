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
public class LeaveApplicationDto {

    private UUID id;

    private UUID employeeId;

    private UUID leaveTypeId;

    private String leaveTypeCode;

    private String leaveTypeName;

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal totalDays;

    private boolean isHalfDay;

    private String reason;

    private String status;

    private UUID approvedBy;

    private Instant approvedAt;

    private String rejectionReason;

    private Instant createdAt;
}
