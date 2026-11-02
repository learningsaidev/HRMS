package com.abcd.hrms.onboarding.entity;

import com.abcd.hrms.common.core.entity.BaseAuditedEntity;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "candidate_document")
public class CandidateDocument extends BaseAuditedEntity {

    @Column(name = "onboarding_case_id", nullable = false)
    private UUID onboardingCaseId;

    @Column(name = "document_type", nullable = false, length = 50)
    private String documentType; // IDENTITY_PROOF, ADDRESS_PROOF, EDUCATION_CERTIFICATE, PREVIOUS_PAYSLIP, RELIEVING_LETTER

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "storage_path", nullable = false, length = 500)
    private String storagePath;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "verification_status", nullable = false, length = 30)
    @Builder.Default
    private String verificationStatus = "PENDING"; // PENDING, VERIFIED, REJECTED
}
