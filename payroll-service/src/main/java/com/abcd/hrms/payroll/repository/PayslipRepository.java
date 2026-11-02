package com.abcd.hrms.payroll.repository;

import com.abcd.hrms.payroll.entity.Payslip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayslipRepository extends JpaRepository<Payslip, UUID> {
    List<Payslip> findByPayrollRunId(UUID payrollRunId);
    List<Payslip> findByCompanyIdAndEmployeeIdOrderByCreatedAtDesc(UUID companyId, UUID employeeId);
    Optional<Payslip> findByPayrollRunIdAndEmployeeId(UUID payrollRunId, UUID employeeId);
}
