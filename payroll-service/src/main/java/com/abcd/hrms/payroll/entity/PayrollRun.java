package com.abcd.hrms.payroll.entity;

import com.abcd.hrms.common.core.entity.BaseAuditedEntity;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payroll_runs")
public class PayrollRun extends BaseAuditedEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "legal_entity_id", nullable = false)
    private UUID legalEntityId;

    @Column(name = "pay_group_id", nullable = false)
    private UUID payGroupId;

    @Column(name = "run_code", nullable = false, length = 60)
    private String runCode;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "run_type", nullable = false, length = 30)
    @Builder.Default
    private String runType = "REGULAR";

    @Column(name = "total_employees", nullable = false)
    @Builder.Default
    private Integer totalEmployees = 0;

    @Column(name = "total_gross", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal totalGross = BigDecimal.ZERO;

    @Column(name = "total_deductions", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal totalDeductions = BigDecimal.ZERO;

    @Column(name = "total_net", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal totalNet = BigDecimal.ZERO;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "DRAFT";

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "run_initiator_id")
    private UUID runInitiatorId;

    @Column(name = "locked_at")
    private Instant lockedAt;
}
