package com.abcd.hrms.paymentledger.entity;

import com.abcd.hrms.common.core.entity.BaseAuditedEntity;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "journal_entries")
public class JournalEntry extends BaseAuditedEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "legal_entity_id", nullable = false)
    private UUID legalEntityId;

    @Column(name = "entry_number", nullable = false, length = 60)
    private String entryNumber;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "source_module", nullable = false, length = 50)
    private String sourceModule; // PAYROLL, EXPENSES, REIMBURSEMENTS, MANUAL

    @Column(name = "source_reference_id", length = 100)
    private String sourceReferenceId;

    @Column(name = "narration")
    private String narration;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "is_posted", nullable = false)
    @Builder.Default
    private Boolean isPosted = false;

    @Column(name = "posted_at")
    private Instant postedAt;
}

