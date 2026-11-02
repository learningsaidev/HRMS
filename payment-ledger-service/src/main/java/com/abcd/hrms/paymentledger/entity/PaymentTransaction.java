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
@Table(name = "payment_transactions")
public class PaymentTransaction extends BaseAuditedEntity {

    @Column(name = "batch_id", nullable = false)
    private UUID batchId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "beneficiary_name", nullable = false, length = 150)
    private String beneficiaryName;

    @Column(name = "account_number_encrypted", nullable = false, length = 255)
    private String accountNumberEncrypted;

    @Column(name = "ifsc_code", nullable = false, length = 20)
    private String ifscCode;

    @Column(name = "amount", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "utr_number", length = 100)
    private String utrNumber;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "PENDING"; // PENDING, IN_TRANSIT, SUCCESS, FAILED, RETURNED

    @Column(name = "failure_reason", length = 500)
    private String failureReason;
}
