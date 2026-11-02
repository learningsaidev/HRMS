package com.abcd.hrms.payroll.dto;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class EmployeeCompensationDto {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignComponentItem {
        @NotNull(message = "Component ID is mandatory")
        private UUID componentId;

        @NotNull(message = "Monthly amount is mandatory")
        private BigDecimal amountMonthly;

        private BigDecimal percentageValue;
    }

    @Getter
    @Setter
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

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignRequest {
        private UUID companyId;

        @NotNull(message = "Employee ID is mandatory")
        private UUID employeeId;

        @NotNull(message = "Pay Group ID is mandatory")
        private UUID payGroupId;

        @NotNull(message = "Effective from date is mandatory")
        private LocalDate effectiveFrom;

        private LocalDate effectiveTo;

        @NotNull(message = "Annual CTC is mandatory")
        private BigDecimal ctcAnnual;

        private BigDecimal grossMonthly;

        private List<AssignComponentItem> components;
    }

    @Getter
    @Setter
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
}
