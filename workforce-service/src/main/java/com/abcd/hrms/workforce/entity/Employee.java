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
@Table(name = "employee")
public class Employee extends BaseAuditedEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "employee_number", nullable = false, unique = true, length = 50)
    private String employeeNumber;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "display_name", nullable = false, length = 255)
    private String displayName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "gender", length = 20)
    private String gender;

    @Column(name = "marital_status", length = 20)
    private String maritalStatus;

    @Column(name = "personal_email", length = 255)
    private String personalEmail;

    @Column(name = "work_email", nullable = false, unique = true, length = 255)
    private String workEmail;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Column(name = "national_id_encrypted", columnDefinition = "TEXT")
    private String nationalIdEncrypted;

    @Column(name = "national_id_masked", length = 50)
    private String nationalIdMasked;

    @Column(name = "bank_account_encrypted", columnDefinition = "TEXT")
    private String bankAccountEncrypted;

    @Column(name = "bank_account_masked", length = 50)
    private String bankAccountMasked;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "bank_ifsc_or_routing", length = 50)
    private String bankIfscOrRouting;

    @Column(name = "current_status", nullable = false, length = 30)
    @Builder.Default
    private String currentStatus = "ACTIVE"; // DRAFT, PREBOARDING, ACTIVE, PROBATION, CONFIRMED, ON_LEAVE, NOTICE_PERIOD, SUSPENDED, TERMINATED, RETIRED

    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Employment employment;
}
