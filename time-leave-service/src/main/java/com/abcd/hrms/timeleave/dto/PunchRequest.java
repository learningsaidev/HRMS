package com.abcd.hrms.timeleave.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PunchRequest {

    @NotNull(message = "Employee ID is mandatory")
    private UUID employeeId;

    @NotBlank(message = "Punch type is mandatory (IN or OUT)")
    private String punchType; // IN, OUT

    private String deviceId;

    private String location;
}
