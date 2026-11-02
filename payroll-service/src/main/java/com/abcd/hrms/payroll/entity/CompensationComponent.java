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
@Table(name = "compensation_components")
public class CompensationComponent extends BaseAuditedEntity {

    @Column(name = "compensation_id", nullable = false)
    private UUID compensationId;

    @Column(name = "component_id", nullable = false)
    private UUID componentId;

    @Column(name = "amount_monthly", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal amountMonthly = BigDecimal.ZERO;

    @Column(name = "percentage_value", precision = 7, scale = 4)
    private BigDecimal percentageValue;
}
