package com.abcd.hrms.payroll.repository;

import com.abcd.hrms.payroll.entity.CompensationComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CompensationComponentRepository extends JpaRepository<CompensationComponent, UUID> {
    List<CompensationComponent> findByCompensationId(UUID compensationId);
    void deleteByCompensationId(UUID compensationId);
}

