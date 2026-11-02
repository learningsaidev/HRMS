package com.abcd.hrms.onboarding.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingCaseDto {

    private UUID id;

    private UUID companyId;

    private String candidateName;

    private String candidateEmail;

    private String phoneNumber;

    private UUID targetLegalEntityId;

    private UUID targetDepartmentId;

    private UUID targetLocationId;

    private String proposedDesignation;

    private LocalDate proposedJoiningDate;

    private String secureToken;

    private String status;

    private Instant createdAt;

    private Instant updatedAt;
}
