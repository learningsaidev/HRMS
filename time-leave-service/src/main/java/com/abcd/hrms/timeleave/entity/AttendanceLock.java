package com.abcd.hrms.timeleave.entity;

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
@Table(name = "attendance_lock")
public class AttendanceLock extends BaseAuditedEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "period_year", nullable = false)
    private int periodYear;

    @Column(name = "period_month", nullable = false)
    private int periodMonth;

    @Column(name = "is_locked", nullable = false)
    @Builder.Default
    private boolean isLocked = true;

    @Column(name = "locked_by", nullable = false, length = 100)
    private String lockedBy;

    @Column(name = "locked_at", nullable = false)
    @Builder.Default
    private Instant lockedAt = Instant.now();
}
