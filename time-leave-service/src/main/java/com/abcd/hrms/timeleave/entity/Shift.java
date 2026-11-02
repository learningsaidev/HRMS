package com.abcd.hrms.timeleave.entity;

import com.abcd.hrms.common.core.entity.BaseAuditedEntity;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "shift")
public class Shift extends BaseAuditedEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "grace_period_minutes", nullable = false)
    @Builder.Default
    private int gracePeriodMinutes = 15;

    @Column(name = "half_day_hours", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal halfDayHours = BigDecimal.valueOf(4.00);

    @Column(name = "full_day_hours", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal fullDayHours = BigDecimal.valueOf(8.00);
}
