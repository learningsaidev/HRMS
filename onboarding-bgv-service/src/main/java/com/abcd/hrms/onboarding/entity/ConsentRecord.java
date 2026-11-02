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
@Table(name = "consent_record")
public class ConsentRecord extends BaseAuditedEntity {

    @Column(name = "onboarding_case_id", nullable = false)
    private UUID onboardingCaseId;

    @Column(name = "consent_text", nullable = false, columnDefinition = "TEXT")
    private String consentText;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "consented_at", nullable = false)
    @Builder.Default
    private Instant consentedAt = Instant.now();

    @Column(name = "consent_hash", nullable = false, length = 100)
    private String consentHash;
}
