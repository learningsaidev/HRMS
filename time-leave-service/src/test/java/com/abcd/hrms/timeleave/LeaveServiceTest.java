package com.abcd.hrms.timeleave;

import com.abcd.hrms.common.core.exception.BusinessValidationException;
import com.abcd.hrms.timeleave.dto.LeaveApplicationDto;
import com.abcd.hrms.timeleave.dto.LeaveApplicationRequest;
import com.abcd.hrms.timeleave.entity.LeaveApplication;
import com.abcd.hrms.timeleave.entity.LeaveLedger;
import com.abcd.hrms.timeleave.entity.LeaveType;
import com.abcd.hrms.timeleave.repository.LeaveApplicationRepository;
import com.abcd.hrms.timeleave.repository.LeaveLedgerRepository;
import com.abcd.hrms.timeleave.repository.LeaveTypeRepository;
import com.abcd.hrms.timeleave.service.LeaveService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LeaveService Ledger & Balance Tests")
class LeaveServiceTest {

    @Mock
    private LeaveApplicationRepository leaveApplicationRepository;

    @Mock
    private LeaveLedgerRepository leaveLedgerRepository;

    @Mock
    private LeaveTypeRepository leaveTypeRepository;

    @InjectMocks
    private LeaveService leaveService;

    private UUID employeeId;
    private UUID leaveTypeId;
    private LeaveType casualLeave;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        leaveTypeId = UUID.randomUUID();

        casualLeave = LeaveType.builder()
                .code("CL")
                .name("Casual Leave")
                .isPaid(true)
                .maxDaysPerYear(BigDecimal.valueOf(12))
                .build();
        casualLeave.setId(leaveTypeId);
    }

    @Test
    @DisplayName("Should successfully apply for leave when balance is sufficient and no overlap exists")
    void testApplyLeaveSuccess() {
        LocalDate start = LocalDate.of(2026, 11, 10);
        LocalDate end = LocalDate.of(2026, 11, 11);

        LeaveApplicationRequest request = LeaveApplicationRequest.builder()
                .employeeId(employeeId)
                .leaveTypeId(leaveTypeId)
                .startDate(start)
                .endDate(end)
                .isHalfDay(false)
                .reason("Personal work")
                .build();

        when(leaveApplicationRepository.countOverlappingApplications(employeeId, start, end)).thenReturn(0L);
        when(leaveTypeRepository.findById(leaveTypeId)).thenReturn(Optional.of(casualLeave));
        when(leaveLedgerRepository.calculateCurrentBalance(employeeId, leaveTypeId)).thenReturn(BigDecimal.valueOf(10.00));

        LeaveApplication saved = LeaveApplication.builder()
                .employeeId(employeeId)
                .leaveTypeId(leaveTypeId)
                .startDate(start)
                .endDate(end)
                .totalDays(BigDecimal.valueOf(2.00))
                .status("PENDING")
                .reason("Personal work")
                .build();
        saved.setId(UUID.randomUUID());

        when(leaveApplicationRepository.save(any())).thenReturn(saved);

        LeaveApplicationDto result = leaveService.applyLeave(request);

        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
        assertEquals(BigDecimal.valueOf(2.00), result.getTotalDays());
        assertEquals("CL", result.getLeaveTypeCode());
    }

    @Test
    @DisplayName("Should reject leave application if balance is insufficient")
    void testApplyLeaveInsufficientBalance() {
        LocalDate start = LocalDate.of(2026, 11, 10);
        LocalDate end = LocalDate.of(2026, 11, 15); // 6 days

        LeaveApplicationRequest request = LeaveApplicationRequest.builder()
                .employeeId(employeeId)
                .leaveTypeId(leaveTypeId)
                .startDate(start)
                .endDate(end)
                .reason("Long vacation")
                .build();

        when(leaveApplicationRepository.countOverlappingApplications(employeeId, start, end)).thenReturn(0L);
        when(leaveTypeRepository.findById(leaveTypeId)).thenReturn(Optional.of(casualLeave));
        when(leaveLedgerRepository.calculateCurrentBalance(employeeId, leaveTypeId)).thenReturn(BigDecimal.valueOf(3.00)); // only 3 days

        assertThrows(BusinessValidationException.class, () -> leaveService.applyLeave(request));
    }

    @Test
    @DisplayName("Should approve leave and debit from immutable leave ledger")
    void testApproveLeave() {
        UUID applicationId = UUID.randomUUID();
        UUID approverId = UUID.randomUUID();

        LeaveApplication app = LeaveApplication.builder()
                .employeeId(employeeId)
                .leaveTypeId(leaveTypeId)
                .startDate(LocalDate.of(2026, 11, 10))
                .endDate(LocalDate.of(2026, 11, 11))
                .totalDays(BigDecimal.valueOf(2.00))
                .status("PENDING")
                .reason("Medical checkup")
                .build();
        app.setId(applicationId);

        when(leaveApplicationRepository.findById(applicationId)).thenReturn(Optional.of(app));
        when(leaveTypeRepository.findById(leaveTypeId)).thenReturn(Optional.of(casualLeave));
        when(leaveLedgerRepository.calculateCurrentBalance(employeeId, leaveTypeId)).thenReturn(BigDecimal.valueOf(10.00));
        when(leaveApplicationRepository.save(any())).thenReturn(app);

        LeaveApplicationDto result = leaveService.approveLeave(applicationId, approverId);

        assertNotNull(result);
        assertEquals("APPROVED", result.getStatus());
        // Verify ledger entry is saved with negative transaction units
        verify(leaveLedgerRepository, times(1)).save(argThat(entry ->
                entry.getTransactionUnits().compareTo(BigDecimal.valueOf(-2.00)) == 0 &&
                "CONSUMPTION".equals(entry.getEntryType())
        ));
    }
}
