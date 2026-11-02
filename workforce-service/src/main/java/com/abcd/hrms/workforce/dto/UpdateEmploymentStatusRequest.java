package com.abcd.hrms.workforce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmploymentStatusRequest {

    @NotBlank(message = "New status is mandatory")
    private String newStatus; // ACTIVE, PROBATION, CONFIRMED, ON_LEAVE, NOTICE_PERIOD, SUSPENDED, TERMINATED, RETIRED

    private LocalDate effectiveDate;

    private String reason;
}
