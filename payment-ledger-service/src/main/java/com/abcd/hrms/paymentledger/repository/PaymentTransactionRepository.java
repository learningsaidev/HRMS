package com.abcd.hrms.paymentledger.repository;

import com.abcd.hrms.paymentledger.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {
    List<PaymentTransaction> findByBatchId(UUID batchId);
    List<PaymentTransaction> findByCompanyIdAndEmployeeIdOrderByCreatedAtDesc(UUID companyId, UUID employeeId);
    List<PaymentTransaction> findByBatchIdAndStatus(UUID batchId, String status);
}
