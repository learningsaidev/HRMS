package com.abcd.hrms.payroll.repository;

import com.abcd.hrms.payroll.entity.EmployeeCompensation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeCompensationRepository extends JpaRepository<EmployeeCompensation, UUID> {
    Optional<EmployeeCompensation> findByCompanyIdAndEmployeeIdAndIsActiveTrue(UUID companyId, UUID employeeId);
    List<EmployeeCompensation> findByPayGroupIdAndIsActiveTrue(UUID payGroupId);
    List<EmployeeCompensation> findByCompanyIdAndIsActiveTrue(UUID companyId);
}
