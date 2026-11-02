package com.abcd.hrms.paymentledger.controller;

import com.abcd.hrms.common.core.dto.ApiResponse;
import com.abcd.hrms.paymentledger.dto.JournalEntryDto;
import com.abcd.hrms.paymentledger.service.GeneralLedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ledger/journals")
@RequiredArgsConstructor
public class GeneralLedgerController {

    private final GeneralLedgerService generalLedgerService;

    @PostMapping
    public ResponseEntity<ApiResponse<JournalEntryDto.Response>> createJournalEntry(
            @Valid @RequestBody JournalEntryDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(generalLedgerService.createJournalEntry(request)));
    }

    @GetMapping("/{entryId}")
    public ResponseEntity<ApiResponse<JournalEntryDto.Response>> getJournalEntry(@PathVariable UUID entryId) {
        return ResponseEntity.ok(ApiResponse.ok(generalLedgerService.getJournalEntry(entryId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<JournalEntryDto.Response>>> listJournalEntries(
            @RequestParam(required = false) UUID companyId) {
        return ResponseEntity.ok(ApiResponse.ok(generalLedgerService.listJournalEntries(companyId)));
    }
}

