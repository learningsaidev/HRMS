package com.abcd.hrms.workforce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrgNodeDto {

    private UUID employeeId;

    private String employeeNumber;

    private String name;

    private String designation;

    private String department;

    private String workEmail;

    private String status;

    @Builder.Default
    private List<OrgNodeDto> directReports = new ArrayList<>();
}
