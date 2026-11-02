package com.abcd.hrms.onboarding.controller;

import com.abcd.hrms.common.core.dto.ApiResponse;
import com.abcd.hrms.onboarding.dto.CandidateConsentRequest;
import com.abcd.hrms.onboarding.dto.CreateCandidateCaseRequest;
import com.abcd.hrms.onboarding.dto.DocumentUploadRequest;
import com.abcd.hrms.onboarding.dto.OnboardingCaseDto;
import com.abcd.hrms.onboarding.entity.CandidateDocument;
import com.abcd.hrms.onboarding.service.OnboardingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;

    @PostMapping("/cases")
    public ResponseEntity<ApiResponse<OnboardingCaseDto>> createCandidateCase(
            @Valid @RequestBody CreateCandidateCaseRequest request) {
        OnboardingCaseDto dto = onboardingService.createCandidateCase(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(dto));
    }

    @GetMapping("/cases/{id}")
    public ResponseEntity<ApiResponse<OnboardingCaseDto>> getCaseById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(onboardingService.getCaseById(id)));
    }

    @GetMapping("/portal/token/{token}")
    public ResponseEntity<ApiResponse<OnboardingCaseDto>> getCaseByToken(@PathVariable String token) {
        return ResponseEntity.ok(ApiResponse.ok(onboardingService.getCaseByToken(token)));
    }

    @GetMapping("/cases")
    public ResponseEntity<ApiResponse<List<OnboardingCaseDto>>> listCases(
            @RequestParam(required = false) UUID companyId) {
        return ResponseEntity.ok(ApiResponse.ok(onboardingService.listCases(companyId)));
    }

    @PostMapping("/portal/consent")
    public ResponseEntity<ApiResponse<OnboardingCaseDto>> submitConsent(
            @Valid @RequestBody CandidateConsentRequest request,
            HttpServletRequest servletRequest) {
        String ip = servletRequest.getRemoteAddr();
        String ua = servletRequest.getHeader("User-Agent");
        return ResponseEntity.ok(ApiResponse.ok(onboardingService.submitConsent(request, ip, ua)));
    }

    @PostMapping("/documents")
    public ResponseEntity<ApiResponse<CandidateDocument>> uploadDocument(
            @Valid @RequestBody DocumentUploadRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(onboardingService.uploadDocument(request)));
    }

    @GetMapping("/cases/{id}/documents")
    public ResponseEntity<ApiResponse<List<CandidateDocument>>> getDocuments(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(onboardingService.getDocuments(id)));
    }
}
