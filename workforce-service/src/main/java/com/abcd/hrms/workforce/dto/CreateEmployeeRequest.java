package com.abcd.hrms.workforce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEmployeeRequest {

    private UUID companyId;

    private String employeeNumber; // If null, auto-generated

    @NotBlank(message = "First name is mandatory")
    private String firstName;

    private String middleName;

    @NotBlank(message = "Last name is mandatory")
    private String lastName;

    @NotNull(message = "Date of birth is mandatory")
    private LocalDate dateOfBirth;

    private String gender;

    private String maritalStatus;

    private String personalEmail;

    @NotBlank(message = "Work email is mandatory")
    @Email(message = "Invalid work email")
    private String workEmail;

    private String phoneNumber;

    private String nationalId; // Will be AES-256-GCM encrypted and masked

    private String bankAccountNumber; // Will be AES-256-GCM encrypted and masked

    private String bankName;

    private String bankIfscOrRouting;

    @NotNull(message = "Legal entity is mandatory")
    private UUID legalEntityId;

    @NotNull(message = "Department is mandatory")
    private UUID departmentId;

    @NotNull(message = "Location is mandatory")
    private UUID locationId;

    private String employmentType; // Default: FULL_TIME

    @NotBlank(message = "Designation is mandatory")
    private String designation;

    private String grade;

    @NotNull(message = "Joining date is mandatory")
    private LocalDate joiningDate;

    private Integer noticePeriodDays;

    private UUID managerEmployeeId;
}
