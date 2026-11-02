package com.abcd.hrms.paymentledger.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class JournalEntryDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private UUID id;
        private UUID companyId;
        private UUID legalEntityId;
        private String entryNumber;
        private LocalDate entryDate;
        private String sourceModule;
        private String sourceReferenceId;
        private String narration;
        private BigDecimal totalAmount;
        private Boolean isPosted;
        private Instant postedAt;
        private Instant createdAt;
        private List<LineDto> lines;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LineDto {
        private UUID id;
        private UUID accountId;
        private String accountCode;
        private String accountName;
        private String entryType; // DEBIT, CREDIT
        private BigDecimal amount;
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private UUID companyId;
        @NotNull(message = "Legal entity ID is mandatory")
        private UUID legalEntityId;
        @NotNull(message = "Entry date is mandatory")
        private LocalDate entryDate;
        @NotBlank(message = "Source module is mandatory")
        private String sourceModule = "MANUAL";
        private String sourceReferenceId;
        private String narration;
        @NotEmpty(message = "At least two journal lines are required for double-entry")
        private List<LineCreateRequest> lines;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LineCreateRequest {
        @NotNull(message = "Account ID is mandatory")
        private UUID accountId;
        @NotBlank(message = "Entry type is mandatory (DEBIT, CREDIT)")
        private String entryType;
        @NotNull(message = "Amount is mandatory")
        private BigDecimal amount;
        private String description;
    }
}

