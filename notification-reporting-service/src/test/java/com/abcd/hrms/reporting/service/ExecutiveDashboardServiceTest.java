package com.abcd.hrms.reporting.service;

import com.abcd.hrms.reporting.dto.ExecutiveDashboardDto;
import com.abcd.hrms.reporting.repository.ReportingMetricRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExecutiveDashboardService Analytics Rollup Tests")
class ExecutiveDashboardServiceTest {

    @Mock
    private ReportingMetricRepository metricRepository;

    @InjectMocks
    private ExecutiveDashboardService dashboardService;

    private UUID companyId;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
    }

    @Test
    void testGetExecutiveDashboard() {
        ExecutiveDashboardDto dto = dashboardService.getExecutiveDashboard(companyId, "2026-11");
        assertNotNull(dto);
        assertEquals(companyId, dto.getCompanyId());
        assertEquals("2026-11", dto.getPeriod());
        assertTrue(dto.getActiveHeadcount() > 0);
        assertTrue(dto.getMonthlyPayrollExpenditure().compareTo(BigDecimal.ZERO) > 0);
        assertNotNull(dto.getHeadcountByDepartment());
        assertFalse(dto.getHeadcountByDepartment().isEmpty());
        assertNotNull(dto.getPayrollCostByDepartment());
        assertFalse(dto.getPayrollCostByDepartment().isEmpty());
        assertNotNull(dto.getRecentActivities());
        assertFalse(dto.getRecentActivities().isEmpty());
    }
}

