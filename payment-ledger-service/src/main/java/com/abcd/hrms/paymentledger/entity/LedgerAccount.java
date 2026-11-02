package com.abcd.hrms.paymentledger.entity;

import com.abcd.hrms.common.core.entity.BaseAuditedEntity;
import lombok.*;

import javax.persistence.*;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ledger_accounts")
public class LedgerAccount extends BaseAuditedEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "account_code", nullable = false, length = 50)
    private String accountCode;

    @Column(name = "account_name", nullable = false, length = 150)
    private String accountName;

    @Column(name = "account_type", nullable = false, length = 30)
    private String accountType; // ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "INR";

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
