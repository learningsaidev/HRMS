package com.abcd.hrms.onboarding.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCandidateCaseRequest {

    private UUID companyId;

    @NotBlank(message = "Candidate name is mandatory")
    private String candidateName;

    @NotBlank(message = "Candidate email is mandatory")
    @Email(message = "Invalid email format")
    private String candidateEmail;

    private String phoneNumber;

    @NotNull(message = "Target legal entity is mandatory")
    private UUID targetLegalEntityId;

    @NotNull(message = "Target department is mandatory")
    private UUID targetDepartmentId;

    @NotNull(message = "Target location is mandatory")
    private UUID targetLocationId;

    @NotBlank(message = "Proposed designation is mandatory")
    private String proposedDesignation;

    @NotNull(message = "Proposed joining date is mandatory")
    private LocalDate proposedJoiningDate;
}

