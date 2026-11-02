package com.abcd.hrms.payroll.repository;

import com.abcd.hrms.payroll.entity.SalaryComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SalaryComponentRepository extends JpaRepository<SalaryComponent, UUID> {
    Optional<SalaryComponent> findByCompanyIdAndComponentCode(UUID companyId, String componentCode);
    List<SalaryComponent> findByCompanyIdAndIsActiveTrueOrderByDisplayOrderAsc(UUID companyId);
}
