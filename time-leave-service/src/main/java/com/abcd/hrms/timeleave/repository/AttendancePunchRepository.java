package com.abcd.hrms.timeleave.repository;

import com.abcd.hrms.timeleave.entity.AttendancePunch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface AttendancePunchRepository extends JpaRepository<AttendancePunch, UUID> {

    List<AttendancePunch> findByEmployeeIdAndPunchTimeBetweenOrderByPunchTimeAsc(UUID employeeId, Instant start, Instant end);
}
