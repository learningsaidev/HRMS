package com.abcd.hrms.onboarding.entity;

import com.abcd.hrms.common.core.entity.BaseAuditedEntity;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "onboarding_case")
public class OnboardingCase extends BaseAuditedEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "candidate_name", nullable = false, length = 150)
    private String candidateName;

    @Column(name = "candidate_email", nullable = false, length = 255)
    private String candidateEmail;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Column(name = "target_legal_entity_id", nullable = false)
    private UUID targetLegalEntityId;

    @Column(name = "target_department_id", nullable = false)
    private UUID targetDepartmentId;

    @Column(name = "target_location_id", nullable = false)
    private UUID targetLocationId;

    @Column(name = "proposed_designation", nullable = false, length = 100)
    private String proposedDesignation;

    @Column(name = "proposed_joining_date", nullable = false)
    private LocalDate proposedJoiningDate;

    @Column(name = "secure_token", nullable = false, unique = true, length = 100)
    private String secureToken;

    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private String status = "PREBOARDING"; // PREBOARDING, LINK_SENT, DETAILS_SUBMITTED, CONSENT_GRANTED, BGV_INITIATED, BGV_CLEAR, BGV_REVIEW_REQUIRED, ONBOARDING_COMPLETED, CANCELLED
}
