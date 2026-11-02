package com.abcd.hrms.timeleave.repository;

import com.abcd.hrms.timeleave.entity.LeaveLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaveLedgerRepository extends JpaRepository<LeaveLedger, UUID> {

    List<LeaveLedger> findByEmployeeIdAndLeaveTypeIdOrderByEffectiveDateAsc(UUID employeeId, UUID leaveTypeId);

    @Query("SELECT COALESCE(SUM(l.transactionUnits), 0.00) FROM LeaveLedger l WHERE l.employeeId = :employeeId AND l.leaveTypeId = :leaveTypeId")
    BigDecimal calculateCurrentBalance(@Param("employeeId") UUID employeeId, @Param("leaveTypeId") UUID leaveTypeId);

    Optional<LeaveLedger> findTopByEmployeeIdAndLeaveTypeIdOrderByEffectiveDateDescCreatedAtDesc(UUID employeeId, UUID leaveTypeId);
}
