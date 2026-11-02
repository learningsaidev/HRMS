package com.abcd.hrms.paymentledger.repository;

import com.abcd.hrms.paymentledger.entity.BankReconciliation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BankReconciliationRepository extends JpaRepository<BankReconciliation, UUID> {
    Optional<BankReconciliation> findByCompanyIdAndBankStatementReference(UUID companyId, String bankStatementReference);
    List<BankReconciliation> findByCompanyIdOrderByTransactionDateDesc(UUID companyId);
}
