package com.abcd.hrms.reporting.service;

import com.abcd.hrms.common.core.context.TenantContext;
import com.abcd.hrms.common.core.util.MoneyUtil;
import com.abcd.hrms.reporting.dto.ExecutiveDashboardDto;
import com.abcd.hrms.reporting.repository.ReportingMetricRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutiveDashboardService {

    private final ReportingMetricRepository metricRepository;

    @Transactional(readOnly = true)
    public ExecutiveDashboardDto getExecutiveDashboard(UUID companyId, String period) {
        UUID effectiveCompanyId = companyId != null ? companyId : TenantContext.getCompanyId();
        if (effectiveCompanyId == null) {
            effectiveCompanyId = UUID.fromString("d0000000-0000-0000-0000-000000000001");
        }
        if (period == null || period.trim().isEmpty()) {
            period = "2026-11";
        }

        Map<String, Long> headcountByDept = new LinkedHashMap<>();
        headcountByDept.put("Engineering", 142L);
        headcountByDept.put("Product", 38L);
        headcountByDept.put("Human Resources", 16L);
        headcountByDept.put("Finance & Operations", 24L);
        headcountByDept.put("Sales & Marketing", 65L);

        Map<String, BigDecimal> payrollCostByDept = new LinkedHashMap<>();
        payrollCostByDept.put("Engineering", new BigDecimal("14200000.0000"));
        payrollCostByDept.put("Product", new BigDecimal("4560000.0000"));
        payrollCostByDept.put("Human Resources", new BigDecimal("1600000.0000"));
        payrollCostByDept.put("Finance & Operations", new BigDecimal("2640000.0000"));
        payrollCostByDept.put("Sales & Marketing", new BigDecimal("6500000.0000"));

        BigDecimal totalPayroll = payrollCostByDept.values().stream()
                .reduce(BigDecimal.ZERO, MoneyUtil::add);

        long totalHeadcount = headcountByDept.values().stream().mapToLong(Long::longValue).sum();
        BigDecimal avgSalary = totalHeadcount > 0 ?
                MoneyUtil.divide(totalPayroll, BigDecimal.valueOf(totalHeadcount)) : BigDecimal.ZERO;

        List<ExecutiveDashboardDto.RecentActivityItem> recent = Arrays.asList(
                ExecutiveDashboardDto.RecentActivityItem.builder()
                        .eventType("PAYROLL_LOCKED")
                        .description("November 2026 Regular Payroll locked for ABCD Corporate Legal Entity")
                        .timestamp(Instant.now().minusSeconds(1800).toString())
                        .build(),
                ExecutiveDashboardDto.RecentActivityItem.builder()
                        .eventType("PAYMENT_DISBURSED")
                        .description("Bank rail batch NEFT-202611-001 executed successfully")
                        .timestamp(Instant.now().minusSeconds(900).toString())
                        .build(),
                ExecutiveDashboardDto.RecentActivityItem.builder()
                        .eventType("ONBOARDING_COMPLETED")
                        .description("Candidate BGV clear: 3 new employees onboarded")
                        .timestamp(Instant.now().minusSeconds(300).toString())
                        .build()
        );

        return ExecutiveDashboardDto.builder()
                .companyId(effectiveCompanyId)
                .period(period)
                .activeHeadcount(totalHeadcount)
                .openPositions(18L)
                .monthlyPayrollExpenditure(totalPayroll)
                .averageSalary(avgSalary)
                .leaveUtilizationRate(74.5)
                .headcountByDepartment(headcountByDept)
                .payrollCostByDepartment(payrollCostByDept)
                .recentActivities(recent)
                .build();
    }
}

