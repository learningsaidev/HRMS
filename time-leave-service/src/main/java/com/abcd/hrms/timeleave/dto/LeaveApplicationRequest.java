package com.abcd.hrms.timeleave.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveApplicationRequest {

    @NotNull(message = "Employee ID is mandatory")
    private UUID employeeId;

    @NotNull(message = "Leave type is mandatory")
    private UUID leaveTypeId;

    @NotNull(message = "Start date is mandatory")
    private LocalDate startDate;

    @NotNull(message = "End date is mandatory")
    private LocalDate endDate;

    private boolean isHalfDay;

    @NotBlank(message = "Reason is mandatory")
    private String reason;
}
