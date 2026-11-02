package com.abcd.hrms.payroll.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class EmployeeCompensationDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private UUID id;
        private UUID companyId;
        private UUID employeeId;
        private UUID payGroupId;
        private LocalDate effectiveFrom;
        private LocalDate effectiveTo;
        private BigDecimal ctcAnnual;
        private BigDecimal grossMonthly;
        private Boolean isActive;
        private List<ComponentItem> components;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComponentItem {
        private UUID componentId;
        private String componentCode;
        private String componentName;
        private String componentType;
        private BigDecimal amountMonthly;
        private BigDecimal percentageValue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignRequest {
        private UUID companyId;
        @NotNull(message = "Employee ID is mandatory")
        private UUID employeeId;
        @NotNull(message = "Pay group ID is mandatory")
        private UUID payGroupId;
        @NotNull(message = "Effective from date is mandatory")
        private LocalDate effectiveFrom;
        private LocalDate effectiveTo;
        @NotNull(message = "CTC annual is mandatory")
        private BigDecimal ctcAnnual;
        private BigDecimal grossMonthly;
        private List<AssignComponentItem> components;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignComponentItem {
        @NotNull(message = "Component ID is mandatory")
        private UUID componentId;
        @NotNull(message = "Amount monthly is mandatory")
        private BigDecimal amountMonthly;
        private BigDecimal percentageValue;
    }
}
