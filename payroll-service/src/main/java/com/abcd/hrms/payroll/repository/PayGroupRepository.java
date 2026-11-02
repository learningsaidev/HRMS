package com.abcd.hrms.payroll.repository;

import com.abcd.hrms.payroll.entity.PayGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayGroupRepository extends JpaRepository<PayGroup, UUID> {
    Optional<PayGroup> findByCompanyIdAndGroupCode(UUID companyId, String groupCode);
    List<PayGroup> findByCompanyIdAndIsActiveTrue(UUID companyId);
    List<PayGroup> findByLegalEntityIdAndIsActiveTrue(UUID legalEntityId);
}

