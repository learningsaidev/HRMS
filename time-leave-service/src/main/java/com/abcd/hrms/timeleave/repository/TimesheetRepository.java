package com.abcd.hrms.timeleave.repository;

import com.abcd.hrms.timeleave.entity.Timesheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimesheetRepository extends JpaRepository<Timesheet, UUID> {

    List<Timesheet> findByEmployeeId(UUID employeeId);

    Optional<Timesheet> findByEmployeeIdAndPeriodStartAndPeriodEnd(UUID employeeId, LocalDate start, LocalDate end);
}
