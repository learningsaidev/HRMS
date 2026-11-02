package com.abcd.hrms.timeleave.entity;

import com.abcd.hrms.common.core.entity.BaseAuditedEntity;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
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
@Table(name = "daily_attendance")
public class DailyAttendance extends BaseAuditedEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "shift_id")
    private UUID shiftId;

    @Column(name = "first_punch_in")
    private Instant firstPunchIn;

    @Column(name = "last_punch_out")
    private Instant lastPunchOut;

    @Column(name = "total_worked_hours", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal totalWorkedHours = BigDecimal.ZERO;

    @Column(name = "overtime_hours", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal overtimeHours = BigDecimal.ZERO;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "PRESENT"; // PRESENT, ABSENT, HALF_DAY, ON_LEAVE, WEEKLY_OFF, HOLIDAY

    @Column(name = "is_regularized", nullable = false)
    @Builder.Default
    private boolean isRegularized = false;

    @Column(name = "regularization_reason", length = 255)
    private String regularizationReason;
}
