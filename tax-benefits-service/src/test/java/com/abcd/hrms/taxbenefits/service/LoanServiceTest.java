package com.abcd.hrms.taxbenefits.service;

import com.abcd.hrms.common.messaging.outbox.OutboxPublisherService;
import com.abcd.hrms.taxbenefits.dto.LoanMasterDto;
import com.abcd.hrms.taxbenefits.entity.LoanInstallment;
import com.abcd.hrms.taxbenefits.entity.LoanMaster;
import com.abcd.hrms.taxbenefits.repository.LoanInstallmentRepository;
import com.abcd.hrms.taxbenefits.repository.LoanMasterRepository;
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
@DisplayName("LoanService Amortization Schedule & Disbursement Tests")
class LoanServiceTest {

    @Mock
    private LoanMasterRepository loanMasterRepository;
    @Mock
    private LoanInstallmentRepository installmentRepository;
    @Mock
    private OutboxPublisherService outboxPublisherService;

    @InjectMocks
    private LoanService loanService;

    private UUID loanId;
    private UUID employeeId;
    private UUID companyId;

    @BeforeEach
    void setUp() {
        loanId = UUID.randomUUID();
        employeeId = UUID.randomUUID();
        companyId = UUID.randomUUID();
    }

    @Test
    void testZeroInterestSalaryAdvanceEmi() {
        BigDecimal emi = loanService.calculateEmi(new BigDecimal("120000.0000"), BigDecimal.ZERO, 12);
        // 120,000 / 12 = 10,000.0000
        assertEquals(0, new BigDecimal("10000.0000").compareTo(emi));
    }

    @Test
    void testApplyLoan() {
        LoanMasterDto.ApplyRequest req = new LoanMasterDto.ApplyRequest();
        req.setCompanyId(companyId);
        req.setEmployeeId(employeeId);
        req.setLoanType("SALARY_ADVANCE");
        req.setPrincipalAmount(new BigDecimal("60000.0000"));
        req.setInterestRate(BigDecimal.ZERO);
        req.setTenureMonths(6);

        when(loanMasterRepository.save(any(LoanMaster.class))).thenAnswer(i -> {
            LoanMaster l = i.getArgument(0);
            l.setId(loanId);
            return l;
        });

        LoanMasterDto.Response response = loanService.applyLoan(req);
        assertNotNull(response);
        assertEquals("PENDING", response.getStatus());
        assertEquals(0, new BigDecimal("10000.0000").compareTo(response.getMonthlyEmi()));
    }

    @Test
    void testDisburseLoanGeneratesInstallments() {
        LoanMaster loan = LoanMaster.builder()
                .companyId(companyId)
                .employeeId(employeeId)
                .loanType("SALARY_ADVANCE")
                .principalAmount(new BigDecimal("60000.0000"))
                .interestRate(BigDecimal.ZERO)
                .tenureMonths(6)
                .monthlyEmi(new BigDecimal("10000.0000"))
                .status("PENDING")
                .totalPaid(BigDecimal.ZERO)
                .balancePrincipal(new BigDecimal("60000.0000"))
                .build();
        loan.setId(loanId);

        when(loanMasterRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(loanMasterRepository.save(any(LoanMaster.class))).thenAnswer(i -> i.getArgument(0));
        when(installmentRepository.save(any(LoanInstallment.class))).thenAnswer(i -> {
            LoanInstallment inst = i.getArgument(0);
            inst.setId(UUID.randomUUID());
            return inst;
        });

        LoanMasterDto.DisburseRequest disburseReq = new LoanMasterDto.DisburseRequest();
        disburseReq.setDisbursementDate(LocalDate.of(2026, 11, 1));

        LoanMasterDto.Response disbursed = loanService.disburseLoan(loanId, disburseReq);
        assertEquals("ACTIVE", disbursed.getStatus());
        assertEquals(6, disbursed.getInstallments().size());
        assertEquals(0, new BigDecimal("10000.0000").compareTo(disbursed.getInstallments().get(0).getEmiAmount()));

        verify(outboxPublisherService, times(1)).enqueueEvent(any(), any(), any(), any());
    }
}
