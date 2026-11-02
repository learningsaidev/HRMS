package com.abcd.hrms.workforce.service;

import com.abcd.hrms.common.core.context.TenantContext;
import com.abcd.hrms.workforce.dto.OrgNodeDto;
import com.abcd.hrms.workforce.entity.Department;
import com.abcd.hrms.workforce.entity.Employee;
import com.abcd.hrms.workforce.entity.Employment;
import com.abcd.hrms.workforce.entity.ManagerRelationship;
import com.abcd.hrms.workforce.repository.DepartmentRepository;
import com.abcd.hrms.workforce.repository.EmployeeRepository;
import com.abcd.hrms.workforce.repository.EmploymentRepository;
import com.abcd.hrms.workforce.repository.ManagerRelationshipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final EmployeeRepository employeeRepository;
    private final EmploymentRepository employmentRepository;
    private final DepartmentRepository departmentRepository;
    private final ManagerRelationshipRepository managerRelationshipRepository;

    @Transactional(readOnly = true)
    public List<OrgNodeDto> getOrganizationTree() {
        UUID companyId = TenantContext.getCompanyId() != null ? TenantContext.getCompanyId() : UUID.fromString("d0000000-0000-0000-0000-000000000001");
        List<Employee> allEmployees = employeeRepository.findAll();

        Map<UUID, List<UUID>> managerToReports = new HashMap<>();
        Set<UUID> employeesWithManager = new HashSet<>();

        List<ManagerRelationship> relationships = managerRelationshipRepository.findAll();
        for (ManagerRelationship rel : relationships) {
            if (rel.isActive()) {
                managerToReports.computeIfAbsent(rel.getManagerEmployeeId(), k -> new ArrayList<>()).add(rel.getEmployeeId());
                employeesWithManager.add(rel.getEmployeeId());
            }
        }

        Map<UUID, Employee> employeeMap = new HashMap<>();
        for (Employee e : allEmployees) {
            employeeMap.put(e.getId(), e);
        }

        List<OrgNodeDto> rootNodes = new ArrayList<>();
        for (Employee e : allEmployees) {
            if (!employeesWithManager.contains(e.getId())) {
                rootNodes.add(buildNodeRecursive(e, managerToReports, employeeMap));
            }
        }

        return rootNodes;
    }

    private OrgNodeDto buildNodeRecursive(Employee employee, Map<UUID, List<UUID>> managerToReports, Map<UUID, Employee> employeeMap) {
        Employment employment = employmentRepository.findByEmployeeId(employee.getId()).orElse(null);
        String deptName = null;
        String designation = null;
        if (employment != null) {
            designation = employment.getDesignation();
            deptName = departmentRepository.findById(employment.getDepartmentId()).map(Department::getName).orElse(null);
        }

        OrgNodeDto node = OrgNodeDto.builder()
                .employeeId(employee.getId())
                .employeeNumber(employee.getEmployeeNumber())
                .name(employee.getDisplayName())
                .designation(designation != null ? designation : "Team Member")
                .department(deptName != null ? deptName : "General")
                .workEmail(employee.getWorkEmail())
                .status(employee.getCurrentStatus())
                .directReports(new ArrayList<>())
                .build();

        List<UUID> reportIds = managerToReports.getOrDefault(employee.getId(), Collections.emptyList());
        for (UUID reportId : reportIds) {
            Employee report = employeeMap.get(reportId);
            if (report != null) {
                node.getDirectReports().add(buildNodeRecursive(report, managerToReports, employeeMap));
            }
        }

        return node;
    }
}
