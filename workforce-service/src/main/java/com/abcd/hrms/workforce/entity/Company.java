package com.abcd.hrms.workforce.entity;

import com.abcd.hrms.common.core.entity.BaseAuditedEntity;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "company")
public class Company extends BaseAuditedEntity {

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "legal_name", nullable = false, length = 255)
    private String legalName;

    @Column(name = "tax_identifier", length = 100)
    private String taxIdentifier;

    @Column(name = "country_code", nullable = false, length = 3)
    @Builder.Default
    private String countryCode = "IND";

    @Column(name = "base_currency", nullable = false, length = 3)
    @Builder.Default
    private String baseCurrency = "INR";

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "ACTIVE";
}
