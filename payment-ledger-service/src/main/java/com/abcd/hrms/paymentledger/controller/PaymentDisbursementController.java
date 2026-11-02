package com.abcd.hrms.paymentledger.controller;

import com.abcd.hrms.common.core.dto.ApiResponse;
import com.abcd.hrms.paymentledger.dto.PaymentBatchDto;
import com.abcd.hrms.paymentledger.service.PaymentDisbursementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ledger/payments")
@RequiredArgsConstructor
public class PaymentDisbursementController {

    private final PaymentDisbursementService disbursementService;

    @PostMapping("/batches/{batchId}/process")
    public ResponseEntity<ApiResponse<PaymentBatchDto.Response>> processBatch(@PathVariable UUID batchId) {
        return ResponseEntity.ok(ApiResponse.ok(disbursementService.processBatch(batchId)));
    }

    @PostMapping("/batches/{batchId}/results")
    public ResponseEntity<ApiResponse<PaymentBatchDto.TransactionDto>> recordTransactionResult(
            @PathVariable UUID batchId,
            @Valid @RequestBody PaymentBatchDto.ProcessResultRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(disbursementService.recordTransactionResult(batchId, request)));
    }

    @PostMapping("/transactions/return")
    public ResponseEntity<ApiResponse<PaymentBatchDto.TransactionDto>> handlePaymentReturn(
            @Valid @RequestBody PaymentBatchDto.PaymentReturnRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(disbursementService.handlePaymentReturn(request)));
    }

    @GetMapping("/batches/{batchId}")
    public ResponseEntity<ApiResponse<PaymentBatchDto.Response>> getBatchById(@PathVariable UUID batchId) {
        return ResponseEntity.ok(ApiResponse.ok(disbursementService.getBatchById(batchId)));
    }

    @GetMapping("/batches")
    public ResponseEntity<ApiResponse<List<PaymentBatchDto.Response>>> listBatches(
            @RequestParam(required = false) UUID companyId) {
        return ResponseEntity.ok(ApiResponse.ok(disbursementService.listBatches(companyId)));
    }
}

