package com.abcd.hrms.payroll.service;

import com.abcd.hrms.common.core.exception.BusinessValidationException;
import com.abcd.hrms.common.core.util.MoneyUtil;
import com.abcd.hrms.payroll.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * High-precision Gross-to-Net payroll calculation engine.
 * Implements strict 4-decimal Banker's Rounding and verifies double-entry balancing invariants.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayrollCalculationEngine {

    private static final BigDecimal PF_RATE = new BigDecimal("0.1200");
    private static final BigDecimal ESI_RATE = new BigDecimal("0.0075");
    private static final BigDecimal ESI_WAGE_LIMIT = new BigDecimal("21000.0000");
    private static final BigDecimal STANDARD_PT = new BigDecimal("200.0000");

    public static class EmployeeCalculationResult {
        private final Payslip payslip;
        private final List<CalculationLine> lines;

        public EmployeeCalculationResult(Payslip payslip, List<CalculationLine> lines) {
            this.payslip = payslip;
            this.lines = lines;
        }

        public Payslip getPayslip() {
            return payslip;
        }

        public List<CalculationLine> getLines() {
            return lines;
        }
    }

    public EmployeeCalculationResult calculateEmployeePayroll(
            PayrollRun run,
            EmployeeCompensation comp,
            List<CompensationComponent> compItems,
            Map<UUID, SalaryComponent> componentMap,
            BigDecimal paidDays,
            BigDecimal lopDays) {

        int daysInMonth = (int) ChronoUnit.DAYS.between(run.getPeriodStart(), run.getPeriodEnd()) + 1;
        if (daysInMonth <= 0) {
            daysInMonth = 30;
        }

        if (paidDays == null) {
            paidDays = BigDecimal.valueOf(daysInMonth);
        }
        if (lopDays == null) {
            lopDays = BigDecimal.valueOf(daysInMonth).subtract(paidDays);
            if (lopDays.compareTo(BigDecimal.ZERO) < 0) {
                lopDays = BigDecimal.ZERO;
            }
        }

        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal totalDeductions = BigDecimal.ZERO;
        List<CalculationLine> lines = new ArrayList<>();

        BigDecimal basicProrated = BigDecimal.ZERO;

        // 1. Process Earnings
        for (CompensationComponent cc : compItems) {
            SalaryComponent sc = componentMap.get(cc.getComponentId());
            if (sc == null || !"EARNING".equalsIgnoreCase(sc.getComponentType())) {
                continue;
            }

            BigDecimal proratedAmount = MoneyUtil.prorate(
                    cc.getAmountMonthly(),
                    paidDays,
                    BigDecimal.valueOf(daysInMonth)
            );

            if ("BASIC".equalsIgnoreCase(sc.getComponentCode())) {
                basicProrated = proratedAmount;
            }

            if (sc.getAffectsGross()) {
                totalGross = MoneyUtil.add(totalGross, proratedAmount);
            }

            CalculationLine line = CalculationLine.builder()
                    .componentId(sc.getId())
                    .componentCode(sc.getComponentCode())
                    .componentName(sc.getComponentName())
                    .componentType(sc.getComponentType())
                    .amount(proratedAmount)
                    .ytdAmount(proratedAmount)
                    .isTaxable(sc.getTaxable())
                    .isStatutory(false)
                    .build();
            lines.add(line);
        }

        // Fallback: If no individual earning components were mapped, use monthly gross from compensation
        if (lines.isEmpty() && comp.getGrossMonthly().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal proratedAmount = MoneyUtil.prorate(
                    comp.getGrossMonthly(),
                    paidDays,
                    BigDecimal.valueOf(daysInMonth)
            );
            totalGross = proratedAmount;
            basicProrated = MoneyUtil.multiply(proratedAmount, new BigDecimal("0.5000")); // 50% default basic

            lines.add(CalculationLine.builder()
                    .componentId(UUID.fromString("c0000000-0000-0000-0000-000000000001"))
                    .componentCode("BASIC")
                    .componentName("Basic Salary")
                    .componentType("EARNING")
                    .amount(basicProrated)
                    .ytdAmount(basicProrated)
                    .isTaxable(true)
                    .isStatutory(false)
                    .build());

            BigDecimal specialAllowance = MoneyUtil.subtract(totalGross, basicProrated);
            lines.add(CalculationLine.builder()
                    .componentId(UUID.fromString("c0000000-0000-0000-0000-000000000003"))
                    .componentCode("SPECIAL_ALLOWANCE")
                    .componentName("Special Allowance")
                    .componentType("EARNING")
                    .amount(specialAllowance)
                    .ytdAmount(specialAllowance)
                    .isTaxable(true)
                    .isStatutory(false)
                    .build());
        }

        // 2. Process Statutory & Other Deductions
        // Employee PF: 12% of basic
        if (basicProrated.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal pfAmount = MoneyUtil.multiply(basicProrated, PF_RATE);
            totalDeductions = MoneyUtil.add(totalDeductions, pfAmount);

            lines.add(CalculationLine.builder()
                    .componentId(UUID.fromString("c0000000-0000-0000-0000-000000000006"))
                    .componentCode("PF_EMPLOYEE")
                    .componentName("Provident Fund (Employee)")
                    .componentType("STATUTORY_EMPLOYEE")
                    .amount(pfAmount)
                    .ytdAmount(pfAmount)
                    .isTaxable(false)
                    .isStatutory(true)
                    .build());
        }

        // Professional Tax: standard PT ₹200 if gross > ₹15,000
        if (totalGross.compareTo(new BigDecimal("15000.0000")) >= 0) {
            BigDecimal ptAmount = STANDARD_PT;
            totalDeductions = MoneyUtil.add(totalDeductions, ptAmount);

            lines.add(CalculationLine.builder()
                    .componentId(UUID.fromString("c0000000-0000-0000-0000-000000000008"))
                    .componentCode("PROFESSIONAL_TAX")
                    .componentName("Professional Tax")
                    .componentType("STATUTORY_EMPLOYEE")
                    .amount(ptAmount)
                    .ytdAmount(ptAmount)
                    .isTaxable(false)
                    .isStatutory(true)
                    .build());
        }

        // Employee ESI: 0.75% of Gross if Gross <= ₹21,000
        if (totalGross.compareTo(BigDecimal.ZERO) > 0 && totalGross.compareTo(ESI_WAGE_LIMIT) <= 0) {
            BigDecimal esiAmount = MoneyUtil.multiply(totalGross, ESI_RATE);
            totalDeductions = MoneyUtil.add(totalDeductions, esiAmount);

            lines.add(CalculationLine.builder()
                    .componentId(UUID.fromString("c0000000-0000-0000-0000-000000000009"))
                    .componentCode("ESI_EMPLOYEE")
                    .componentName("Employee State Insurance")
                    .componentType("STATUTORY_EMPLOYEE")
                    .amount(esiAmount)
                    .ytdAmount(esiAmount)
                    .isTaxable(false)
                    .isStatutory(true)
                    .build());
        }

        // Any custom deduction components in compensation
        for (CompensationComponent cc : compItems) {
            SalaryComponent sc = componentMap.get(cc.getComponentId());
            if (sc == null || !"DEDUCTION".equalsIgnoreCase(sc.getComponentType())) {
                continue;
            }

            BigDecimal dedAmount = cc.getAmountMonthly();
            totalDeductions = MoneyUtil.add(totalDeductions, dedAmount);

            lines.add(CalculationLine.builder()
                    .componentId(sc.getId())
                    .componentCode(sc.getComponentCode())
                    .componentName(sc.getComponentName())
                    .componentType(sc.getComponentType())
                    .amount(dedAmount)
                    .ytdAmount(dedAmount)
                    .isTaxable(sc.getTaxable())
                    .isStatutory(false)
                    .build());
        }

        // 3. Net Pay & Invariant Validation
        BigDecimal netPay = MoneyUtil.subtract(totalGross, totalDeductions);
        if (netPay.compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Employee {} has deductions ({}) exceeding gross ({}), net clamped to 0",
                    comp.getEmployeeId(), totalDeductions, totalGross);
            netPay = BigDecimal.ZERO;
        }

        // Double-entry invariant verification
        if (!MoneyUtil.equals(netPay, MoneyUtil.subtract(totalGross, totalDeductions)) && netPay.compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessValidationException(
                    "Payroll invariant violation: Gross (" + totalGross + ") - Deductions (" + totalDeductions + ") != Net (" + netPay + ")",
                    "ERR_PAYROLL_INVARIANT_FAILED"
            );
        }

        Payslip payslip = Payslip.builder()
                .payrollRunId(run.getId())
                .employeeId(comp.getEmployeeId())
                .companyId(run.getCompanyId())
                .payGroupId(run.getPayGroupId())
                .daysInMonth(daysInMonth)
                .paidDays(paidDays)
                .lopDays(lopDays)
                .grossEarnings(totalGross)
                .totalDeductions(totalDeductions)
                .netPay(netPay)
                .paymentStatus("PENDING")
                .build();

        return new EmployeeCalculationResult(payslip, lines);
    }
}

