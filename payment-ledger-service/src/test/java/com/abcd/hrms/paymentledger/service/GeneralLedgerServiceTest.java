package com.abcd.hrms.paymentledger.service;

import com.abcd.hrms.common.core.exception.BusinessValidationException;
import com.abcd.hrms.paymentledger.dto.JournalEntryDto;
import com.abcd.hrms.paymentledger.entity.JournalEntry;
import com.abcd.hrms.paymentledger.entity.JournalLine;
import com.abcd.hrms.paymentledger.repository.JournalEntryRepository;
import com.abcd.hrms.paymentledger.repository.JournalLineRepository;
import com.abcd.hrms.paymentledger.repository.LedgerAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GeneralLedgerService Double-Entry Invariant Tests")
class GeneralLedgerServiceTest {

    @Mock
    private JournalEntryRepository journalEntryRepository;
    @Mock
    private JournalLineRepository journalLineRepository;
    @Mock
    private LedgerAccountRepository ledgerAccountRepository;

    @InjectMocks
    private GeneralLedgerService generalLedgerService;

    private UUID companyId;
    private UUID legalEntityId;
    private UUID debitAccountId;
    private UUID creditAccountId;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        legalEntityId = UUID.randomUUID();
        debitAccountId = UUID.randomUUID();
        creditAccountId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Verify strictly balanced Journal Entry succeeds")
    void testBalancedJournalEntrySuccess() {
        JournalEntryDto.CreateRequest request = new JournalEntryDto.CreateRequest();
        request.setCompanyId(companyId);
        request.setLegalEntityId(legalEntityId);
        request.setEntryDate(LocalDate.of(2026, 11, 30));
        request.setSourceModule("PAYROLL");
        request.setNarration("Monthly salary disbursement");

        JournalEntryDto.LineCreateRequest debit = new JournalEntryDto.LineCreateRequest(
                debitAccountId, "DEBIT", new BigDecimal("100000.0000"), "Salary Expense"
        );
        JournalEntryDto.LineCreateRequest credit = new JournalEntryDto.LineCreateRequest(
                creditAccountId, "CREDIT", new BigDecimal("100000.0000"), "Salary Payable"
        );
        request.setLines(Arrays.asList(debit, credit));

        when(journalEntryRepository.save(any(JournalEntry.class))).thenAnswer(i -> {
            JournalEntry e = i.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });
        when(journalLineRepository.save(any(JournalLine.class))).thenAnswer(i -> {
            JournalLine l = i.getArgument(0);
            l.setId(UUID.randomUUID());
            return l;
        });

        JournalEntryDto.Response response = generalLedgerService.createJournalEntry(request);
        assertNotNull(response);
        assertTrue(response.getIsPosted());
        assertEquals(0, new BigDecimal("100000.0000").compareTo(response.getTotalAmount()));
        verify(journalLineRepository, times(2)).save(any(JournalLine.class));
    }

    @Test
    @DisplayName("Verify imbalanced Journal Entry throws ERR_LEDGER_IMBALANCE")
    void testImbalancedJournalEntryThrowsException() {
        JournalEntryDto.CreateRequest request = new JournalEntryDto.CreateRequest();
        request.setCompanyId(companyId);
        request.setLegalEntityId(legalEntityId);
        request.setEntryDate(LocalDate.of(2026, 11, 30));
        request.setSourceModule("MANUAL");

        JournalEntryDto.LineCreateRequest debit = new JournalEntryDto.LineCreateRequest(
                debitAccountId, "DEBIT", new BigDecimal("100000.0000"), "Salary Expense"
        );
        JournalEntryDto.LineCreateRequest credit = new JournalEntryDto.LineCreateRequest(
                creditAccountId, "CREDIT", new BigDecimal("99999.0000"), "Salary Payable (Imbalanced)"
        );
        request.setLines(Arrays.asList(debit, credit));

        BusinessValidationException ex = assertThrows(BusinessValidationException.class,
                () -> generalLedgerService.createJournalEntry(request));
        assertEquals("ERR_LEDGER_IMBALANCE", ex.getErrorCode());
        verify(journalEntryRepository, never()).save(any());
    }
}
