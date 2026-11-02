package com.abcd.hrms.onboarding.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BgvReviewRequest {

    @NotBlank(message = "Review status is mandatory (CLEAR, DISCREPANCY, FAILED)")
    private String status; // CLEAR, DISCREPANCY, FAILED

    private String reviewerNotes;

    private UUID reviewerId;
}

