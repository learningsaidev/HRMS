package com.abcd.hrms.taxbenefits.controller;

import com.abcd.hrms.common.core.dto.ApiResponse;
import com.abcd.hrms.taxbenefits.dto.TaxDeclarationDto;
import com.abcd.hrms.taxbenefits.service.TaxDeclarationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tax/declarations")
@RequiredArgsConstructor
public class TaxDeclarationController {

    private final TaxDeclarationService declarationService;

    @PostMapping
    public ResponseEntity<ApiResponse<TaxDeclarationDto.Response>> submitDeclaration(
            @Valid @RequestBody TaxDeclarationDto.SubmitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(declarationService.submitDeclaration(request)));
    }

    @PostMapping("/{declarationId}/verify")
    public ResponseEntity<ApiResponse<TaxDeclarationDto.Response>> verifyItem(
            @PathVariable UUID declarationId,
            @Valid @RequestBody TaxDeclarationDto.VerifyItemRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(declarationService.verifyItem(declarationId, request)));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<TaxDeclarationDto.Response>> getDeclaration(
            @PathVariable UUID employeeId,
            @RequestParam String financialYear,
            @RequestParam(required = false) UUID companyId) {
        return ResponseEntity.ok(ApiResponse.ok(declarationService.getDeclaration(employeeId, financialYear, companyId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TaxDeclarationDto.Response>>> listDeclarations(
            @RequestParam String financialYear,
            @RequestParam(required = false) UUID companyId) {
        return ResponseEntity.ok(ApiResponse.ok(declarationService.listDeclarations(companyId, financialYear)));
    }
}
