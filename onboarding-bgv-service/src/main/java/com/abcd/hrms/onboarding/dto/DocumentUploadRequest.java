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
public class DocumentUploadRequest {

    private UUID onboardingCaseId;

    @NotBlank(message = "Document type is mandatory")
    private String documentType; // IDENTITY_PROOF, ADDRESS_PROOF, EDUCATION_CERTIFICATE, PREVIOUS_PAYSLIP, RELIEVING_LETTER

    @NotBlank(message = "File name is mandatory")
    private String fileName;

    private long fileSize;

    @NotBlank(message = "Storage path is mandatory")
    private String storagePath;

    @NotBlank(message = "MIME type is mandatory")
    private String mimeType;
}
