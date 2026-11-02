package com.abcd.hrms.workforce.repository;

import com.abcd.hrms.workforce.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    List<Department> findByCompanyId(UUID companyId);

    List<Department> findByLegalEntityId(UUID legalEntityId);

    Optional<Department> findByCode(String code);
}
