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
@Table(name = "bgv_check_item")
public class BgvCheckItem extends BaseAuditedEntity {

    @Column(name = "bgv_case_id", nullable = false)
    private UUID bgvCaseId;

    @Column(name = "check_type", nullable = false, length = 50)
    private String checkType; // IDENTITY, ADDRESS, EDUCATION, PREVIOUS_EMPLOYMENT, CRIMINAL_RECORD

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "PENDING"; // PENDING, CLEAR, DISCREPANCY, FAILED

    @Column(name = "discrepancy_notes", length = 500)
    private String discrepancyNotes;
}
