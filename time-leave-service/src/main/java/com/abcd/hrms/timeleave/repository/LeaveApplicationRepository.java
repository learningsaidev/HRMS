package com.abcd.hrms.timeleave.repository;

import com.abcd.hrms.timeleave.entity.LeaveApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, UUID> {

    List<LeaveApplication> findByEmployeeId(UUID employeeId);

    List<LeaveApplication> findByCompanyIdAndStatus(UUID companyId, String status);

    @Query("SELECT COUNT(l) FROM LeaveApplication l WHERE l.employeeId = :employeeId " +
           "AND l.status IN ('PENDING', 'APPROVED') " +
           "AND ((l.startDate <= :endDate AND l.endDate >= :startDate))")
    long countOverlappingApplications(@Param("employeeId") UUID employeeId,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);
}
