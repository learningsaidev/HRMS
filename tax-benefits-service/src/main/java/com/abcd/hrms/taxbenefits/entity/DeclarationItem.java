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
@Table(name = "declaration_items")
public class DeclarationItem extends BaseAuditedEntity {

    @Column(name = "declaration_id", nullable = false)
    private UUID declarationId;

    @Column(name = "section_code", nullable = false, length = 50)
    private String sectionCode; // 80C, 80D, 80E, 24B, HRA

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "declared_amount", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal declaredAmount = BigDecimal.ZERO;

    @Column(name = "verified_amount", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal verifiedAmount = BigDecimal.ZERO;

    @Column(name = "proof_storage_path", length = 500)
    private String proofStoragePath;

    @Column(name = "verification_status", nullable = false, length = 30)
    @Builder.Default
    private String verificationStatus = "PENDING"; // PENDING, APPROVED, REJECTED

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;
}

