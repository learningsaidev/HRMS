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
@Table(name = "loan_masters")
public class LoanMaster extends BaseAuditedEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "loan_type", nullable = false, length = 50)
    @Builder.Default
    private String loanType = "SALARY_ADVANCE"; // SALARY_ADVANCE, HOME_LOAN, PERSONAL_LOAN

    @Column(name = "principal_amount", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal principalAmount = BigDecimal.ZERO;

    @Column(name = "interest_rate", nullable = false, precision = 7, scale = 4)
    @Builder.Default
    private BigDecimal interestRate = BigDecimal.ZERO;

    @Column(name = "tenure_months", nullable = false)
    @Builder.Default
    private Integer tenureMonths = 12;

    @Column(name = "monthly_emi", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal monthlyEmi = BigDecimal.ZERO;

    @Column(name = "disbursement_date")
    private LocalDate disbursementDate;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "PENDING"; // PENDING, APPROVED, ACTIVE, CLOSED, REJECTED

    @Column(name = "total_paid", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal totalPaid = BigDecimal.ZERO;

    @Column(name = "balance_principal", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal balancePrincipal = BigDecimal.ZERO;
}
