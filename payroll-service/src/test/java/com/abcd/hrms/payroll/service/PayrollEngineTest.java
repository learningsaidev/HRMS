package com.abcd.hrms.payroll.service;

import com.abcd.hrms.common.core.util.MoneyUtil;
import com.abcd.hrms.payroll.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Payroll Calculation Engine & Double-Entry Invariant Tests")
class PayrollEngineTest {

    private PayrollCalculationEngine calculationEngine;
    private PayrollRun payrollRun;
    private UUID companyId;
    private UUID payGroupId;
    private UUID employeeId;

    private UUID basicCompId;
    private UUID hraCompId;
    private UUID saCompId;

    private Map<UUID, SalaryComponent> componentMap;

    @BeforeEach
    void setUp() {
        calculationEngine = new PayrollCalculationEngine();
        companyId = UUID.randomUUID();
        payGroupId = UUID.randomUUID();
        employeeId = UUID.randomUUID();

        payrollRun = PayrollRun.builder()
                .companyId(companyId)
                .legalEntityId(UUID.randomUUID())
                .payGroupId(payGroupId)
                .runCode("RUN-202611-001")
                .periodStart(LocalDate.of(2026, 11, 1))
                .periodEnd(LocalDate.of(2026, 11, 30)) // 30 days
                .status("DRAFT")
                .build();
        payrollRun.setId(UUID.randomUUID());

        basicCompId = UUID.randomUUID();
        hraCompId = UUID.randomUUID();
        saCompId = UUID.randomUUID();

        componentMap = new HashMap<>();
        componentMap.put(basicCompId, SalaryComponent.builder()
                .companyId(companyId)
                .componentCode("BASIC")
                .componentName("Basic Salary")
                .componentType("EARNING")
                .calculationType("FIXED")
                .affectsGross(true)
                .taxable(true)
                .build());
        componentMap.put(hraCompId, SalaryComponent.builder()
                .companyId(companyId)
                .componentCode("HRA")
                .componentName("House Rent Allowance")
                .componentType("EARNING")
                .calculationType("FIXED")
                .affectsGross(true)
                .taxable(true)
                .build());
        componentMap.put(saCompId, SalaryComponent.builder()
                .companyId(companyId)
                .componentCode("SPECIAL_ALLOWANCE")
                .componentName("Special Allowance")
                .componentType("EARNING")
                .calculationType("FIXED")
                .affectsGross(true)
                .taxable(true)
                .build());
    }

    @Test
    @DisplayName("Verify full-month Gross-to-Net and Double-Entry Invariant")
    void testFullMonthGrossToNet() {
        EmployeeCompensation comp = EmployeeCompensation.builder()
                .companyId(companyId)
                .employeeId(employeeId)
                .payGroupId(payGroupId)
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .ctcAnnual(new BigDecimal("1200000.0000"))
                .grossMonthly(new BigDecimal("100000.0000"))
                .build();

        List<CompensationComponent> compItems = Arrays.asList(
                CompensationComponent.builder().componentId(basicCompId).amountMonthly(new BigDecimal("50000.0000")).build(),
                CompensationComponent.builder().componentId(hraCompId).amountMonthly(new BigDecimal("25000.0000")).build(),
                CompensationComponent.builder().componentId(saCompId).amountMonthly(new BigDecimal("25000.0000")).build()
        );

        PayrollCalculationEngine.EmployeeCalculationResult result = calculationEngine.calculateEmployeePayroll(
                payrollRun, comp, compItems, componentMap, BigDecimal.valueOf(30), BigDecimal.ZERO
        );

        Payslip payslip = result.getPayslip();
        assertNotNull(payslip);

        // Gross = 50,000 + 25,000 + 25,000 = 100,000
        assertEquals(0, new BigDecimal("100000.0000").compareTo(payslip.getGrossEarnings()));

        // PF = 12% of 50,000 = 6,000.0000
        // PT = 200.0000
        // Total Deductions = 6,200.0000
        assertEquals(0, new BigDecimal("6200.0000").compareTo(payslip.getTotalDeductions()));

        // Net Pay = 100,000 - 6,200 = 93,800.0000
        assertEquals(0, new BigDecimal("93800.0000").compareTo(payslip.getNetPay()));

        // Double-entry balancing invariant: Gross - Total Deductions == Net Pay
        BigDecimal expectedNet = MoneyUtil.subtract(payslip.getGrossEarnings(), payslip.getTotalDeductions());
        assertTrue(MoneyUtil.equals(expectedNet, payslip.getNetPay()));
    }

    @Test
    @DisplayName("Verify Proration with 15 Days LOP (Loss of Pay)")
    void testProrationWithLop() {
        EmployeeCompensation comp = EmployeeCompensation.builder()
                .companyId(companyId)
                .employeeId(employeeId)
                .payGroupId(payGroupId)
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .ctcAnnual(new BigDecimal("1200000.0000"))
                .grossMonthly(new BigDecimal("100000.0000"))
                .build();

        List<CompensationComponent> compItems = Arrays.asList(
                CompensationComponent.builder().componentId(basicCompId).amountMonthly(new BigDecimal("50000.0000")).build(),
                CompensationComponent.builder().componentId(hraCompId).amountMonthly(new BigDecimal("25000.0000")).build(),
                CompensationComponent.builder().componentId(saCompId).amountMonthly(new BigDecimal("25000.0000")).build()
        );

        // 15 days worked out of 30 days
        PayrollCalculationEngine.EmployeeCalculationResult result = calculationEngine.calculateEmployeePayroll(
                payrollRun, comp, compItems, componentMap, BigDecimal.valueOf(15), BigDecimal.valueOf(15)
        );

        Payslip payslip = result.getPayslip();
        assertEquals(0, new BigDecimal("50000.0000").compareTo(payslip.getGrossEarnings()));

        // Basic prorated = 25,000 -> PF = 12% of 25,000 = 3,000.0000
        // PT = 200.0000
        // Deductions = 3,200.0000
        assertEquals(0, new BigDecimal("3200.0000").compareTo(payslip.getTotalDeductions()));

        // Net Pay = 50,000 - 3,200 = 46,800.0000
        assertEquals(0, new BigDecimal("46800.0000").compareTo(payslip.getNetPay()));
        assertTrue(MoneyUtil.equals(payslip.getNetPay(), MoneyUtil.subtract(payslip.getGrossEarnings(), payslip.getTotalDeductions())));
    }

    @Test
    @DisplayName("Verify ESI deduction for Gross <= 21,000")
    void testEsiDeductionApplicability() {
        EmployeeCompensation comp = EmployeeCompensation.builder()
                .companyId(companyId)
                .employeeId(employeeId)
                .payGroupId(payGroupId)
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .grossMonthly(new BigDecimal("20000.0000"))
                .build();

        List<CompensationComponent> compItems = Arrays.asList(
                CompensationComponent.builder().componentId(basicCompId).amountMonthly(new BigDecimal("10000.0000")).build(),
                CompensationComponent.builder().componentId(saCompId).amountMonthly(new BigDecimal("10000.0000")).build()
        );

        PayrollCalculationEngine.EmployeeCalculationResult result = calculationEngine.calculateEmployeePayroll(
                payrollRun, comp, compItems, componentMap, BigDecimal.valueOf(30), BigDecimal.ZERO
        );

        Payslip payslip = result.getPayslip();
        assertEquals(0, new BigDecimal("20000.0000").compareTo(payslip.getGrossEarnings()));

        // Basic = 10,000 -> PF = 1,200.0000
        // PT = 200.0000
        // ESI = 0.75% of 20,000 = 150.0000
        // Total deductions = 1,200 + 200 + 150 = 1,550.0000
        assertEquals(0, new BigDecimal("1550.0000").compareTo(payslip.getTotalDeductions()));
        assertEquals(0, new BigDecimal("18450.0000").compareTo(payslip.getNetPay()));

        // Verify calculation lines include ESI
        boolean hasEsiLine = result.getLines().stream()
                .anyMatch(l -> "ESI_EMPLOYEE".equals(l.getComponentCode()) && l.getAmount().compareTo(new BigDecimal("150.0000")) == 0);
        assertTrue(hasEsiLine);
    }
}
