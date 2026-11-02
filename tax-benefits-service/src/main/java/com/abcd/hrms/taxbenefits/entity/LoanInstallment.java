package com.abcd.hrms.taxbenefits.entity;

import com.abcd.hrms.common.core.entity.BaseAuditedEntity;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "loan_installments")
public class LoanInstallment extends BaseAuditedEntity {

    @Column(name = "loan_id", nullable = false)
    private UUID loanId;

    @Column(name = "installment_number", nullable = false)
    private Integer installmentNumber;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "emi_amount", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal emiAmount = BigDecimal.ZERO;

    @Column(name = "principal_component", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal principalComponent = BigDecimal.ZERO;

    @Column(name = "interest_component", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal interestComponent = BigDecimal.ZERO;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "PENDING"; // PENDING, DEDUCTED, WAIVED

    @Column(name = "payroll_run_id")
    private UUID payrollRunId;
}

