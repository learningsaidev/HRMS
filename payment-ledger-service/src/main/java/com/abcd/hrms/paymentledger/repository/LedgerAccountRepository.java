package com.abcd.hrms.paymentledger.repository;

import com.abcd.hrms.paymentledger.entity.LedgerAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LedgerAccountRepository extends JpaRepository<LedgerAccount, UUID> {
    Optional<LedgerAccount> findByCompanyIdAndAccountCode(UUID companyId, String accountCode);
    List<LedgerAccount> findByCompanyIdAndIsActiveTrue(UUID companyId);
}
