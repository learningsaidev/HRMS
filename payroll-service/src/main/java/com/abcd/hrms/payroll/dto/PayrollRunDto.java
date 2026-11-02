package com.abcd.hrms.payroll.dto;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class PayrollRunDto {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InitiateRequest {
        private UUID companyId;

        @NotNull(message = "Legal Entity ID is mandatory")
        private UUID legalEntityId;

        @NotNull(message = "Pay Group ID is mandatory")
        private UUID payGroupId;

        @NotNull(message = "Period start date is mandatory")
        private LocalDate periodStart;

        @NotNull(message = "Period end date is mandatory")
        private LocalDate periodEnd;

        private String runType;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private UUID id;
        private UUID companyId;
        private UUID legalEntityId;
        private UUID payGroupId;
        private String runCode;
        private LocalDate periodStart;
        private LocalDate periodEnd;
        private String runType;
        private Integer totalEmployees;
        private BigDecimal totalGross;
        private BigDecimal totalDeductions;
        private BigDecimal totalNet;
        private String status;
        private String createdBy;
        private UUID approvedBy;
        private Instant lockedAt;
        private Instant createdAt;
    }
}
