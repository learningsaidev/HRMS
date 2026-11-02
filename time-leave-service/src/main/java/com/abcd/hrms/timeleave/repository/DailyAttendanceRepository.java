package com.abcd.hrms.timeleave.repository;

import com.abcd.hrms.timeleave.entity.DailyAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DailyAttendanceRepository extends JpaRepository<DailyAttendance, UUID> {

    Optional<DailyAttendance> findByEmployeeIdAndAttendanceDate(UUID employeeId, LocalDate attendanceDate);

    List<DailyAttendance> findByEmployeeIdAndAttendanceDateBetween(UUID employeeId, LocalDate start, LocalDate end);

    List<DailyAttendance> findByCompanyIdAndAttendanceDateBetween(UUID companyId, LocalDate start, LocalDate end);
}
