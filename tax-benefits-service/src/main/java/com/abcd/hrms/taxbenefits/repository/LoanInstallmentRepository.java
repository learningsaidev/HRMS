package com.abcd.hrms.taxbenefits.repository;

import com.abcd.hrms.taxbenefits.entity.LoanInstallment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface LoanInstallmentRepository extends JpaRepository<LoanInstallment, UUID> {
    List<LoanInstallment> findByLoanIdOrderByInstallmentNumberAsc(UUID loanId);
    List<LoanInstallment> findByStatusAndDueDateLessThanEqual(String status, LocalDate maxDueDate);
    void deleteByLoanId(UUID loanId);
}
