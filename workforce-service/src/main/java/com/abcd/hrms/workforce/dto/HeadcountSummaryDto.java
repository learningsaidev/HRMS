package com.abcd.hrms.workforce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeadcountSummaryDto {

    private long totalHeadcount;

    private long activeCount;

    private long probationCount;

    private long confirmedCount;

    private long onLeaveCount;

    private long noticePeriodCount;

    private long terminatedCount;

    private long preboardingCount;

    private Map<String, Long> headcountByLegalEntity;

    private Map<String, Long> headcountByDepartment;

    private Map<String, Long> headcountByLocation;

    private Map<String, Long> headcountByEmploymentType;
}
