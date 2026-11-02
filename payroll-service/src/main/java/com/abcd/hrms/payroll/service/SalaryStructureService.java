package com.abcd.hrms.payroll.service;

import com.abcd.hrms.common.core.context.TenantContext;
import com.abcd.hrms.common.core.exception.ConflictException;
import com.abcd.hrms.common.core.exception.EntityNotFoundException;
import com.abcd.hrms.common.core.util.MoneyUtil;
import com.abcd.hrms.payroll.dto.EmployeeCompensationDto;
import com.abcd.hrms.payroll.dto.PayGroupDto;
import com.abcd.hrms.payroll.dto.SalaryComponentDto;
import com.abcd.hrms.payroll.entity.CompensationComponent;
import com.abcd.hrms.payroll.entity.EmployeeCompensation;
import com.abcd.hrms.payroll.entity.PayGroup;
import com.abcd.hrms.payroll.entity.SalaryComponent;
import com.abcd.hrms.payroll.repository.CompensationComponentRepository;
import com.abcd.hrms.payroll.repository.EmployeeCompensationRepository;
import com.abcd.hrms.payroll.repository.PayGroupRepository;
import com.abcd.hrms.payroll.repository.SalaryComponentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalaryStructureService {

    private final PayGroupRepository payGroupRepository;
    private final SalaryComponentRepository salaryComponentRepository;
    private final EmployeeCompensationRepository employeeCompensationRepository;
    private final CompensationComponentRepository compensationComponentRepository;

    @Transactional
    public PayGroupDto.Response createPayGroup(PayGroupDto.CreateRequest request) {
        UUID companyId = request.getCompanyId() != null ? request.getCompanyId() : TenantContext.getCompanyId();
        if (companyId == null) {
            companyId = UUID.fromString("d0000000-0000-0000-0000-000000000001");
        }

        payGroupRepository.findByCompanyIdAndGroupCode(companyId, request.getGroupCode()).ifPresent(p -> {
            throw new ConflictException("PayGroup", "groupCode " + request.getGroupCode());
        });

        PayGroup pg = PayGroup.builder()
                .companyId(companyId)
                .legalEntityId(request.getLegalEntityId())
                .groupCode(request.getGroupCode().toUpperCase())
                .groupName(request.getGroupName())
                .payFrequency(request.getPayFrequency() != null ? request.getPayFrequency() : "MONTHLY")
                .currency(request.getCurrency() != null ? request.getCurrency() : "INR")
                .salaryDisbursementDay(request.getSalaryDisbursementDay() != null ? request.getSalaryDisbursementDay() : 30)
                .isActive(true)
                .build();

        PayGroup saved = payGroupRepository.save(pg);
        return mapToPayGroupResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PayGroupDto.Response> listPayGroups(UUID companyId) {
        UUID effectiveCompanyId = companyId != null ? companyId : TenantContext.getCompanyId();
        if (effectiveCompanyId == null) {
            effectiveCompanyId = UUID.fromString("d0000000-0000-0000-0000-000000000001");
        }
        return payGroupRepository.findByCompanyIdAndIsActiveTrue(effectiveCompanyId).stream()
                .map(this::mapToPayGroupResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SalaryComponentDto.Response createSalaryComponent(SalaryComponentDto.CreateRequest request) {
        UUID companyId = request.getCompanyId() != null ? request.getCompanyId() : TenantContext.getCompanyId();
        if (companyId == null) {
            companyId = UUID.fromString("d0000000-0000-0000-0000-000000000001");
        }

        salaryComponentRepository.findByCompanyIdAndComponentCode(companyId, request.getComponentCode()).ifPresent(c -> {
            throw new ConflictException("SalaryComponent", "componentCode " + request.getComponentCode());
        });

        SalaryComponent sc = SalaryComponent.builder()
                .companyId(companyId)
                .componentCode(request.getComponentCode().toUpperCase())
                .componentName(request.getComponentName())
                .componentType(request.getComponentType().toUpperCase())
                .calculationType(request.getCalculationType() != null ? request.getCalculationType() : "FIXED")
                .taxable(request.getTaxable() != null ? request.getTaxable() : true)
                .affectsGross(request.getAffectsGross() != null ? request.getAffectsGross() : true)
                .affectsNet(request.getAffectsNet() != null ? request.getAffectsNet() : true)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(true)
                .build();

        SalaryComponent saved = salaryComponentRepository.save(sc);
        return mapToSalaryComponentResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<SalaryComponentDto.Response> listSalaryComponents(UUID companyId) {
        UUID effectiveCompanyId = companyId != null ? companyId : TenantContext.getCompanyId();
        if (effectiveCompanyId == null) {
            effectiveCompanyId = UUID.fromString("d0000000-0000-0000-0000-000000000001");
        }
        return salaryComponentRepository.findByCompanyIdAndIsActiveTrueOrderByDisplayOrderAsc(effectiveCompanyId).stream()
                .map(this::mapToSalaryComponentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public EmployeeCompensationDto.Response assignCompensation(EmployeeCompensationDto.AssignRequest request) {
        UUID companyId = request.getCompanyId() != null ? request.getCompanyId() : TenantContext.getCompanyId();
        if (companyId == null) {
            companyId = UUID.fromString("d0000000-0000-0000-0000-000000000001");
        }

        // Deactivate any existing active compensation
        employeeCompensationRepository.findByCompanyIdAndEmployeeIdAndIsActiveTrue(companyId, request.getEmployeeId())
                .ifPresent(existing -> {
                    existing.setIsActive(false);
                    employeeCompensationRepository.save(existing);
                });

        BigDecimal grossMonthly = request.getGrossMonthly() != null ? request.getGrossMonthly() :
                MoneyUtil.divide(request.getCtcAnnual(), BigDecimal.valueOf(12));

        EmployeeCompensation compensation = EmployeeCompensation.builder()
                .companyId(companyId)
                .employeeId(request.getEmployeeId())
                .payGroupId(request.getPayGroupId())
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .ctcAnnual(MoneyUtil.scale(request.getCtcAnnual()))
                .grossMonthly(MoneyUtil.scale(grossMonthly))
                .isActive(true)
                .build();

        EmployeeCompensation savedCompensation = employeeCompensationRepository.save(compensation);

        List<EmployeeCompensationDto.ComponentItem> componentItems = new ArrayList<>();
        if (request.getComponents() != null) {
            for (EmployeeCompensationDto.AssignComponentItem item : request.getComponents()) {
                SalaryComponent sc = salaryComponentRepository.findById(item.getComponentId())
                        .orElseThrow(() -> new EntityNotFoundException("SalaryComponent", item.getComponentId()));

                CompensationComponent compComp = CompensationComponent.builder()
                        .compensationId(savedCompensation.getId())
                        .componentId(sc.getId())
                        .amountMonthly(MoneyUtil.scale(item.getAmountMonthly()))
                        .percentageValue(item.getPercentageValue())
                        .build();

                compensationComponentRepository.save(compComp);

                componentItems.add(EmployeeCompensationDto.ComponentItem.builder()
                        .componentId(sc.getId())
                        .componentCode(sc.getComponentCode())
                        .componentName(sc.getComponentName())
                        .componentType(sc.getComponentType())
                        .amountMonthly(compComp.getAmountMonthly())
                        .percentageValue(compComp.getPercentageValue())
                        .build());
            }
        }

        return EmployeeCompensationDto.Response.builder()
                .id(savedCompensation.getId())
                .companyId(savedCompensation.getCompanyId())
                .employeeId(savedCompensation.getEmployeeId())
                .payGroupId(savedCompensation.getPayGroupId())
                .effectiveFrom(savedCompensation.getEffectiveFrom())
                .effectiveTo(savedCompensation.getEffectiveTo())
                .ctcAnnual(savedCompensation.getCtcAnnual())
                .grossMonthly(savedCompensation.getGrossMonthly())
                .isActive(savedCompensation.getIsActive())
                .components(componentItems)
                .build();
    }

    @Transactional(readOnly = true)
    public EmployeeCompensationDto.Response getEmployeeCompensation(UUID employeeId, UUID companyId) {
        UUID effectiveCompanyId = companyId != null ? companyId : TenantContext.getCompanyId();
        if (effectiveCompanyId == null) {
            effectiveCompanyId = UUID.fromString("d0000000-0000-0000-0000-000000000001");
        }

        EmployeeCompensation comp = employeeCompensationRepository
                .findByCompanyIdAndEmployeeIdAndIsActiveTrue(effectiveCompanyId, employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Active compensation for employee", employeeId));

        List<CompensationComponent> compItems = compensationComponentRepository.findByCompensationId(comp.getId());
        List<EmployeeCompensationDto.ComponentItem> items = compItems.stream().map(c -> {
            SalaryComponent sc = salaryComponentRepository.findById(c.getComponentId()).orElse(null);
            return EmployeeCompensationDto.ComponentItem.builder()
                    .componentId(c.getComponentId())
                    .componentCode(sc != null ? sc.getComponentCode() : "UNKNOWN")
                    .componentName(sc != null ? sc.getComponentName() : "Unknown")
                    .componentType(sc != null ? sc.getComponentType() : "EARNING")
                    .amountMonthly(c.getAmountMonthly())
                    .percentageValue(c.getPercentageValue())
                    .build();
        }).collect(Collectors.toList());

        return EmployeeCompensationDto.Response.builder()
                .id(comp.getId())
                .companyId(comp.getCompanyId())
                .employeeId(comp.getEmployeeId())
                .payGroupId(comp.getPayGroupId())
                .effectiveFrom(comp.getEffectiveFrom())
                .effectiveTo(comp.getEffectiveTo())
                .ctcAnnual(comp.getCtcAnnual())
                .grossMonthly(comp.getGrossMonthly())
                .isActive(comp.getIsActive())
                .components(items)
                .build();
    }

    private PayGroupDto.Response mapToPayGroupResponse(PayGroup pg) {
        return PayGroupDto.Response.builder()
                .id(pg.getId())
                .companyId(pg.getCompanyId())
                .legalEntityId(pg.getLegalEntityId())
                .groupCode(pg.getGroupCode())
                .groupName(pg.getGroupName())
                .payFrequency(pg.getPayFrequency())
                .currency(pg.getCurrency())
                .salaryDisbursementDay(pg.getSalaryDisbursementDay())
                .isActive(pg.getIsActive())
                .createdAt(pg.getCreatedAt())
                .updatedAt(pg.getUpdatedAt())
                .build();
    }

    private SalaryComponentDto.Response mapToSalaryComponentResponse(SalaryComponent sc) {
        return SalaryComponentDto.Response.builder()
                .id(sc.getId())
                .companyId(sc.getCompanyId())
                .componentCode(sc.getComponentCode())
                .componentName(sc.getComponentName())
                .componentType(sc.getComponentType())
                .calculationType(sc.getCalculationType())
                .taxable(sc.getTaxable())
                .affectsGross(sc.getAffectsGross())
                .affectsNet(sc.getAffectsNet())
                .displayOrder(sc.getDisplayOrder())
                .isActive(sc.getIsActive())
                .createdAt(sc.getCreatedAt())
                .updatedAt(sc.getUpdatedAt())
                .build();
    }
}
