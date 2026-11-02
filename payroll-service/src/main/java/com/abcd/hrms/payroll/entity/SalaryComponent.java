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
@Table(name = "salary_components")
public class SalaryComponent extends BaseAuditedEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "component_code", nullable = false, length = 50)
    private String componentCode;

    @Column(name = "component_name", nullable = false, length = 100)
    private String componentName;

    @Column(name = "component_type", nullable = false, length = 40)
    private String componentType; // EARNING, DEDUCTION, STATUTORY_EMPLOYEE, STATUTORY_EMPLOYER, REIMBURSEMENT

    @Column(name = "calculation_type", nullable = false, length = 30)
    @Builder.Default
    private String calculationType = "FIXED"; // FIXED, PERCENTAGE_OF, FORMULA

    @Column(name = "taxable", nullable = false)
    @Builder.Default
    private Boolean taxable = true;

    @Column(name = "affects_gross", nullable = false)
    @Builder.Default
    private Boolean affectsGross = true;

    @Column(name = "affects_net", nullable = false)
    @Builder.Default
    private Boolean affectsNet = true;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
