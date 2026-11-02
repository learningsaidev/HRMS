package com.abcd.hrms.workforce.entity;

import com.abcd.hrms.common.core.entity.BaseAuditedEntity;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "legal_entity")
public class LegalEntity extends BaseAuditedEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "entity_code", nullable = false, unique = true, length = 50)
    private String entityCode;

    @Column(name = "legal_name", nullable = false, length = 255)
    private String legalName;

    @Column(name = "registration_number", length = 100)
    private String registrationNumber;

    @Column(name = "tax_identifier", length = 100)
    private String taxIdentifier;

    @Column(name = "country_code", nullable = false, length = 3)
    private String countryCode;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "ACTIVE";
}
