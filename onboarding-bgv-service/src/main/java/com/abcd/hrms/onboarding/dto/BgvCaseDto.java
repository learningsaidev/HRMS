package com.abcd.hrms.onboarding.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BgvCaseDto {

    private UUID id;

    private UUID onboardingCaseId;

    private String vendorName;

    private String vendorReferenceId;

    private String status;

    private String manualReviewerNotes;

    private UUID decidedBy;

    private Instant decidedAt;

    private List<BgvCheckItemDto> checkItems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BgvCheckItemDto {
        private UUID id;
        private String checkType;
        private String status;
        private String discrepancyNotes;
    }
}
