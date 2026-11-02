package com.abcd.hrms.workforce.repository;

import com.abcd.hrms.workforce.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID>, JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByEmployeeNumber(String employeeNumber);

    Optional<Employee> findByWorkEmail(String workEmail);

    boolean existsByEmployeeNumber(String employeeNumber);

    boolean existsByWorkEmail(String workEmail);

    Page<Employee> findByCompanyId(UUID companyId, Pageable pageable);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.companyId = :companyId AND e.currentStatus = :status")
    long countByCompanyIdAndCurrentStatus(@Param("companyId") UUID companyId, @Param("status") String status);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.companyId = :companyId")
    long countTotalByCompanyId(@Param("companyId") UUID companyId);
}
