package com.abcd.hrms.payroll.entity;

import com.abcd.hrms.common.core.entity.BaseAuditedEntity;
import lombok.*;

import javax.persistence.*;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pay_groups")
public class PayGroup extends BaseAuditedEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "legal_entity_id", nullable = false)
    private UUID legalEntityId;

    @Column(name = "group_code", nullable = false, length = 50)
    private String groupCode;

    @Column(name = "group_name", nullable = false, length = 100)
    private String groupName;

    @Column(name = "pay_frequency", nullable = false, length = 30)
    @Builder.Default
    private String payFrequency = "MONTHLY";

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "INR";

    @Column(name = "salary_disbursement_day", nullable = false)
    @Builder.Default
    private Integer salaryDisbursementDay = 30;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
