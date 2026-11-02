package com.abcd.hrms.paymentledger.entity;

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
@Table(name = "bank_reconciliations")
public class BankReconciliation extends BaseAuditedEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "bank_statement_reference", nullable = false, length = 100)
    private String bankStatementReference;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "statement_amount", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal statementAmount = BigDecimal.ZERO;

    @Column(name = "matched_transaction_id")
    private UUID matchedTransactionId;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "MATCHED"; // MATCHED, UNMATCHED, DISCREPANCY
}
