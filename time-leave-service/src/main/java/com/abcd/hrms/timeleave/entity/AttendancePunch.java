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
@Table(name = "attendance_punch")
public class AttendancePunch extends BaseAuditedEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "punch_time", nullable = false)
    private Instant punchTime;

    @Column(name = "punch_type", nullable = false, length = 10)
    private String punchType; // IN, OUT

    @Column(name = "device_id", length = 50)
    private String deviceId;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "verification_mode", length = 30)
    @Builder.Default
    private String verificationMode = "WEB_PORTAL";
}
