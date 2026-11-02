package com.abcd.hrms.paymentledger.dto;

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

public class PaymentBatchDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private UUID id;
        private UUID companyId;
        private UUID legalEntityId;
        private String batchReference;
        private String sourceType;
        private String sourceId;
        private String paymentMethod;
        private String currency;
        private BigDecimal totalAmount;
        private Integer totalTransactions;
        private String status;
        private Instant createdAt;
        private List<TransactionDto> transactions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionDto {
        private UUID id;
        private UUID employeeId;
        private String beneficiaryName;
        private String accountNumberMasked;
        private String ifscCode;
        private BigDecimal amount;
        private String utrNumber;
        private String status;
        private String failureReason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessResultRequest {
        @NotNull(message = "Transaction ID is mandatory")
        private UUID transactionId;
        @NotBlank(message = "Status is mandatory (SUCCESS, FAILED, RETURNED)")
        private String status;
        private String utrNumber;
        private String failureReason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentReturnRequest {
        @NotNull(message = "Transaction ID is mandatory")
        private UUID transactionId;
        @NotBlank(message = "Return reason is mandatory")
        private String returnReason;
    }
}

