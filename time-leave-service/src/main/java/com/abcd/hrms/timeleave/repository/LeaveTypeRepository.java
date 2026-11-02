package com.abcd.hrms.timeleave.repository;

import com.abcd.hrms.timeleave.entity.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaveTypeRepository extends JpaRepository<LeaveType, UUID> {

    Optional<LeaveType> findByCode(String code);
}
