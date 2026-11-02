package com.abcd.hrms.timeleave.repository;

import com.abcd.hrms.timeleave.entity.AttendanceLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceLockRepository extends JpaRepository<AttendanceLock, UUID> {

    Optional<AttendanceLock> findByCompanyIdAndPeriodYearAndPeriodMonth(UUID companyId, int year, int month);

    boolean existsByCompanyIdAndPeriodYearAndPeriodMonthAndIsLockedTrue(UUID companyId, int year, int month);
}
