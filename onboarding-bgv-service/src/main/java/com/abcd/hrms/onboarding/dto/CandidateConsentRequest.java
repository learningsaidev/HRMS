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
public class CandidateConsentRequest {

    @NotBlank(message = "Secure token is required")
    private String secureToken;

    @NotBlank(message = "Consent acknowledgment is required")
    private String consentText;
}

