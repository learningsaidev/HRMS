package com.abcd.hrms.paymentledger.service;

import com.abcd.hrms.common.core.context.TenantContext;
import com.abcd.hrms.common.core.exception.BusinessValidationException;
import com.abcd.hrms.common.core.exception.EntityNotFoundException;
import com.abcd.hrms.common.core.util.MoneyUtil;
import com.abcd.hrms.common.messaging.KafkaTopicConstants;
import com.abcd.hrms.common.messaging.event.CanonicalEvent;
import com.abcd.hrms.common.messaging.outbox.OutboxPublisherService;
import com.abcd.hrms.common.security.encryption.FieldEncryptionService;
import com.abcd.hrms.common.security.masking.DataMaskingUtil;
import com.abcd.hrms.paymentledger.dto.JournalEntryDto;
import com.abcd.hrms.paymentledger.dto.PaymentBatchDto;
import com.abcd.hrms.paymentledger.entity.LedgerAccount;
import com.abcd.hrms.paymentledger.entity.PaymentBatch;
import com.abcd.hrms.paymentledger.entity.PaymentTransaction;
import com.abcd.hrms.paymentledger.repository.LedgerAccountRepository;
import com.abcd.hrms.paymentledger.repository.PaymentBatchRepository;
import com.abcd.hrms.paymentledger.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentDisbursementService {

    private final PaymentBatchRepository batchRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final LedgerAccountRepository accountRepository;
    private final GeneralLedgerService generalLedgerService;
    private final FieldEncryptionService encryptionService;
    private final OutboxPublisherService outboxPublisherService;

    @Transactional
    public PaymentBatchDto.Response processBatch(UUID batchId) {
        PaymentBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new EntityNotFoundException("PaymentBatch", batchId));

        if (!"PENDING".equalsIgnoreCase(batch.getStatus()) && !"APPROVED".equalsIgnoreCase(batch.getStatus())) {
            throw new BusinessValidationException("Batch is not in a payable state", "ERR_BATCH_NOT_PAYABLE");
        }

        batch.setStatus("PROCESSING");
        PaymentBatch savedBatch = batchRepository.save(batch);

        List<PaymentTransaction> txList = transactionRepository.findByBatchId(batchId);
        for (PaymentTransaction tx : txList) {
            tx.setStatus("IN_TRANSIT");
            transactionRepository.save(tx);
        }

        // Debit: Salaries Payable Clearing (2001), Credit: Bank Account (1001)
        LedgerAccount clearingAcc = accountRepository.findByCompanyIdAndAccountCode(batch.getCompanyId(), "2001")
                .orElse(null);
        LedgerAccount bankAcc = accountRepository.findByCompanyIdAndAccountCode(batch.getCompanyId(), "1001")
                .orElse(null);

        if (clearingAcc != null && bankAcc != null && batch.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            JournalEntryDto.CreateRequest journalReq = new JournalEntryDto.CreateRequest();
            journalReq.setCompanyId(batch.getCompanyId());
            journalReq.setLegalEntityId(batch.getLegalEntityId());
            journalReq.setEntryDate(LocalDate.now());
            journalReq.setSourceModule("PAYROLL_DISBURSEMENT");
            journalReq.setSourceReferenceId(batch.getBatchReference());
            journalReq.setNarration("Bank disbursement batch " + batch.getBatchReference());

            JournalEntryDto.LineCreateRequest debitLine = new JournalEntryDto.LineCreateRequest(
                    clearingAcc.getId(), "DEBIT", batch.getTotalAmount(), "Clearing net salary liability"
            );
            JournalEntryDto.LineCreateRequest creditLine = new JournalEntryDto.LineCreateRequest(
                    bankAcc.getId(), "CREDIT", batch.getTotalAmount(), "Disbursement from corporate bank account"
            );
            journalReq.setLines(Arrays.asList(debitLine, creditLine));
            generalLedgerService.createJournalEntry(journalReq);
        }

        return mapToDto(savedBatch, txList);
    }

    @Transactional
    public PaymentBatchDto.TransactionDto recordTransactionResult(UUID batchId, PaymentBatchDto.ProcessResultRequest request) {
        PaymentTransaction tx = transactionRepository.findById(request.getTransactionId())
                .orElseThrow(() -> new EntityNotFoundException("PaymentTransaction", request.getTransactionId()));

        tx.setStatus(request.getStatus().toUpperCase());
        if ("SUCCESS".equalsIgnoreCase(request.getStatus())) {
            tx.setUtrNumber(request.getUtrNumber());
            tx.setFailureReason(null);
        } else {
            tx.setFailureReason(request.getFailureReason());
        }

        PaymentTransaction savedTx = transactionRepository.save(tx);

        // Check if all transactions in batch are settled
        List<PaymentTransaction> batchTxs = transactionRepository.findByBatchId(batchId);
        boolean allSettled = batchTxs.stream()
                .noneMatch(t -> "PENDING".equalsIgnoreCase(t.getStatus()) || "IN_TRANSIT".equalsIgnoreCase(t.getStatus()));

        if (allSettled) {
            PaymentBatch batch = batchRepository.findById(batchId).orElse(null);
            if (batch != null) {
                boolean hasFailures = batchTxs.stream().anyMatch(t -> "FAILED".equalsIgnoreCase(t.getStatus()) || "RETURNED".equalsIgnoreCase(t.getStatus()));
                batch.setStatus(hasFailures ? "PARTIALLY_FAILED" : "COMPLETED");
                batchRepository.save(batch);
            }
        }

        return mapToTxDto(savedTx);
    }

    @Transactional
    public PaymentBatchDto.TransactionDto handlePaymentReturn(PaymentBatchDto.PaymentReturnRequest request) {
        PaymentTransaction tx = transactionRepository.findById(request.getTransactionId())
                .orElseThrow(() -> new EntityNotFoundException("PaymentTransaction", request.getTransactionId()));

        tx.setStatus("RETURNED");
        tx.setFailureReason(request.getReturnReason());
        PaymentTransaction savedTx = transactionRepository.save(tx);

        PaymentBatch batch = batchRepository.findById(tx.getBatchId())
                .orElseThrow(() -> new EntityNotFoundException("PaymentBatch", tx.getBatchId()));

        // Compensating Double-Entry Journal Entry:
        // Debit: Bank Account (1001) - money returned to company
        // Credit: Salaries Payable (2001) - obligation to employee reinstated
        LedgerAccount bankAcc = accountRepository.findByCompanyIdAndAccountCode(batch.getCompanyId(), "1001").orElse(null);
        LedgerAccount clearingAcc = accountRepository.findByCompanyIdAndAccountCode(batch.getCompanyId(), "2001").orElse(null);

        if (bankAcc != null && clearingAcc != null) {
            JournalEntryDto.CreateRequest reversal = new JournalEntryDto.CreateRequest();
            reversal.setCompanyId(batch.getCompanyId());
            reversal.setLegalEntityId(batch.getLegalEntityId());
            reversal.setEntryDate(LocalDate.now());
            reversal.setSourceModule("PAYMENT_RETURN");
            reversal.setSourceReferenceId(tx.getId().toString());
            reversal.setNarration(String.format("Payment return reversal for employee %s: %s",
                    tx.getEmployeeId(), request.getReturnReason()));

            JournalEntryDto.LineCreateRequest debitBank = new JournalEntryDto.LineCreateRequest(
                    bankAcc.getId(), "DEBIT", tx.getAmount(), "Funds returned by bank rail"
            );
            JournalEntryDto.LineCreateRequest creditClearing = new JournalEntryDto.LineCreateRequest(
                    clearingAcc.getId(), "CREDIT", tx.getAmount(), "Reinstated salary payable liability"
            );
            reversal.setLines(Arrays.asList(debitBank, creditClearing));
            generalLedgerService.createJournalEntry(reversal);
        }

        // Publish PaymentReturned.v1 event
        Map<String, Object> payload = new HashMap<>();
        payload.put("transactionId", savedTx.getId());
        payload.put("batchId", savedTx.getBatchId());
        payload.put("employeeId", savedTx.getEmployeeId());
        payload.put("amount", savedTx.getAmount().toString());
        payload.put("returnReason", request.getReturnReason());

        CanonicalEvent<Map<String, Object>> event = CanonicalEvent.of(
                "PaymentReturned.v1",
                "PAYMENT_TRANSACTION",
                savedTx.getId().toString(),
                savedTx.getCompanyId(),
                batch.getLegalEntityId(),
                "payment-ledger-service",
                TenantContext.getCorrelationId(),
                payload
        );

        outboxPublisherService.enqueueEvent(
                KafkaTopicConstants.TOPIC_PAYMENT_LEDGER_EVENTS,
                "PAYMENT_TRANSACTION",
                savedTx.getId().toString(),
                event
        );

        return mapToTxDto(savedTx);
    }

    @Transactional(readOnly = true)
    public PaymentBatchDto.Response getBatchById(UUID batchId) {
        PaymentBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new EntityNotFoundException("PaymentBatch", batchId));
        List<PaymentTransaction> txs = transactionRepository.findByBatchId(batchId);
        return mapToDto(batch, txs);
    }

    @Transactional(readOnly = true)
    public List<PaymentBatchDto.Response> listBatches(UUID companyId) {
        UUID effectiveCompanyId = companyId != null ? companyId : TenantContext.getCompanyId();
        if (effectiveCompanyId == null) {
            effectiveCompanyId = UUID.fromString("d0000000-0000-0000-0000-000000000001");
        }
        return batchRepository.findByCompanyIdOrderByCreatedAtDesc(effectiveCompanyId).stream()
                .map(b -> mapToDto(b, transactionRepository.findByBatchId(b.getId())))
                .collect(Collectors.toList());
    }

    private PaymentBatchDto.Response mapToDto(PaymentBatch b, List<PaymentTransaction> txs) {
        List<PaymentBatchDto.TransactionDto> txDtos = txs.stream()
                .map(this::mapToTxDto)
                .collect(Collectors.toList());

        return PaymentBatchDto.Response.builder()
                .id(b.getId())
                .companyId(b.getCompanyId())
                .legalEntityId(b.getLegalEntityId())
                .batchReference(b.getBatchReference())
                .sourceType(b.getSourceType())
                .sourceId(b.getSourceId())
                .paymentMethod(b.getPaymentMethod())
                .currency(b.getCurrency())
                .totalAmount(b.getTotalAmount())
                .totalTransactions(b.getTotalTransactions())
                .status(b.getStatus())
                .createdAt(b.getCreatedAt())
                .transactions(txDtos)
                .build();
    }

    private PaymentBatchDto.TransactionDto mapToTxDto(PaymentTransaction tx) {
        String decryptedAcc = "";
        try {
            decryptedAcc = encryptionService.decrypt(tx.getAccountNumberEncrypted());
        } catch (Exception e) {
            decryptedAcc = "0000000000";
        }

        return PaymentBatchDto.TransactionDto.builder()
                .id(tx.getId())
                .employeeId(tx.getEmployeeId())
                .beneficiaryName(tx.getBeneficiaryName())
                .accountNumberMasked(DataMaskingUtil.maskBankAccount(decryptedAcc))
                .ifscCode(tx.getIfscCode())
                .amount(tx.getAmount())
                .utrNumber(tx.getUtrNumber())
                .status(tx.getStatus())
                .failureReason(tx.getFailureReason())
                .build();
    }
}
