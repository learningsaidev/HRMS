package com.abcd.hrms.payroll.service;

import com.abcd.hrms.common.core.context.TenantContext;
import com.abcd.hrms.common.core.exception.BusinessValidationException;
import com.abcd.hrms.common.core.exception.ConflictException;
import com.abcd.hrms.common.core.exception.EntityNotFoundException;
import com.abcd.hrms.common.core.util.MoneyUtil;
import com.abcd.hrms.common.messaging.KafkaTopicConstants;
import com.abcd.hrms.common.messaging.event.CanonicalEvent;
import com.abcd.hrms.common.messaging.outbox.OutboxPublisherService;
import com.abcd.hrms.payroll.dto.PayrollRunDto;
import com.abcd.hrms.payroll.dto.PayslipDto;
import com.abcd.hrms.payroll.entity.*;
import com.abcd.hrms.payroll.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayrollRunService {

    private final PayrollRunRepository payrollRunRepository;
    private final PayslipRepository payslipRepository;
    private final CalculationLineRepository calculationLineRepository;
    private final EmployeeCompensationRepository employeeCompensationRepository;
    private final CompensationComponentRepository compensationComponentRepository;
    private final SalaryComponentRepository salaryComponentRepository;
    private final PayrollCalculationEngine calculationEngine;
    private final OutboxPublisherService outboxPublisherService;

    @Transactional
    public PayrollRunDto.Response initiateRun(PayrollRunDto.InitiateRequest request) {
        UUID companyId = request.getCompanyId() != null ? request.getCompanyId() : TenantContext.getCompanyId();
        if (companyId == null) {
            companyId = UUID.fromString("d0000000-0000-0000-0000-000000000001");
        }

        payrollRunRepository.findByPayGroupIdAndPeriodStartAndPeriodEnd(
                request.getPayGroupId(), request.getPeriodStart(), request.getPeriodEnd())
                .ifPresent(existing -> {
                    throw new ConflictException("PayrollRun", "payGroupId & period " + request.getPayGroupId());
                });

        String runCode = "RUN-" + request.getPeriodStart().format(DateTimeFormatter.ofPattern("yyyyMM"))
                + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        PayrollRun run = PayrollRun.builder()
                .companyId(companyId)
                .legalEntityId(request.getLegalEntityId())
                .payGroupId(request.getPayGroupId())
                .runCode(runCode)
                .periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd())
                .runType(request.getRunType() != null ? request.getRunType() : "REGULAR")
                .status("DRAFT")
                .runInitiatorId(TenantContext.getUserId())
                .build();

        PayrollRun saved = payrollRunRepository.save(run);
        return mapToRunResponse(saved);
    }

    @Transactional
    public PayrollRunDto.Response calculateRun(UUID runId) {
        PayrollRun run = payrollRunRepository.findById(runId)
                .orElseThrow(() -> new EntityNotFoundException("PayrollRun", runId));

        if ("LOCKED".equalsIgnoreCase(run.getStatus()) || "DISBURSED".equalsIgnoreCase(run.getStatus())) {
            throw new BusinessValidationException("Cannot recalculate a locked or disbursed payroll run", "ERR_RUN_ALREADY_LOCKED");
        }

        run.setStatus("IN_CALCULATION");
        payrollRunRepository.save(run);

        // Fetch components for lookup
        Map<UUID, SalaryComponent> componentMap = salaryComponentRepository.findAll().stream()
                .collect(Collectors.toMap(SalaryComponent::getId, c -> c));

        // Fetch active employees assigned to this pay group
        List<EmployeeCompensation> compensations = employeeCompensationRepository.findByPayGroupIdAndIsActiveTrue(run.getPayGroupId());

        // Clean up prior calculated payslips for idempotent re-run
        List<Payslip> existingPayslips = payslipRepository.findByPayrollRunId(runId);
        for (Payslip p : existingPayslips) {
            calculationLineRepository.deleteByPayslipId(p.getId());
        }
        payslipRepository.deleteAll(existingPayslips);

        BigDecimal runTotalGross = BigDecimal.ZERO;
        BigDecimal runTotalDeductions = BigDecimal.ZERO;
        BigDecimal runTotalNet = BigDecimal.ZERO;

        for (EmployeeCompensation comp : compensations) {
            List<CompensationComponent> compItems = compensationComponentRepository.findByCompensationId(comp.getId());

            PayrollCalculationEngine.EmployeeCalculationResult result = calculationEngine.calculateEmployeePayroll(
                    run, comp, compItems, componentMap, null, null
            );

            Payslip savedSlip = payslipRepository.save(result.getPayslip());
            for (CalculationLine line : result.getLines()) {
                line.setPayslipId(savedSlip.getId());
                calculationLineRepository.save(line);
            }

            runTotalGross = MoneyUtil.add(runTotalGross, savedSlip.getGrossEarnings());
            runTotalDeductions = MoneyUtil.add(runTotalDeductions, savedSlip.getTotalDeductions());
            runTotalNet = MoneyUtil.add(runTotalNet, savedSlip.getNetPay());
        }

        run.setTotalEmployees(compensations.size());
        run.setTotalGross(runTotalGross);
        run.setTotalDeductions(runTotalDeductions);
        run.setTotalNet(runTotalNet);
        run.setStatus("CALCULATED");
        PayrollRun updated = payrollRunRepository.save(run);

        // Publish PayrollRunCalculated.v1
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("payrollRunId", updated.getId());
        eventData.put("runCode", updated.getRunCode());
        eventData.put("totalEmployees", updated.getTotalEmployees());
        eventData.put("totalGross", updated.getTotalGross().toString());
        eventData.put("totalNet", updated.getTotalNet().toString());

        CanonicalEvent<Map<String, Object>> event = CanonicalEvent.of(
                "PayrollRunCalculated.v1",
                "PAYROLL_RUN",
                updated.getId().toString(),
                updated.getCompanyId(),
                updated.getLegalEntityId(),
                "payroll-service",
                TenantContext.getCorrelationId(),
                eventData
        );

        outboxPublisherService.enqueueEvent(
                KafkaTopicConstants.TOPIC_PAYROLL_EVENTS,
                "PAYROLL_RUN",
                updated.getId().toString(),
                event
        );

        return mapToRunResponse(updated);
    }

    @Transactional
    public PayrollRunDto.Response reviewRun(UUID runId) {
        PayrollRun run = payrollRunRepository.findById(runId)
                .orElseThrow(() -> new EntityNotFoundException("PayrollRun", runId));

        if (!"CALCULATED".equalsIgnoreCase(run.getStatus())) {
            throw new BusinessValidationException("Only CALCULATED runs can be marked as REVIEWED", "ERR_INVALID_STATUS_TRANSITION");
        }

        run.setStatus("REVIEWED");
        return mapToRunResponse(payrollRunRepository.save(run));
    }

    @Transactional
    public PayrollRunDto.Response lockRun(UUID runId) {
        PayrollRun run = payrollRunRepository.findById(runId)
                .orElseThrow(() -> new EntityNotFoundException("PayrollRun", runId));

        if (!"CALCULATED".equalsIgnoreCase(run.getStatus()) && !"REVIEWED".equalsIgnoreCase(run.getStatus())) {
            throw new BusinessValidationException("Run must be CALCULATED or REVIEWED to lock", "ERR_INVALID_STATUS_TRANSITION");
        }

        run.setStatus("LOCKED");
        run.setLockedAt(Instant.now());
        run.setApprovedBy(TenantContext.getUserId());
        PayrollRun lockedRun = payrollRunRepository.save(run);

        // Publish PayrollLocked.v1 to Kafka for payment-ledger-service and double entry journal ingestion
        Map<String, Object> payload = new HashMap<>();
        payload.put("payrollRunId", lockedRun.getId());
        payload.put("companyId", lockedRun.getCompanyId());
        payload.put("legalEntityId", lockedRun.getLegalEntityId());
        payload.put("payGroupId", lockedRun.getPayGroupId());
        payload.put("runCode", lockedRun.getRunCode());
        payload.put("periodStart", lockedRun.getPeriodStart().toString());
        payload.put("periodEnd", lockedRun.getPeriodEnd().toString());
        payload.put("totalEmployees", lockedRun.getTotalEmployees());
        payload.put("totalGross", lockedRun.getTotalGross().toString());
        payload.put("totalDeductions", lockedRun.getTotalDeductions().toString());
        payload.put("totalNet", lockedRun.getTotalNet().toString());
        payload.put("lockedAt", lockedRun.getLockedAt().toString());

        CanonicalEvent<Map<String, Object>> event = CanonicalEvent.of(
                "PayrollLocked.v1",
                "PAYROLL_RUN",
                lockedRun.getId().toString(),
                lockedRun.getCompanyId(),
                lockedRun.getLegalEntityId(),
                "payroll-service",
                TenantContext.getCorrelationId(),
                payload
        );

        outboxPublisherService.enqueueEvent(
                KafkaTopicConstants.TOPIC_PAYROLL_EVENTS,
                "PAYROLL_RUN",
                lockedRun.getId().toString(),
                event
        );

        return mapToRunResponse(lockedRun);
    }

    @Transactional(readOnly = true)
    public PayrollRunDto.Response getRunById(UUID runId) {
        return payrollRunRepository.findById(runId)
                .map(this::mapToRunResponse)
                .orElseThrow(() -> new EntityNotFoundException("PayrollRun", runId));
    }

    @Transactional(readOnly = true)
    public List<PayrollRunDto.Response> listRuns(UUID companyId) {
        UUID effectiveCompanyId = companyId != null ? companyId : TenantContext.getCompanyId();
        if (effectiveCompanyId == null) {
            effectiveCompanyId = UUID.fromString("d0000000-0000-0000-0000-000000000001");
        }
        return payrollRunRepository.findByCompanyIdOrderByPeriodStartDesc(effectiveCompanyId).stream()
                .map(this::mapToRunResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PayslipDto.Response> getPayslipsByRun(UUID runId) {
        return payslipRepository.findByPayrollRunId(runId).stream()
                .map(this::mapToPayslipResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PayslipDto.Response> getPayslipsForEmployee(UUID employeeId, UUID companyId) {
        UUID effectiveCompanyId = companyId != null ? companyId : TenantContext.getCompanyId();
        if (effectiveCompanyId == null) {
            effectiveCompanyId = UUID.fromString("d0000000-0000-0000-0000-000000000001");
        }
        return payslipRepository.findByCompanyIdAndEmployeeIdOrderByCreatedAtDesc(effectiveCompanyId, employeeId).stream()
                .map(this::mapToPayslipResponse)
                .collect(Collectors.toList());
    }

    private PayrollRunDto.Response mapToRunResponse(PayrollRun r) {
        return PayrollRunDto.Response.builder()
                .id(r.getId())
                .companyId(r.getCompanyId())
                .legalEntityId(r.getLegalEntityId())
                .payGroupId(r.getPayGroupId())
                .runCode(r.getRunCode())
                .periodStart(r.getPeriodStart())
                .periodEnd(r.getPeriodEnd())
                .runType(r.getRunType())
                .totalEmployees(r.getTotalEmployees())
                .totalGross(r.getTotalGross())
                .totalDeductions(r.getTotalDeductions())
                .totalNet(r.getTotalNet())
                .status(r.getStatus())
                .createdBy(r.getCreatedBy())
                .approvedBy(r.getApprovedBy())
                .lockedAt(r.getLockedAt())
                .createdAt(r.getCreatedAt())
                .build();
    }

    private PayslipDto.Response mapToPayslipResponse(Payslip p) {
        List<CalculationLine> lines = calculationLineRepository.findByPayslipId(p.getId());
        List<PayslipDto.LineItem> lineDtos = lines.stream()
                .map(l -> PayslipDto.LineItem.builder()
                        .id(l.getId())
                        .componentId(l.getComponentId())
                        .componentCode(l.getComponentCode())
                        .componentName(l.getComponentName())
                        .componentType(l.getComponentType())
                        .amount(l.getAmount())
                        .ytdAmount(l.getYtdAmount())
                        .isTaxable(l.getIsTaxable())
                        .isStatutory(l.getIsStatutory())
                        .build())
                .collect(Collectors.toList());

        return PayslipDto.Response.builder()
                .id(p.getId())
                .payrollRunId(p.getPayrollRunId())
                .employeeId(p.getEmployeeId())
                .companyId(p.getCompanyId())
                .payGroupId(p.getPayGroupId())
                .daysInMonth(p.getDaysInMonth())
                .paidDays(p.getPaidDays())
                .lopDays(p.getLopDays())
                .grossEarnings(p.getGrossEarnings())
                .totalDeductions(p.getTotalDeductions())
                .netPay(p.getNetPay())
                .paymentStatus(p.getPaymentStatus())
                .pdfStoragePath(p.getPdfStoragePath())
                .createdAt(p.getCreatedAt())
                .lines(lineDtos)
                .build();
    }
}
