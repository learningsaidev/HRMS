package com.abcd.hrms.taxbenefits.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class TaxDeclarationDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private UUID id;
        private UUID companyId;
        private UUID employeeId;
        private String financialYear;
        private String regimeCode;
        private String status;
        private BigDecimal totalDeclaredDeductions;
        private BigDecimal totalVerifiedDeductions;
        private Instant createdAt;
        private List<ItemDto> items;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemDto {
        private UUID id;
        private String sectionCode;
        private String description;
        private BigDecimal declaredAmount;
        private BigDecimal verifiedAmount;
        private String proofStoragePath;
        private String verificationStatus;
        private String rejectionReason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubmitRequest {
        private UUID companyId;
        @NotNull(message = "Employee ID is mandatory")
        private UUID employeeId;
        @NotBlank(message = "Financial year is mandatory")
        private String financialYear;
        private String regimeCode = "NEW";
        private List<ItemSubmitRequest> items;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemSubmitRequest {
        @NotBlank(message = "Section code is mandatory")
        private String sectionCode;
        private String description;
        @NotNull(message = "Declared amount is mandatory")
        private BigDecimal declaredAmount;
        private String proofStoragePath;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerifyItemRequest {
        @NotNull(message = "Item ID is mandatory")
        private UUID itemId;
        @NotBlank(message = "Status is mandatory (APPROVED, REJECTED)")
        private String status;
        private BigDecimal verifiedAmount;
        private String rejectionReason;
    }
}

