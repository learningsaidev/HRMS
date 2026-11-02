package com.abcd.hrms.workforce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDto {

    private UUID id;

    private UUID companyId;

    private String employeeNumber;

    private String firstName;

    private String middleName;

    private String lastName;

    private String displayName;

    private LocalDate dateOfBirth;

    private String gender;

    private String maritalStatus;

    private String personalEmail;

    private String workEmail;

    private String phoneNumber;

    private String nationalIdMasked;

    private String bankAccountMasked;

    private String bankName;

    private String bankIfscOrRouting;

    private String currentStatus;

    // Employment details
    private UUID employmentId;

    private UUID legalEntityId;

    private String legalEntityName;

    private UUID departmentId;

    private String departmentName;

    private UUID locationId;

    private String locationName;

    private String employmentType;

    private String designation;

    private String grade;

    private LocalDate joiningDate;

    private LocalDate confirmationDate;

    private int noticePeriodDays;

    private LocalDate exitDate;

    private String exitReason;

    // Manager
    private UUID managerEmployeeId;

    private String managerName;

    private Instant createdAt;

    private Instant updatedAt;
}
