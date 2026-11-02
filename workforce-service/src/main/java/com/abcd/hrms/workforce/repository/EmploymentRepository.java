package com.abcd.hrms.workforce.repository;

import com.abcd.hrms.workforce.entity.Employment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmploymentRepository extends JpaRepository<Employment, UUID> {

    Optional<Employment> findByEmployeeId(UUID employeeId);

    List<Employment> findByDepartmentId(UUID departmentId);

    List<Employment> findByLocationId(UUID locationId);

    List<Employment> findByLegalEntityId(UUID legalEntityId);
}
