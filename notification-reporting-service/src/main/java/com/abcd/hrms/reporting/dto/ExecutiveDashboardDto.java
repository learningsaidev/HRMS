package com.abcd.hrms.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutiveDashboardDto {

    private UUID companyId;
    private String period;
    private Long activeHeadcount;
    private Long openPositions;
    private BigDecimal monthlyPayrollExpenditure;
    private BigDecimal averageSalary;
    private Double leaveUtilizationRate;
    private Map<String, Long> headcountByDepartment;
    private Map<String, BigDecimal> payrollCostByDepartment;
    private List<RecentActivityItem> recentActivities;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentActivityItem {
        private String eventType;
        private String description;
        private String timestamp;
    }
}

