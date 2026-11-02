package com.abcd.hrms.taxbenefits.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class LoanMasterDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private UUID id;
        private UUID companyId;
        private UUID employeeId;
        private String loanType;
        private BigDecimal principalAmount;
        private BigDecimal interestRate;
        private Integer tenureMonths;
        private BigDecimal monthlyEmi;
        private LocalDate disbursementDate;
        private String status;
        private BigDecimal totalPaid;
        private BigDecimal balancePrincipal;
        private Instant createdAt;
        private List<InstallmentDto> installments;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstallmentDto {
        private UUID id;
        private Integer installmentNumber;
        private LocalDate dueDate;
        private BigDecimal emiAmount;
        private BigDecimal principalComponent;
        private BigDecimal interestComponent;
        private String status;
        private UUID payrollRunId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplyRequest {
        private UUID companyId;
        @NotNull(message = "Employee ID is mandatory")
        private UUID employeeId;
        private String loanType = "SALARY_ADVANCE";
        @NotNull(message = "Principal amount is mandatory")
        private BigDecimal principalAmount;
        private BigDecimal interestRate; // Annual %
        @NotNull(message = "Tenure in months is mandatory")
        private Integer tenureMonths;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DisburseRequest {
        @NotNull(message = "Disbursement date is mandatory")
        private LocalDate disbursementDate;
    }
}

