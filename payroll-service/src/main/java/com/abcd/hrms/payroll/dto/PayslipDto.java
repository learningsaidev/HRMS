package com.abcd.hrms.payroll.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class PayslipDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private UUID id;
        private UUID payrollRunId;
        private UUID employeeId;
        private UUID companyId;
        private UUID payGroupId;
        private Integer daysInMonth;
        private BigDecimal paidDays;
        private BigDecimal lopDays;
        private BigDecimal grossEarnings;
        private BigDecimal totalDeductions;
        private BigDecimal netPay;
        private String paymentStatus;
        private String pdfStoragePath;
        private Instant createdAt;
        private List<LineItem> lines;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LineItem {
        private UUID id;
        private UUID componentId;
        private String componentCode;
        private String componentName;
        private String componentType;
        private BigDecimal amount;
        private BigDecimal ytdAmount;
        private Boolean isTaxable;
        private Boolean isStatutory;
    }
}
