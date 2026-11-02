package com.abcd.hrms.payroll.entity;

import com.abcd.hrms.common.core.entity.BaseAuditedEntity;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payslips")
public class Payslip extends BaseAuditedEntity {

    @Column(name = "payroll_run_id", nullable = false)
    private UUID payrollRunId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "pay_group_id", nullable = false)
    private UUID payGroupId;

    @Column(name = "days_in_month", nullable = false)
    @Builder.Default
    private Integer daysInMonth = 30;

    @Column(name = "paid_days", nullable = false, precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal paidDays = BigDecimal.valueOf(30.00);

    @Column(name = "lop_days", nullable = false, precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal lopDays = BigDecimal.ZERO;

    @Column(name = "gross_earnings", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal grossEarnings = BigDecimal.ZERO;

    @Column(name = "total_deductions", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal totalDeductions = BigDecimal.ZERO;

    @Column(name = "net_pay", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal netPay = BigDecimal.ZERO;

    @Column(name = "payment_status", nullable = false, length = 30)
    @Builder.Default
    private String paymentStatus = "PENDING"; // PENDING, PROCESSING, PAID, FAILED

    @Column(name = "pdf_storage_path", length = 500)
    private String pdfStoragePath;
}

