package com.abcd.hrms.timeleave.entity;

import com.abcd.hrms.common.core.entity.BaseAuditedEntity;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "leave_ledger")
public class LeaveLedger extends BaseAuditedEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "leave_type_id", nullable = false)
    private UUID leaveTypeId;

    @Column(name = "transaction_units", nullable = false, precision = 6, scale = 2)
    private BigDecimal transactionUnits; // Positive for credit, negative for debit

    @Column(name = "entry_type", nullable = false, length = 30)
    private String entryType; // OPENING, ACCRUAL, CONSUMPTION, LAPSE, ENCASHMENT, ADJUSTMENT

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "reference_id", length = 100)
    private String referenceId;

    @Column(name = "balance_after", nullable = false, precision = 6, scale = 2)
    private BigDecimal balanceAfter;
}
