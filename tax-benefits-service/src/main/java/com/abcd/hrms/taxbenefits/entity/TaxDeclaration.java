package com.abcd.hrms.taxbenefits.entity;

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
@Table(name = "tax_declarations")
public class TaxDeclaration extends BaseAuditedEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "financial_year", nullable = false, length = 20)
    private String financialYear;

    @Column(name = "regime_code", nullable = false, length = 30)
    @Builder.Default
    private String regimeCode = "NEW";

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "DRAFT"; // DRAFT, SUBMITTED, VERIFIED, REJECTED

    @Column(name = "total_declared_deductions", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal totalDeclaredDeductions = BigDecimal.ZERO;

    @Column(name = "total_verified_deductions", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal totalVerifiedDeductions = BigDecimal.ZERO;
}
