package com.abcd.hrms.payroll.entity;

import com.abcd.hrms.common.core.entity.BaseAuditedEntity;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "calculation_lines")
public class CalculationLine extends BaseAuditedEntity {

    @Column(name = "payslip_id", nullable = false)
    private UUID payslipId;

    @Column(name = "component_id", nullable = false)
    private UUID componentId;

    @Column(name = "component_code", nullable = false, length = 50)
    private String componentCode;

    @Column(name = "component_name", nullable = false, length = 100)
    private String componentName;

    @Column(name = "component_type", nullable = false, length = 40)
    private String componentType;

    @Column(name = "amount", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "ytd_amount", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal ytdAmount = BigDecimal.ZERO;

    @Column(name = "is_taxable", nullable = false)
    @Builder.Default
    private Boolean isTaxable = true;

    @Column(name = "is_statutory", nullable = false)
    @Builder.Default
    private Boolean isStatutory = false;
}
