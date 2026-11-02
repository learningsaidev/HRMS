package com.abcd.hrms.paymentledger.service;

import com.abcd.hrms.common.core.context.TenantContext;
import com.abcd.hrms.common.core.exception.BusinessValidationException;
import com.abcd.hrms.common.core.exception.EntityNotFoundException;
import com.abcd.hrms.common.core.util.MoneyUtil;
import com.abcd.hrms.paymentledger.dto.JournalEntryDto;
import com.abcd.hrms.paymentledger.entity.JournalEntry;
import com.abcd.hrms.paymentledger.entity.JournalLine;
import com.abcd.hrms.paymentledger.entity.LedgerAccount;
import com.abcd.hrms.paymentledger.repository.JournalEntryRepository;
import com.abcd.hrms.paymentledger.repository.JournalLineRepository;
import com.abcd.hrms.paymentledger.repository.LedgerAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneralLedgerService {

    private final JournalEntryRepository journalEntryRepository;
    private final JournalLineRepository journalLineRepository;
    private final LedgerAccountRepository ledgerAccountRepository;

    @Transactional
    public JournalEntryDto.Response createJournalEntry(JournalEntryDto.CreateRequest request) {
        UUID companyId = request.getCompanyId() != null ? request.getCompanyId() : TenantContext.getCompanyId();
        if (companyId == null) {
            companyId = UUID.fromString("d0000000-0000-0000-0000-000000000001");
        }

        // Validate double-entry mathematical balancing invariant
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;

        for (JournalEntryDto.LineCreateRequest lineReq : request.getLines()) {
            BigDecimal amt = MoneyUtil.round(lineReq.getAmount());
            if ("DEBIT".equalsIgnoreCase(lineReq.getEntryType())) {
                totalDebit = MoneyUtil.add(totalDebit, amt);
            } else if ("CREDIT".equalsIgnoreCase(lineReq.getEntryType())) {
                totalCredit = MoneyUtil.add(totalCredit, amt);
            } else {
                throw new BusinessValidationException("Entry type must be DEBIT or CREDIT", "ERR_INVALID_ENTRY_TYPE");
            }
        }

        if (!MoneyUtil.equals(totalDebit, totalCredit)) {
            throw new BusinessValidationException(
                    String.format("Double-entry ledger invariant violation: Total Debit (%s) does not balance Total Credit (%s)",
                            totalDebit, totalCredit),
                    "ERR_LEDGER_IMBALANCE"
            );
        }

        String entryNumber = "JV-" + request.getEntryDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        JournalEntry entry = JournalEntry.builder()
                .companyId(companyId)
                .legalEntityId(request.getLegalEntityId())
                .entryNumber(entryNumber)
                .entryDate(request.getEntryDate())
                .sourceModule(request.getSourceModule())
                .sourceReferenceId(request.getSourceReferenceId())
                .narration(request.getNarration())
                .totalAmount(totalDebit)
                .isPosted(true)
                .postedAt(Instant.now())
                .build();

        JournalEntry savedEntry = journalEntryRepository.save(entry);

        List<JournalLine> savedLines = new ArrayList<>();
        for (JournalEntryDto.LineCreateRequest lineReq : request.getLines()) {
            JournalLine line = JournalLine.builder()
                    .journalEntryId(savedEntry.getId())
                    .accountId(lineReq.getAccountId())
                    .entryType(lineReq.getEntryType().toUpperCase())
                    .amount(MoneyUtil.round(lineReq.getAmount()))
                    .description(lineReq.getDescription())
                    .build();
            savedLines.add(journalLineRepository.save(line));
        }

        log.info("Posted balanced Journal Voucher {} (Amount: {}) for legalEntity: {}",
                savedEntry.getEntryNumber(), totalDebit, savedEntry.getLegalEntityId());

        return mapToDto(savedEntry, savedLines);
    }

    @Transactional(readOnly = true)
    public JournalEntryDto.Response getJournalEntry(UUID entryId) {
        JournalEntry entry = journalEntryRepository.findById(entryId)
                .orElseThrow(() -> new EntityNotFoundException("JournalEntry", entryId));
        List<JournalLine> lines = journalLineRepository.findByJournalEntryId(entryId);
        return mapToDto(entry, lines);
    }

    @Transactional(readOnly = true)
    public List<JournalEntryDto.Response> listJournalEntries(UUID companyId) {
        UUID effectiveCompanyId = companyId != null ? companyId : TenantContext.getCompanyId();
        if (effectiveCompanyId == null) {
            effectiveCompanyId = UUID.fromString("d0000000-0000-0000-0000-000000000001");
        }
        return journalEntryRepository.findByCompanyIdOrderByEntryDateDesc(effectiveCompanyId).stream()
                .map(e -> mapToDto(e, journalLineRepository.findByJournalEntryId(e.getId())))
                .collect(Collectors.toList());
    }

    private JournalEntryDto.Response mapToDto(JournalEntry e, List<JournalLine> lines) {
        Map<UUID, LedgerAccount> accounts = ledgerAccountRepository.findAll().stream()
                .collect(Collectors.toMap(LedgerAccount::getId, a -> a));

        List<JournalEntryDto.LineDto> lineDtos = lines.stream()
                .map(l -> {
                    LedgerAccount acc = accounts.get(l.getAccountId());
                    return JournalEntryDto.LineDto.builder()
                            .id(l.getId())
                            .accountId(l.getAccountId())
                            .accountCode(acc != null ? acc.getAccountCode() : "UNKNOWN")
                            .accountName(acc != null ? acc.getAccountName() : "Unknown Account")
                            .entryType(l.getEntryType())
                            .amount(l.getAmount())
                            .description(l.getDescription())
                            .build();
                })
                .collect(Collectors.toList());

        return JournalEntryDto.Response.builder()
                .id(e.getId())
                .companyId(e.getCompanyId())
                .legalEntityId(e.getLegalEntityId())
                .entryNumber(e.getEntryNumber())
                .entryDate(e.getEntryDate())
                .sourceModule(e.getSourceModule())
                .sourceReferenceId(e.getSourceReferenceId())
                .narration(e.getNarration())
                .totalAmount(e.getTotalAmount())
                .isPosted(e.getIsPosted())
                .postedAt(e.getPostedAt())
                .createdAt(e.getCreatedAt())
                .lines(lineDtos)
                .build();
    }
}
