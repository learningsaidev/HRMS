package com.abcd.hrms.workforce.service;

import com.abcd.hrms.common.core.context.TenantContext;
import com.abcd.hrms.common.core.exception.ConflictException;
import com.abcd.hrms.common.core.exception.EntityNotFoundException;
import com.abcd.hrms.common.messaging.KafkaTopicConstants;
import com.abcd.hrms.common.messaging.event.CanonicalEvent;
import com.abcd.hrms.common.messaging.outbox.OutboxPublisherService;
import com.abcd.hrms.common.security.encryption.FieldEncryptionService;
import com.abcd.hrms.common.security.masking.DataMaskingUtil;
import com.abcd.hrms.workforce.dto.CreateEmployeeRequest;
import com.abcd.hrms.workforce.dto.EmployeeDto;
import com.abcd.hrms.workforce.dto.UpdateEmploymentStatusRequest;
import com.abcd.hrms.workforce.entity.Department;
import com.abcd.hrms.workforce.entity.Employee;
import com.abcd.hrms.workforce.entity.Employment;
import com.abcd.hrms.workforce.entity.LegalEntity;
import com.abcd.hrms.workforce.entity.Location;
import com.abcd.hrms.workforce.entity.ManagerRelationship;
import com.abcd.hrms.workforce.repository.DepartmentRepository;
import com.abcd.hrms.workforce.repository.EmployeeRepository;
import com.abcd.hrms.workforce.repository.EmploymentRepository;
import com.abcd.hrms.workforce.repository.LegalEntityRepository;
import com.abcd.hrms.workforce.repository.LocationRepository;
import com.abcd.hrms.workforce.repository.ManagerRelationshipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmploymentRepository employmentRepository;
    private final LegalEntityRepository legalEntityRepository;
    private final DepartmentRepository departmentRepository;
    private final LocationRepository locationRepository;
    private final ManagerRelationshipRepository managerRelationshipRepository;
    private final FieldEncryptionService encryptionService;
    private final OutboxPublisherService outboxPublisherService;

    @Transactional
    public EmployeeDto createEmployee(CreateEmployeeRequest request) {
        UUID companyId = request.getCompanyId() != null ? request.getCompanyId() : TenantContext.getCompanyId();
        if (companyId == null) {
            // Default to ABCD Corp seed id if not set
            companyId = UUID.fromString("d0000000-0000-0000-0000-000000000001");
        }

        if (employeeRepository.existsByWorkEmail(request.getWorkEmail())) {
            throw new ConflictException("Employee", "Work email [" + request.getWorkEmail() + "] already in use");
        }

        String employeeNumber = request.getEmployeeNumber();
        if (employeeNumber == null || employeeNumber.trim().isEmpty()) {
            employeeNumber = "ABCD-" + (1000 + employeeRepository.count());
        } else if (employeeRepository.existsByEmployeeNumber(employeeNumber)) {
            throw new ConflictException("Employee", "Employee number [" + employeeNumber + "] already exists");
        }

        String displayName = request.getFirstName() + (request.getMiddleName() != null ? " " + request.getMiddleName() : "") + " " + request.getLastName();

        // Field-level encryption and masking
        String nationalIdEncrypted = request.getNationalId() != null ? encryptionService.encrypt(request.getNationalId()) : null;
        String nationalIdMasked = request.getNationalId() != null ? DataMaskingUtil.maskNationalId(request.getNationalId()) : null;

        String bankEncrypted = request.getBankAccountNumber() != null ? encryptionService.encrypt(request.getBankAccountNumber()) : null;
        String bankMasked = request.getBankAccountNumber() != null ? DataMaskingUtil.maskBankAccount(request.getBankAccountNumber()) : null;

        Employee employee = Employee.builder()
                .companyId(companyId)
                .employeeNumber(employeeNumber)
                .firstName(request.getFirstName())
                .middleName(request.getMiddleName())
                .lastName(request.getLastName())
                .displayName(displayName)
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .maritalStatus(request.getMaritalStatus())
                .personalEmail(request.getPersonalEmail())
                .workEmail(request.getWorkEmail())
                .phoneNumber(request.getPhoneNumber())
                .nationalIdEncrypted(nationalIdEncrypted)
                .nationalIdMasked(nationalIdMasked)
                .bankAccountEncrypted(bankEncrypted)
                .bankAccountMasked(bankMasked)
                .bankName(request.getBankName())
                .bankIfscOrRouting(request.getBankIfscOrRouting())
                .currentStatus("ACTIVE")
                .build();

        Employee savedEmployee = employeeRepository.save(employee);

        Employment employment = Employment.builder()
                .employee(savedEmployee)
                .legalEntityId(request.getLegalEntityId())
                .departmentId(request.getDepartmentId())
                .locationId(request.getLocationId())
                .employmentType(request.getEmploymentType() != null ? request.getEmploymentType() : "FULL_TIME")
                .designation(request.getDesignation())
                .grade(request.getGrade())
                .joiningDate(request.getJoiningDate())
                .noticePeriodDays(request.getNoticePeriodDays() != null ? request.getNoticePeriodDays() : 60)
                .build();

        Employment savedEmployment = employmentRepository.save(employment);
        savedEmployee.setEmployment(savedEmployment);

        if (request.getManagerEmployeeId() != null) {
            ManagerRelationship rel = ManagerRelationship.builder()
                    .employeeId(savedEmployee.getId())
                    .managerEmployeeId(request.getManagerEmployeeId())
                    .relationshipType("PRIMARY")
                    .isActive(true)
                    .build();
            managerRelationshipRepository.save(rel);
        }

        // Publish EmployeeActivated.v1 via Transactional Outbox
        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("employeeId", savedEmployee.getId());
        eventPayload.put("employeeNumber", savedEmployee.getEmployeeNumber());
        eventPayload.put("workEmail", savedEmployee.getWorkEmail());
        eventPayload.put("displayName", savedEmployee.getDisplayName());
        eventPayload.put("joiningDate", savedEmployment.getJoiningDate().toString());
        eventPayload.put("legalEntityId", savedEmployment.getLegalEntityId());

        CanonicalEvent<Map<String, Object>> event = CanonicalEvent.of(
                "EmployeeActivated.v1",
                "EMPLOYEE",
                savedEmployee.getId().toString(),
                companyId,
                request.getLegalEntityId(),
                "workforce-service",
                TenantContext.getCorrelationId(),
                eventPayload
        );
        outboxPublisherService.enqueueEvent(KafkaTopicConstants.TOPIC_WORKFORCE_EVENTS, "EMPLOYEE", savedEmployee.getId().toString(), event);

        return mapToDto(savedEmployee, savedEmployment);
    }

    @Transactional(readOnly = true)
    public Page<EmployeeDto> listEmployees(Pageable pageable) {
        UUID companyId = TenantContext.getCompanyId() != null ? TenantContext.getCompanyId() : UUID.fromString("d0000000-0000-0000-0000-000000000001");
        return employeeRepository.findByCompanyId(companyId, pageable)
                .map(e -> mapToDto(e, employmentRepository.findByEmployeeId(e.getId()).orElse(null)));
    }

    @Transactional(readOnly = true)
    public EmployeeDto getEmployeeById(UUID id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee", id));
        Employment employment = employmentRepository.findByEmployeeId(id).orElse(null);
        return mapToDto(employee, employment);
    }

    @Transactional
    public EmployeeDto updateStatus(UUID id, UpdateEmploymentStatusRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee", id));

        employee.setCurrentStatus(request.getNewStatus().toUpperCase());
        Employee updated = employeeRepository.save(employee);

        Employment employment = employmentRepository.findByEmployeeId(id).orElse(null);
        if (employment != null && "TERMINATED".equalsIgnoreCase(request.getNewStatus())) {
            employment.setExitDate(request.getEffectiveDate());
            employment.setExitReason(request.getReason());
            employmentRepository.save(employment);
        }

        return mapToDto(updated, employment);
    }

    private EmployeeDto mapToDto(Employee employee, Employment employment) {
        String legalEntityName = null;
        String deptName = null;
        String locName = null;

        if (employment != null) {
            legalEntityName = legalEntityRepository.findById(employment.getLegalEntityId()).map(LegalEntity::getLegalName).orElse(null);
            deptName = departmentRepository.findById(employment.getDepartmentId()).map(Department::getName).orElse(null);
            locName = locationRepository.findById(employment.getLocationId()).map(Location::getName).orElse(null);
        }

        UUID managerId = null;
        String managerName = null;
        Optional<ManagerRelationship> relOpt = managerRelationshipRepository.findByEmployeeIdAndIsActiveTrue(employee.getId());
        if (relOpt.isPresent()) {
            managerId = relOpt.get().getManagerEmployeeId();
            managerName = employeeRepository.findById(managerId).map(Employee::getDisplayName).orElse(null);
        }

        return EmployeeDto.builder()
                .id(employee.getId())
                .companyId(employee.getCompanyId())
                .employeeNumber(employee.getEmployeeNumber())
                .firstName(employee.getFirstName())
                .middleName(employee.getMiddleName())
                .lastName(employee.getLastName())
                .displayName(employee.getDisplayName())
                .dateOfBirth(employee.getDateOfBirth())
                .gender(employee.getGender())
                .maritalStatus(employee.getMaritalStatus())
                .personalEmail(employee.getPersonalEmail())
                .workEmail(employee.getWorkEmail())
                .phoneNumber(employee.getPhoneNumber())
                .nationalIdMasked(employee.getNationalIdMasked())
                .bankAccountMasked(employee.getBankAccountMasked())
                .bankName(employee.getBankName())
                .bankIfscOrRouting(employee.getBankIfscOrRouting())
                .currentStatus(employee.getCurrentStatus())
                .employmentId(employment != null ? employment.getId() : null)
                .legalEntityId(employment != null ? employment.getLegalEntityId() : null)
                .legalEntityName(legalEntityName)
                .departmentId(employment != null ? employment.getDepartmentId() : null)
                .departmentName(deptName)
                .locationId(employment != null ? employment.getLocationId() : null)
                .locationName(locName)
                .employmentType(employment != null ? employment.getEmploymentType() : null)
                .designation(employment != null ? employment.getDesignation() : null)
                .grade(employment != null ? employment.getGrade() : null)
                .joiningDate(employment != null ? employment.getJoiningDate() : null)
                .confirmationDate(employment != null ? employment.getConfirmationDate() : null)
                .noticePeriodDays(employment != null ? employment.getNoticePeriodDays() : 0)
                .exitDate(employment != null ? employment.getExitDate() : null)
                .exitReason(employment != null ? employment.getExitReason() : null)
                .managerEmployeeId(managerId)
                .managerName(managerName)
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .build();
    }
}
