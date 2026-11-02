package com.abcd.hrms.payroll.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public class PayGroupDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private UUID id;
        private UUID companyId;
        private UUID legalEntityId;
        private String groupCode;
        private String groupName;
        private String payFrequency;
        private String currency;
        private Integer salaryDisbursementDay;
        private Boolean isActive;
        private Instant createdAt;
        private Instant updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private UUID companyId;
        @NotNull(message = "Legal entity ID is mandatory")
        private UUID legalEntityId;
        @NotBlank(message = "Group code is mandatory")
        private String groupCode;
        @NotBlank(message = "Group name is mandatory")
        private String groupName;
        private String payFrequency = "MONTHLY";
        private String currency = "INR";
        private Integer salaryDisbursementDay = 30;
    }
}
