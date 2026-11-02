package com.abcd.hrms.taxbenefits.service;

import com.abcd.hrms.common.core.context.TenantContext;
import com.abcd.hrms.common.core.exception.BusinessValidationException;
import com.abcd.hrms.common.core.exception.EntityNotFoundException;
import com.abcd.hrms.common.core.util.MoneyUtil;
import com.abcd.hrms.common.messaging.KafkaTopicConstants;
import com.abcd.hrms.common.messaging.event.CanonicalEvent;
import com.abcd.hrms.common.messaging.outbox.OutboxPublisherService;
import com.abcd.hrms.taxbenefits.dto.LoanMasterDto;
import com.abcd.hrms.taxbenefits.entity.LoanInstallment;
import com.abcd.hrms.taxbenefits.entity.LoanMaster;
import com.abcd.hrms.taxbenefits.repository.LoanInstallmentRepository;
import com.abcd.hrms.taxbenefits.repository.LoanMasterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanMasterRepository loanMasterRepository;
    private final LoanInstallmentRepository installmentRepository;
    private final OutboxPublisherService outboxPublisherService;

    @Transactional
    public LoanMasterDto.Response applyLoan(LoanMasterDto.ApplyRequest request) {
        UUID companyId = request.getCompanyId() != null ? request.getCompanyId() : TenantContext.getCompanyId();
        if (companyId == null) {
            companyId = UUID.fromString("d0000000-0000-0000-0000-000000000001");
        }

        BigDecimal principal = MoneyUtil.round(request.getPrincipalAmount());
        BigDecimal rate = request.getInterestRate() != null ? MoneyUtil.round(request.getInterestRate()) : BigDecimal.ZERO;
        int tenure = request.getTenureMonths() != null && request.getTenureMonths() > 0 ? request.getTenureMonths() : 12;

        BigDecimal monthlyEmi = calculateEmi(principal, rate, tenure);

        LoanMaster loan = LoanMaster.builder()
                .companyId(companyId)
                .employeeId(request.getEmployeeId())
                .loanType(request.getLoanType() != null ? request.getLoanType().toUpperCase() : "SALARY_ADVANCE")
                .principalAmount(principal)
                .interestRate(rate)
                .tenureMonths(tenure)
                .monthlyEmi(monthlyEmi)
                .status("PENDING")
                .totalPaid(BigDecimal.ZERO)
                .balancePrincipal(principal)
                .build();

        LoanMaster saved = loanMasterRepository.save(loan);
        return mapToDto(saved, Collections.emptyList());
    }

    @Transactional
    public LoanMasterDto.Response disburseLoan(UUID loanId, LoanMasterDto.DisburseRequest request) {
        LoanMaster loan = loanMasterRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("LoanMaster", loanId));

        if ("ACTIVE".equalsIgnoreCase(loan.getStatus()) || "CLOSED".equalsIgnoreCase(loan.getStatus())) {
            throw new BusinessValidationException("Loan is already disbursed or closed", "ERR_LOAN_ALREADY_ACTIVE");
        }

        LocalDate disburseDate = request.getDisbursementDate() != null ? request.getDisbursementDate() : LocalDate.now();
        loan.setStatus("ACTIVE");
        loan.setDisbursementDate(disburseDate);
        LoanMaster updatedLoan = loanMasterRepository.save(loan);

        // Delete any prior installments if previously generated
        installmentRepository.deleteByLoanId(loanId);

        // Generate Amortization Schedule
        List<LoanInstallment> installments = generateAmortizationSchedule(updatedLoan, disburseDate);

        // Publish LoanDisbursed.v1 event
        Map<String, Object> payload = new HashMap<>();
        payload.put("loanId", updatedLoan.getId());
        payload.put("employeeId", updatedLoan.getEmployeeId());
        payload.put("loanType", updatedLoan.getLoanType());
        payload.put("principalAmount", updatedLoan.getPrincipalAmount().toString());
        payload.put("monthlyEmi", updatedLoan.getMonthlyEmi().toString());
        payload.put("tenureMonths", updatedLoan.getTenureMonths());
        payload.put("disbursementDate", disburseDate.toString());

        CanonicalEvent<Map<String, Object>> event = CanonicalEvent.of(
                "LoanDisbursed.v1",
                "LOAN_MASTER",
                updatedLoan.getId().toString(),
                updatedLoan.getCompanyId(),
                null,
                "tax-benefits-service",
                TenantContext.getCorrelationId(),
                payload
        );

        outboxPublisherService.enqueueEvent(
                KafkaTopicConstants.TOPIC_PAYROLL_EVENTS,
                "LOAN_MASTER",
                updatedLoan.getId().toString(),
                event
        );

        return mapToDto(updatedLoan, installments);
    }

    @Transactional(readOnly = true)
    public LoanMasterDto.Response getLoanById(UUID loanId) {
        LoanMaster loan = loanMasterRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("LoanMaster", loanId));
        List<LoanInstallment> installments = installmentRepository.findByLoanIdOrderByInstallmentNumberAsc(loanId);
        return mapToDto(loan, installments);
    }

    @Transactional(readOnly = true)
    public List<LoanMasterDto.Response> listLoansForEmployee(UUID employeeId, UUID companyId) {
        UUID effectiveCompanyId = companyId != null ? companyId : TenantContext.getCompanyId();
        if (effectiveCompanyId == null) {
            effectiveCompanyId = UUID.fromString("d0000000-0000-0000-0000-000000000001");
        }
        return loanMasterRepository.findByCompanyIdAndEmployeeIdOrderByCreatedAtDesc(effectiveCompanyId, employeeId).stream()
                .map(l -> mapToDto(l, installmentRepository.findByLoanIdOrderByInstallmentNumberAsc(l.getId())))
                .collect(Collectors.toList());
    }

    public BigDecimal calculateEmi(BigDecimal principal, BigDecimal annualRate, int tenureMonths) {
        if (annualRate == null || annualRate.compareTo(BigDecimal.ZERO) == 0) {
            return MoneyUtil.divide(principal, BigDecimal.valueOf(tenureMonths));
        }

        double p = principal.doubleValue();
        double r = (annualRate.doubleValue() / 12.0) / 100.0;
        int n = tenureMonths;

        double emi = (p * r * Math.pow(1.0 + r, n)) / (Math.pow(1.0 + r, n) - 1.0);
        return MoneyUtil.round(BigDecimal.valueOf(emi));
    }

    private List<LoanInstallment> generateAmortizationSchedule(LoanMaster loan, LocalDate startDate) {
        List<LoanInstallment> list = new ArrayList<>();
        BigDecimal balance = loan.getPrincipalAmount();
        BigDecimal annualRate = loan.getInterestRate();
        BigDecimal monthlyRate = annualRate.compareTo(BigDecimal.ZERO) > 0 ?
                annualRate.divide(BigDecimal.valueOf(1200), 8, MoneyUtil.DEFAULT_ROUNDING_MODE) : BigDecimal.ZERO;

        for (int i = 1; i <= loan.getTenureMonths(); i++) {
            LocalDate dueDate = startDate.plusMonths(i);
            BigDecimal interest = MoneyUtil.round(balance.multiply(monthlyRate));
            BigDecimal emi = loan.getMonthlyEmi();
            BigDecimal principal = emi.subtract(interest);

            if (i == loan.getTenureMonths() || principal.compareTo(balance) > 0) {
                principal = balance;
                emi = principal.add(interest);
            }

            balance = balance.subtract(principal);
            if (balance.compareTo(BigDecimal.ZERO) < 0) {
                balance = BigDecimal.ZERO;
            }

            LoanInstallment installment = LoanInstallment.builder()
                    .loanId(loan.getId())
                    .installmentNumber(i)
                    .dueDate(dueDate)
                    .emiAmount(MoneyUtil.round(emi))
                    .principalComponent(MoneyUtil.round(principal))
                    .interestComponent(MoneyUtil.round(interest))
                    .status("PENDING")
                    .build();

            list.add(installmentRepository.save(installment));
        }

        return list;
    }

    private LoanMasterDto.Response mapToDto(LoanMaster l, List<LoanInstallment> installments) {
        List<LoanMasterDto.InstallmentDto> installmentDtos = installments.stream()
                .map(i -> LoanMasterDto.InstallmentDto.builder()
                        .id(i.getId())
                        .installmentNumber(i.getInstallmentNumber())
                        .dueDate(i.getDueDate())
                        .emiAmount(i.getEmiAmount())
                        .principalComponent(i.getPrincipalComponent())
                        .interestComponent(i.getInterestComponent())
                        .status(i.getStatus())
                        .payrollRunId(i.getPayrollRunId())
                        .build())
                .collect(Collectors.toList());

        return LoanMasterDto.Response.builder()
                .id(l.getId())
                .companyId(l.getCompanyId())
                .employeeId(l.getEmployeeId())
                .loanType(l.getLoanType())
                .principalAmount(l.getPrincipalAmount())
                .interestRate(l.getInterestRate())
                .tenureMonths(l.getTenureMonths())
                .monthlyEmi(l.getMonthlyEmi())
                .disbursementDate(l.getDisbursementDate())
                .status(l.getStatus())
                .totalPaid(l.getTotalPaid())
                .balancePrincipal(l.getBalancePrincipal())
                .createdAt(l.getCreatedAt())
                .installments(installmentDtos)
                .build();
    }
}
