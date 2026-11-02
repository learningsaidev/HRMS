package com.abcd.hrms.paymentledger.service;

import com.abcd.hrms.common.messaging.outbox.OutboxPublisherService;
import com.abcd.hrms.common.security.encryption.FieldEncryptionService;
import com.abcd.hrms.paymentledger.dto.PaymentBatchDto;
import com.abcd.hrms.paymentledger.entity.LedgerAccount;
import com.abcd.hrms.paymentledger.entity.PaymentBatch;
import com.abcd.hrms.paymentledger.entity.PaymentTransaction;
import com.abcd.hrms.paymentledger.repository.LedgerAccountRepository;
import com.abcd.hrms.paymentledger.repository.PaymentBatchRepository;
import com.abcd.hrms.paymentledger.repository.PaymentTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentDisbursementService Compensating Saga & Returns Tests")
class PaymentDisbursementServiceTest {

    @Mock
    private PaymentBatchRepository batchRepository;
    @Mock
    private PaymentTransactionRepository transactionRepository;
    @Mock
    private LedgerAccountRepository accountRepository;
    @Mock
    private GeneralLedgerService generalLedgerService;
    @Mock
    private FieldEncryptionService encryptionService;
    @Mock
    private OutboxPublisherService outboxPublisherService;

    @InjectMocks
    private PaymentDisbursementService disbursementService;

    private UUID batchId;
    private UUID txId;
    private UUID companyId;

    @BeforeEach
    void setUp() {
        batchId = UUID.randomUUID();
        txId = UUID.randomUUID();
        companyId = UUID.randomUUID();
    }

    @Test
    void testHandlePaymentReturnCreatesReversalAndPublishesEvent() {
        PaymentBatch batch = PaymentBatch.builder()
                .companyId(companyId)
                .legalEntityId(UUID.randomUUID())
                .batchReference("BATCH-202611-001")
                .totalAmount(new BigDecimal("50000.0000"))
                .status("PROCESSING")
                .build();
        batch.setId(batchId);

        PaymentTransaction tx = PaymentTransaction.builder()
                .batchId(batchId)
                .companyId(companyId)
                .employeeId(UUID.randomUUID())
                .beneficiaryName("Bob Smith")
                .accountNumberEncrypted("enc_1234567890")
                .ifscCode("HDFC0001234")
                .amount(new BigDecimal("50000.0000"))
                .status("IN_TRANSIT")
                .build();
        tx.setId(txId);

        when(transactionRepository.findById(txId)).thenReturn(Optional.of(tx));
        when(transactionRepository.save(any(PaymentTransaction.class))).thenAnswer(i -> i.getArgument(0));
        when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));

        LedgerAccount bankAcc = LedgerAccount.builder().companyId(companyId).accountCode("1001").accountName("Bank").build();
        bankAcc.setId(UUID.randomUUID());
        LedgerAccount clearingAcc = LedgerAccount.builder().companyId(companyId).accountCode("2001").accountName("Clearing").build();
        clearingAcc.setId(UUID.randomUUID());

        when(accountRepository.findByCompanyIdAndAccountCode(companyId, "1001")).thenReturn(Optional.of(bankAcc));
        when(accountRepository.findByCompanyIdAndAccountCode(companyId, "2001")).thenReturn(Optional.of(clearingAcc));
        when(encryptionService.decrypt(any())).thenReturn("1234567890");

        PaymentBatchDto.PaymentReturnRequest returnReq = new PaymentBatchDto.PaymentReturnRequest();
        returnReq.setTransactionId(txId);
        returnReq.setReturnReason("Account frozen by beneficiary bank");

        PaymentBatchDto.TransactionDto result = disbursementService.handlePaymentReturn(returnReq);
        assertEquals("RETURNED", result.getStatus());
        assertEquals("Account frozen by beneficiary bank", result.getFailureReason());

        // Verify Compensating Double-Entry Journal Entry was posted
        verify(generalLedgerService, times(1)).createJournalEntry(any());
        // Verify PaymentReturned.v1 event was published
        verify(outboxPublisherService, times(1)).enqueueEvent(any(), any(), any(), any());
    }
}

