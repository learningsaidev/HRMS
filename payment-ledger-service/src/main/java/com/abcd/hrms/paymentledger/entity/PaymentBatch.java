package com.abcd.hrms.paymentledger.entity;

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
@Table(name = "payment_batches")
public class PaymentBatch extends BaseAuditedEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "legal_entity_id", nullable = false)
    private UUID legalEntityId;

    @Column(name = "batch_reference", nullable = false, length = 60)
    private String batchReference;

    @Column(name = "source_type", nullable = false, length = 40)
    @Builder.Default
    private String sourceType = "PAYROLL";

    @Column(name = "source_id", length = 100)
    private String sourceId;

    @Column(name = "payment_method", nullable = false, length = 30)
    @Builder.Default
    private String paymentMethod = "NEFT"; // NEFT, RTGS, IMPS, NACH

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "INR";

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "total_transactions", nullable = false)
    @Builder.Default
    private Integer totalTransactions = 0;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "PENDING"; // PENDING, APPROVED, PROCESSING, COMPLETED, PARTIALLY_FAILED
}

