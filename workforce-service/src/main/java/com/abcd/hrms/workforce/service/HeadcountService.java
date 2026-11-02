package com.abcd.hrms.workforce.service;

import com.abcd.hrms.common.core.context.TenantContext;
import com.abcd.hrms.workforce.dto.HeadcountSummaryDto;
import com.abcd.hrms.workforce.entity.Department;
import com.abcd.hrms.workforce.entity.Employee;
import com.abcd.hrms.workforce.entity.Employment;
import com.abcd.hrms.workforce.entity.LegalEntity;
import com.abcd.hrms.workforce.entity.Location;
import com.abcd.hrms.workforce.repository.DepartmentRepository;
import com.abcd.hrms.workforce.repository.EmployeeRepository;
import com.abcd.hrms.workforce.repository.EmploymentRepository;
import com.abcd.hrms.workforce.repository.LegalEntityRepository;
import com.abcd.hrms.workforce.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class HeadcountService {

    private final EmployeeRepository employeeRepository;
    private final EmploymentRepository employmentRepository;
    private final LegalEntityRepository legalEntityRepository;
    private final DepartmentRepository departmentRepository;
    private final LocationRepository locationRepository;

    @Transactional(readOnly = true)
    public HeadcountSummaryDto getHeadcountSummary() {
        UUID companyId = TenantContext.getCompanyId() != null ? TenantContext.getCompanyId() : UUID.fromString("d0000000-0000-0000-0000-000000000001");
        List<Employee> employees = employeeRepository.findAll();

        long total = employees.size();
        long active = 0;
        long probation = 0;
        long confirmed = 0;
        long onLeave = 0;
        long noticePeriod = 0;
        long terminated = 0;
        long preboarding = 0;

        for (Employee e : employees) {
            String status = e.getCurrentStatus();
            if ("ACTIVE".equalsIgnoreCase(status)) active++;
            else if ("PROBATION".equalsIgnoreCase(status)) probation++;
            else if ("CONFIRMED".equalsIgnoreCase(status)) confirmed++;
            else if ("ON_LEAVE".equalsIgnoreCase(status)) onLeave++;
            else if ("NOTICE_PERIOD".equalsIgnoreCase(status)) noticePeriod++;
            else if ("TERMINATED".equalsIgnoreCase(status)) terminated++;
            else if ("PREBOARDING".equalsIgnoreCase(status)) preboarding++;
        }

        Map<String, Long> byLegalEntity = new HashMap<>();
        Map<String, Long> byDepartment = new HashMap<>();
        Map<String, Long> byLocation = new HashMap<>();
        Map<String, Long> byEmploymentType = new HashMap<>();

        Map<UUID, String> legalEntityNames = new HashMap<>();
        legalEntityRepository.findAll().forEach(le -> legalEntityNames.put(le.getId(), le.getEntityCode()));

        Map<UUID, String> deptNames = new HashMap<>();
        departmentRepository.findAll().forEach(d -> deptNames.put(d.getId(), d.getName()));

        Map<UUID, String> locNames = new HashMap<>();
        locationRepository.findAll().forEach(l -> locNames.put(l.getId(), l.getName()));

        for (Employee e : employees) {
            Employment emp = employmentRepository.findByEmployeeId(e.getId()).orElse(null);
            if (emp != null) {
                String leName = legalEntityNames.getOrDefault(emp.getLegalEntityId(), "Unknown Entity");
                byLegalEntity.put(leName, byLegalEntity.getOrDefault(leName, 0L) + 1);

                String dName = deptNames.getOrDefault(emp.getDepartmentId(), "Unknown Dept");
                byDepartment.put(dName, byDepartment.getOrDefault(dName, 0L) + 1);

                String lName = locNames.getOrDefault(emp.getLocationId(), "Unknown Location");
                byLocation.put(lName, byLocation.getOrDefault(lName, 0L) + 1);

                String empType = emp.getEmploymentType() != null ? emp.getEmploymentType() : "FULL_TIME";
                byEmploymentType.put(empType, byEmploymentType.getOrDefault(empType, 0L) + 1);
            }
        }

        return HeadcountSummaryDto.builder()
                .totalHeadcount(total)
                .activeCount(active)
                .probationCount(probation)
                .confirmedCount(confirmed)
                .onLeaveCount(onLeave)
                .noticePeriodCount(noticePeriod)
                .terminatedCount(terminated)
                .preboardingCount(preboarding)
                .headcountByLegalEntity(byLegalEntity)
                .headcountByDepartment(byDepartment)
                .headcountByLocation(byLocation)
                .headcountByEmploymentType(byEmploymentType)
                .build();
    }
}
