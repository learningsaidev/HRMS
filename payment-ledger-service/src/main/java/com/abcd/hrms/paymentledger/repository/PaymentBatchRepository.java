package com.abcd.hrms.paymentledger.repository;

import com.abcd.hrms.paymentledger.entity.PaymentBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentBatchRepository extends JpaRepository<PaymentBatch, UUID> {
    Optional<PaymentBatch> findByCompanyIdAndBatchReference(UUID companyId, String batchReference);
    List<PaymentBatch> findByCompanyIdOrderByCreatedAtDesc(UUID companyId);
    List<PaymentBatch> findByLegalEntityIdOrderByCreatedAtDesc(UUID legalEntityId);
    Optional<PaymentBatch> findBySourceTypeAndSourceId(String sourceType, String sourceId);
}
