package com.abcd.hrms.workforce;

import com.abcd.hrms.common.messaging.outbox.OutboxPublisherService;
import com.abcd.hrms.common.security.encryption.FieldEncryptionService;
import com.abcd.hrms.workforce.dto.CreateEmployeeRequest;
import com.abcd.hrms.workforce.dto.EmployeeDto;
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
import com.abcd.hrms.workforce.repository.ManagerRelationshipRepository;
import com.abcd.hrms.workforce.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeService Master Lifecycle Tests")
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmploymentRepository employmentRepository;

    @Mock
    private LegalEntityRepository legalEntityRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private ManagerRelationshipRepository managerRelationshipRepository;

    @Mock
    private FieldEncryptionService encryptionService;

    @Mock
    private OutboxPublisherService outboxPublisherService;

    @InjectMocks
    private EmployeeService employeeService;

    private UUID legalEntityId;
    private UUID departmentId;
    private UUID locationId;

    @BeforeEach
    void setUp() {
        legalEntityId = UUID.randomUUID();
        departmentId = UUID.randomUUID();
        locationId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should successfully create employee, encrypt PII, and enqueue Outbox event")
    void testCreateEmployee() {
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .firstName("Jane")
                .lastName("Doe")
                .workEmail("jane.doe@abcd.corp")
                .dateOfBirth(LocalDate.of(1995, 4, 10))
                .legalEntityId(legalEntityId)
                .departmentId(departmentId)
                .locationId(locationId)
                .designation("Software Engineer")
                .joiningDate(LocalDate.now())
                .nationalId("ABCDE9999Z")
                .bankAccountNumber("9876543210")
                .build();

        when(employeeRepository.existsByWorkEmail("jane.doe@abcd.corp")).thenReturn(false);
        when(encryptionService.encrypt(any())).thenReturn("encrypted_cipher_text");

        Employee savedEmployee = Employee.builder()
                .employeeNumber("ABCD-1005")
                .firstName("Jane")
                .lastName("Doe")
                .displayName("Jane Doe")
                .workEmail("jane.doe@abcd.corp")
                .currentStatus("ACTIVE")
                .nationalIdMasked("XXXXX9999Z")
                .bankAccountMasked("XXXXXX3210")
                .build();
        savedEmployee.setId(UUID.randomUUID());

        Employment savedEmployment = Employment.builder()
                .employee(savedEmployee)
                .legalEntityId(legalEntityId)
                .departmentId(departmentId)
                .locationId(locationId)
                .designation("Software Engineer")
                .joiningDate(LocalDate.now())
                .build();
        savedEmployment.setId(UUID.randomUUID());

        when(employeeRepository.save(any())).thenReturn(savedEmployee);
        when(employmentRepository.save(any())).thenReturn(savedEmployment);
        when(legalEntityRepository.findById(legalEntityId)).thenReturn(Optional.of(LegalEntity.builder().entityCode("ABCD_INDIA").legalName("ABCD India").build()));
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(Department.builder().name("Engineering").build()));
        when(locationRepository.findById(locationId)).thenReturn(Optional.of(Location.builder().name("Bangalore HQ").build()));

        EmployeeDto result = employeeService.createEmployee(request);

        assertNotNull(result);
        assertEquals("Jane Doe", result.getDisplayName());
        assertEquals("XXXXX9999Z", result.getNationalIdMasked());
        assertEquals("XXXXXX3210", result.getBankAccountMasked());
        assertEquals("Engineering", result.getDepartmentName());
        assertEquals("Bangalore HQ", result.getLocationName());
        verify(outboxPublisherService, times(1)).enqueueEvent(any(), eq("EMPLOYEE"), any(), any());
    }
}
