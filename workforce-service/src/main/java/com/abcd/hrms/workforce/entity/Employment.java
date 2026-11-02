package com.abcd.hrms.workforce.entity;

import com.abcd.hrms.common.core.entity.BaseAuditedEntity;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "employment")
public class Employment extends BaseAuditedEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "legal_entity_id", nullable = false)
    private UUID legalEntityId;

    @Column(name = "department_id", nullable = false)
    private UUID departmentId;

    @Column(name = "location_id", nullable = false)
    private UUID locationId;

    @Column(name = "employment_type", nullable = false, length = 30)
    @Builder.Default
    private String employmentType = "FULL_TIME"; // FULL_TIME, PART_TIME, CONTRACTOR, INTERN

    @Column(name = "designation", nullable = false, length = 100)
    private String designation;

    @Column(name = "grade", length = 20)
    private String grade;

    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;

    @Column(name = "confirmation_date")
    private LocalDate confirmationDate;

    @Column(name = "notice_period_days", nullable = false)
    @Builder.Default
    private int noticePeriodDays = 60;

    @Column(name = "exit_date")
    private LocalDate exitDate;

    @Column(name = "exit_reason", length = 255)
    private String exitReason;
}
