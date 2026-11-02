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
public class LeaveBalanceDto {

    private UUID leaveTypeId;

    private String leaveTypeCode;

    private String leaveTypeName;

    private boolean isPaid;

    private BigDecimal currentBalance;

    private BigDecimal openingBalance;

    private BigDecimal accrued;

    private BigDecimal consumed;
}
