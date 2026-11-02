package com.abcd.hrms.timeleave.service;

import com.abcd.hrms.common.core.context.TenantContext;
import com.abcd.hrms.common.core.exception.BusinessValidationException;
import com.abcd.hrms.common.core.exception.EntityNotFoundException;
import com.abcd.hrms.timeleave.dto.LeaveApplicationDto;
import com.abcd.hrms.timeleave.dto.LeaveApplicationRequest;
import com.abcd.hrms.timeleave.dto.LeaveBalanceDto;
import com.abcd.hrms.timeleave.entity.LeaveApplication;
import com.abcd.hrms.timeleave.entity.LeaveLedger;
import com.abcd.hrms.timeleave.entity.LeaveType;
import com.abcd.hrms.timeleave.repository.LeaveApplicationRepository;
import com.abcd.hrms.timeleave.repository.LeaveLedgerRepository;
import com.abcd.hrms.timeleave.repository.LeaveTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveApplicationRepository leaveApplicationRepository;
    private final LeaveLedgerRepository leaveLedgerRepository;
    private final LeaveTypeRepository leaveTypeRepository;

    @Transactional
    public LeaveApplicationDto applyLeave(LeaveApplicationRequest request) {
        UUID companyId = TenantContext.getCompanyId() != null ? TenantContext.getCompanyId() : UUID.fromString("d0000000-0000-0000-0000-000000000001");

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BusinessValidationException("Leave start date cannot be after end date", "ERR_INVALID_LEAVE_DATES");
        }

        long overlapCount = leaveApplicationRepository.countOverlappingApplications(
                request.getEmployeeId(), request.getStartDate(), request.getEndDate()
        );
        if (overlapCount > 0) {
            throw new BusinessValidationException("Leave application overlaps with an existing pending/approved leave request", "ERR_LEAVE_OVERLAP");
        }

        LeaveType leaveType = leaveTypeRepository.findById(request.getLeaveTypeId())
                .orElseThrow(() -> new EntityNotFoundException("LeaveType", request.getLeaveTypeId()));

        long daysBetween = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        BigDecimal totalDays = request.isHalfDay() ? BigDecimal.valueOf(0.50) : BigDecimal.valueOf(daysBetween);

        if (leaveType.isPaid()) {
            BigDecimal currentBalance = leaveLedgerRepository.calculateCurrentBalance(request.getEmployeeId(), leaveType.getId());
            if (currentBalance.compareTo(totalDays) < 0) {
                throw new BusinessValidationException(
                        String.format("Insufficient leave balance for %s. Available [%s], Requested [%s]",
                                leaveType.getName(), currentBalance, totalDays),
                        "ERR_INSUFFICIENT_LEAVE_BALANCE"
                );
            }
        }

        LeaveApplication application = LeaveApplication.builder()
                .companyId(companyId)
                .employeeId(request.getEmployeeId())
                .leaveTypeId(request.getLeaveTypeId())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalDays(totalDays)
                .isHalfDay(request.isHalfDay())
                .reason(request.getReason())
                .status("PENDING")
                .build();

        LeaveApplication saved = leaveApplicationRepository.save(application);
        return mapToDto(saved, leaveType);
    }

    @Transactional
    public LeaveApplicationDto approveLeave(UUID applicationId, UUID approverId) {
        LeaveApplication application = leaveApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new EntityNotFoundException("LeaveApplication", applicationId));

        if (!"PENDING".equalsIgnoreCase(application.getStatus())) {
            throw new BusinessValidationException("Cannot approve leave application in state: " + application.getStatus(), "ERR_INVALID_LEAVE_STATE");
        }

        LeaveType leaveType = leaveTypeRepository.findById(application.getLeaveTypeId())
                .orElseThrow(() -> new EntityNotFoundException("LeaveType", application.getLeaveTypeId()));

        // Check balance again prior to debiting
        BigDecimal currentBalance = leaveLedgerRepository.calculateCurrentBalance(application.getEmployeeId(), leaveType.getId());
        if (leaveType.isPaid() && currentBalance.compareTo(application.getTotalDays()) < 0) {
            throw new BusinessValidationException("Insufficient leave balance upon approval", "ERR_INSUFFICIENT_LEAVE_BALANCE");
        }

        application.setStatus("APPROVED");
        application.setApprovedBy(approverId != null ? approverId : TenantContext.getUserId());
        application.setApprovedAt(Instant.now());
        LeaveApplication updated = leaveApplicationRepository.save(application);

        // Record immutable ledger entry
        BigDecimal newBalance = currentBalance.subtract(application.getTotalDays());
        LeaveLedger consumptionEntry = LeaveLedger.builder()
                .companyId(application.getCompanyId())
                .employeeId(application.getEmployeeId())
                .leaveTypeId(leaveType.getId())
                .transactionUnits(application.getTotalDays().negate())
                .entryType("CONSUMPTION")
                .effectiveDate(application.getStartDate())
                .referenceId(application.getId().toString())
                .balanceAfter(newBalance)
                .build();
        leaveLedgerRepository.save(consumptionEntry);

        return mapToDto(updated, leaveType);
    }

    @Transactional(readOnly = true)
    public List<LeaveBalanceDto> getEmployeeBalances(UUID employeeId) {
        List<LeaveType> leaveTypes = leaveTypeRepository.findAll();
        List<LeaveBalanceDto> dtos = new ArrayList<>();

        for (LeaveType lt : leaveTypes) {
            BigDecimal balance = leaveLedgerRepository.calculateCurrentBalance(employeeId, lt.getId());
            dtos.add(LeaveBalanceDto.builder()
                    .leaveTypeId(lt.getId())
                    .leaveTypeCode(lt.getCode())
                    .leaveTypeName(lt.getName())
                    .isPaid(lt.isPaid())
                    .currentBalance(balance)
                    .build());
        }

        return dtos;
    }

    private LeaveApplicationDto mapToDto(LeaveApplication app, LeaveType leaveType) {
        return LeaveApplicationDto.builder()
                .id(app.getId())
                .employeeId(app.getEmployeeId())
                .leaveTypeId(app.getLeaveTypeId())
                .leaveTypeCode(leaveType != null ? leaveType.getCode() : null)
                .leaveTypeName(leaveType != null ? leaveType.getName() : null)
                .startDate(app.getStartDate())
                .endDate(app.getEndDate())
                .totalDays(app.getTotalDays())
                .isHalfDay(app.isHalfDay())
                .reason(app.getReason())
                .status(app.getStatus())
                .approvedBy(app.getApprovedBy())
                .approvedAt(app.getApprovedAt())
                .rejectionReason(app.getRejectionReason())
                .createdAt(app.getCreatedAt())
                .build();
    }
}
