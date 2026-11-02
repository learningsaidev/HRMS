package com.abcd.hrms.taxbenefits.entity;

import com.abcd.hrms.common.core.entity.BaseAuditedEntity;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tax_regimes")
public class TaxRegime extends BaseAuditedEntity {

    @Column(name = "regime_code", nullable = false, length = 30)
    private String regimeCode;

    @Column(name = "regime_name", nullable = false, length = 100)
    private String regimeName;

    @Column(name = "financial_year", nullable = false, length = 20)
    private String financialYear;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    @Column(name = "standard_deduction", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal standardDeduction = new BigDecimal("75000.0000");
}
