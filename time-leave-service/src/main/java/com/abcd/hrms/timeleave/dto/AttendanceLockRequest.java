package com.abcd.hrms.timeleave.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceLockRequest {

    private UUID companyId;

    @NotNull(message = "Year is mandatory")
    private Integer year;

    @NotNull(message = "Month is mandatory")
    @Min(1)
    @Max(12)
    private Integer month;
}
