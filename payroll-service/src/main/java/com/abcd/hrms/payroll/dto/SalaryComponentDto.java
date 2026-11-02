package com.abcd.hrms.payroll.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.UUID;

public class SalaryComponentDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private UUID id;
        private UUID companyId;
        private String componentCode;
        private String componentName;
        private String componentType;
        private String calculationType;
        private Boolean taxable;
        private Boolean affectsGross;
        private Boolean affectsNet;
        private Integer displayOrder;
        private Boolean isActive;
        private Instant createdAt;
        private Instant updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private UUID companyId;
        @NotBlank(message = "Component code is mandatory")
        private String componentCode;
        @NotBlank(message = "Component name is mandatory")
        private String componentName;
        @NotBlank(message = "Component type is mandatory")
        private String componentType;
        private String calculationType = "FIXED";
        private Boolean taxable = true;
        private Boolean affectsGross = true;
        private Boolean affectsNet = true;
        private Integer displayOrder = 0;
    }
}
