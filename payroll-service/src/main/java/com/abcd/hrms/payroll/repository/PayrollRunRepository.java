package com.abcd.hrms.payroll.repository;

import com.abcd.hrms.payroll.entity.PayrollRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayrollRunRepository extends JpaRepository<PayrollRun, UUID> {
    Optional<PayrollRun> findByCompanyIdAndRunCode(UUID companyId, String runCode);
    List<PayrollRun> findByCompanyIdOrderByPeriodStartDesc(UUID companyId);
    List<PayrollRun> findByLegalEntityIdOrderByPeriodStartDesc(UUID legalEntityId);
    Optional<PayrollRun> findByPayGroupIdAndPeriodStartAndPeriodEnd(UUID payGroupId, LocalDate periodStart, LocalDate periodEnd);
}
