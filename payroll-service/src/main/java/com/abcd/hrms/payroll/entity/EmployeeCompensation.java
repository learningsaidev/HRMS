package com.abcd.hrms.payroll.entity;

import com.abcd.hrms.common.core.entity.BaseAuditedEntity;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "employee_compensations")
public class EmployeeCompensation extends BaseAuditedEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "pay_group_id", nullable = false)
    private UUID payGroupId;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "ctc_annual", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal ctcAnnual = BigDecimal.ZERO;

    @Column(name = "gross_monthly", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal grossMonthly = BigDecimal.ZERO;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
