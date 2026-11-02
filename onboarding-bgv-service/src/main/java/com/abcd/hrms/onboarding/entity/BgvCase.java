package com.abcd.hrms.onboarding.entity;

import com.abcd.hrms.common.core.entity.BaseAuditedEntity;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bgv_case")
public class BgvCase extends BaseAuditedEntity {

    @Column(name = "onboarding_case_id", nullable = false)
    private UUID onboardingCaseId;

    @Column(name = "vendor_name", nullable = false, length = 100)
    @Builder.Default
    private String vendorName = "ABCD_INTERNAL_REVIEW";

    @Column(name = "vendor_reference_id", length = 100)
    private String vendorReferenceId;

    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private String status = "NOT_STARTED"; // NOT_STARTED, INITIATED, IN_PROGRESS, CLEAR, REVIEW_REQUIRED, DISCREPANCY, FAILED, CANCELLED

    @Column(name = "manual_reviewer_notes", columnDefinition = "TEXT")
    private String manualReviewerNotes;

    @Column(name = "decided_by")
    private UUID decidedBy;

    @Column(name = "decided_at")
    private Instant decidedAt;
}
