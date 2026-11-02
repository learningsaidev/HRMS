package com.abcd.hrms.taxbenefits.repository;

import com.abcd.hrms.taxbenefits.entity.LoanMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LoanMasterRepository extends JpaRepository<LoanMaster, UUID> {
    List<LoanMaster> findByCompanyIdAndEmployeeIdOrderByCreatedAtDesc(UUID companyId, UUID employeeId);
    List<LoanMaster> findByCompanyIdAndStatus(UUID companyId, String status);
    List<LoanMaster> findByStatus(String status);
}
