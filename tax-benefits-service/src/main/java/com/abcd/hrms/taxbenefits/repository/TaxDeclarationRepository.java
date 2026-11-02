package com.abcd.hrms.taxbenefits.repository;

import com.abcd.hrms.taxbenefits.entity.TaxDeclaration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaxDeclarationRepository extends JpaRepository<TaxDeclaration, UUID> {
    Optional<TaxDeclaration> findByCompanyIdAndEmployeeIdAndFinancialYear(UUID companyId, UUID employeeId, String financialYear);
    List<TaxDeclaration> findByCompanyIdAndFinancialYear(UUID companyId, String financialYear);
    List<TaxDeclaration> findByEmployeeIdOrderByCreatedAtDesc(UUID employeeId);
}

