package com.abcd.hrms.payroll.service;

import com.abcd.hrms.common.messaging.outbox.OutboxPublisherService;
import com.abcd.hrms.payroll.dto.PayrollRunDto;
import com.abcd.hrms.payroll.entity.*;
import com.abcd.hrms.payroll.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PayrollRunService Orchestration & Locking Tests")
class PayrollRunServiceTest {

    @Mock
    private PayrollRunRepository payrollRunRepository;
    @Mock
    private PayslipRepository payslipRepository;
    @Mock
    private CalculationLineRepository calculationLineRepository;
    @Mock
    private EmployeeCompensationRepository employeeCompensationRepository;
    @Mock
    private CompensationComponentRepository compensationComponentRepository;
    @Mock
    private SalaryComponentRepository salaryComponentRepository;
    @Mock
    private PayrollCalculationEngine calculationEngine;
    @Mock
    private OutboxPublisherService outboxPublisherService;

    @InjectMocks
    private PayrollRunService payrollRunService;

    private UUID runId;
    private UUID companyId;
    private UUID payGroupId;

    @BeforeEach
    void setUp() {
        runId = UUID.randomUUID();
        companyId = UUID.randomUUID();
        payGroupId = UUID.randomUUID();
    }

    @Test
    void testInitiateRun() {
        PayrollRunDto.InitiateRequest request = new PayrollRunDto.InitiateRequest();
        request.setCompanyId(companyId);
        request.setLegalEntityId(UUID.randomUUID());
        request.setPayGroupId(payGroupId);
        request.setPeriodStart(LocalDate.of(2026, 11, 1));
        request.setPeriodEnd(LocalDate.of(2026, 11, 30));

        when(payrollRunRepository.findByPayGroupIdAndPeriodStartAndPeriodEnd(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(payrollRunRepository.save(any(PayrollRun.class))).thenAnswer(i -> {
            PayrollRun r = i.getArgument(0);
            r.setId(runId);
            return r;
        });

        PayrollRunDto.Response response = payrollRunService.initiateRun(request);
        assertNotNull(response);
        assertEquals("DRAFT", response.getStatus());
        assertNotNull(response.getRunCode());
    }

    @Test
    void testLockRunPublishesOutboxEvent() {
        PayrollRun run = PayrollRun.builder()
                .companyId(companyId)
                .legalEntityId(UUID.randomUUID())
                .payGroupId(payGroupId)
                .runCode("RUN-202611-001")
                .periodStart(LocalDate.of(2026, 11, 1))
                .periodEnd(LocalDate.of(2026, 11, 30))
                .totalEmployees(5)
                .totalGross(new BigDecimal("500000.0000"))
                .totalDeductions(new BigDecimal("31000.0000"))
                .totalNet(new BigDecimal("469000.0000"))
                .status("CALCULATED")
                .build();
        run.setId(runId);

        when(payrollRunRepository.findById(runId)).thenReturn(Optional.of(run));
        when(payrollRunRepository.save(any(PayrollRun.class))).thenAnswer(i -> i.getArgument(0));

        PayrollRunDto.Response locked = payrollRunService.lockRun(runId);
        assertEquals("LOCKED", locked.getStatus());
        assertNotNull(locked.getLockedAt());

        // Verify Kafka Transactional Outbox event is enqueued
        verify(outboxPublisherService, times(1)).enqueueEvent(any(), any(), any(), any());
    }
}

