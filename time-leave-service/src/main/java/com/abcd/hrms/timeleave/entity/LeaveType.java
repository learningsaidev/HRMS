package com.abcd.hrms.timeleave.entity;

import com.abcd.hrms.common.core.entity.BaseAuditedEntity;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "leave_type")
public class LeaveType extends BaseAuditedEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "is_paid", nullable = false)
    @Builder.Default
    private boolean isPaid = true;

    @Column(name = "max_days_per_year", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal maxDaysPerYear = BigDecimal.valueOf(12.00);

    @Column(name = "can_carry_forward", nullable = false)
    @Builder.Default
    private boolean canCarryForward = true;

    @Column(name = "is_encashable", nullable = false)
    @Builder.Default
    private boolean isEncashable = false;
}
