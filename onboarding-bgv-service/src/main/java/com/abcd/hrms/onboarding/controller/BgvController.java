package com.abcd.hrms.onboarding.controller;

import com.abcd.hrms.common.core.dto.ApiResponse;
import com.abcd.hrms.onboarding.dto.BgvCaseDto;
import com.abcd.hrms.onboarding.dto.BgvReviewRequest;
import com.abcd.hrms.onboarding.service.OnboardingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bgv")
@RequiredArgsConstructor
public class BgvController {

    private final OnboardingService onboardingService;

    @PostMapping("/initiate/{onboardingCaseId}")
    public ResponseEntity<ApiResponse<BgvCaseDto>> initiateBgv(@PathVariable UUID onboardingCaseId) {
        BgvCaseDto dto = onboardingService.initiateBgv(onboardingCaseId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(dto));
    }

    @PostMapping("/cases/{bgvCaseId}/review")
    public ResponseEntity<ApiResponse<BgvCaseDto>> reviewBgv(
            @PathVariable UUID bgvCaseId,
            @Valid @RequestBody BgvReviewRequest request) {
        BgvCaseDto dto = onboardingService.reviewBgvCase(bgvCaseId, request);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @GetMapping("/cases/by-onboarding/{onboardingCaseId}")
    public ResponseEntity<ApiResponse<BgvCaseDto>> getBgvByOnboardingCase(@PathVariable UUID onboardingCaseId) {
        return ResponseEntity.ok(ApiResponse.ok(onboardingService.getBgvCase(onboardingCaseId)));
    }
}
