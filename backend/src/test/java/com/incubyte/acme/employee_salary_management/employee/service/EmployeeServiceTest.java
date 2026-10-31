package com.incubyte.acme.employee_salary_management.employee.service;

import com.incubyte.acme.employee_salary_management.common.DuplicateResourceException;
import com.incubyte.acme.employee_salary_management.common.ResourceNotFoundException;
import com.incubyte.acme.employee_salary_management.employee.dto.EmployeeRequest;
import com.incubyte.acme.employee_salary_management.employee.dto.EmployeeResponse;
import com.incubyte.acme.employee_salary_management.employee.entity.Employee;
import com.incubyte.acme.employee_salary_management.employee.enums.EmploymentStatus;
import com.incubyte.acme.employee_salary_management.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private UUID employeeId;
    private Employee employee;
    private EmployeeRequest employeeRequest;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();

        employee = new Employee();
        employee.setId(employeeId);
        employee.setEmployeeCode("EMP-001");
        employee.setName("Alice Johnson");
        employee.setEmail("alice@acme.com");
        employee.setDepartment("Engineering");
        employee.setDesignation("Software Engineer");
        employee.setCountry("US");
        employee.setEmploymentStatus(EmploymentStatus.ACTIVE);

        employeeRequest = new EmployeeRequest(
                "EMP-001",
                "Alice Johnson",
                "alice@acme.com",
                "Engineering",
                "Software Engineer",
                "US",
                EmploymentStatus.ACTIVE
        );
    }

    @Nested
    @DisplayName("createEmployee Tests")
    class CreateEmployeeTests {

        @Test
        @DisplayName("Should successfully create an employee when code and email are unique")
        void createEmployee_Success() {
            when(employeeRepository.existsByEmployeeCode("EMP-001")).thenReturn(false);
            when(employeeRepository.existsByEmail("alice@acme.com")).thenReturn(false);
            when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
                Employee toSave = invocation.getArgument(0);
                toSave.setId(employeeId);
                return toSave;
            });

            EmployeeResponse response = employeeService.createEmployee(employeeRequest);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(employeeId);
            assertThat(response.employeeCode()).isEqualTo("EMP-001");
            assertThat(response.name()).isEqualTo("Alice Johnson");
            assertThat(response.email()).isEqualTo("alice@acme.com");
            assertThat(response.department()).isEqualTo("Engineering");
            assertThat(response.designation()).isEqualTo("Software Engineer");
            assertThat(response.countryCode()).isEqualTo("US");
            assertThat(response.employmentStatus()).isEqualTo(EmploymentStatus.ACTIVE);

            ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
            verify(employeeRepository).save(captor.capture());
            Employee saved = captor.getValue();
            assertThat(saved.getEmployeeCode()).isEqualTo("EMP-001");
            assertThat(saved.getCreatedBy()).isEqualTo("system");
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when employeeCode already exists")
        void createEmployee_DuplicateEmployeeCode_ThrowsException() {
            when(employeeRepository.existsByEmployeeCode("EMP-001")).thenReturn(true);

            assertThatThrownBy(() -> employeeService.createEmployee(employeeRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("Employee code already exists: EMP-001");

            verify(employeeRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when email already exists")
        void createEmployee_DuplicateEmail_ThrowsException() {
            when(employeeRepository.existsByEmployeeCode("EMP-001")).thenReturn(false);
            when(employeeRepository.existsByEmail("alice@acme.com")).thenReturn(true);

            assertThatThrownBy(() -> employeeService.createEmployee(employeeRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("Email already exists: alice@acme.com");

            verify(employeeRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getEmployee Tests")
    class GetEmployeeTests {

        @Test
        @DisplayName("Should return employee response when found by ID")
        void getEmployee_Success() {
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            EmployeeResponse response = employeeService.getEmployee(employeeId);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(employeeId);
            assertThat(response.name()).isEqualTo("Alice Johnson");
            verify(employeeRepository).findById(employeeId);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when employee not found by ID")
        void getEmployee_NotFound_ThrowsException() {
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.getEmployee(employeeId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Employee not found: " + employeeId);
        }
    }

    @Nested
    @DisplayName("getAllEmployees Tests")
    class GetAllEmployeesTests {

        @Test
        @DisplayName("Should return list of all employee responses")
        void getAllEmployees_Success() {
            Employee employee2 = new Employee();
            employee2.setId(UUID.randomUUID());
            employee2.setEmployeeCode("EMP-002");
            employee2.setName("Bob Smith");
            employee2.setEmail("bob@acme.com");
            employee2.setDepartment("Marketing");
            employee2.setDesignation("Marketing Specialist");
            employee2.setCountry("GB");
            employee2.setEmploymentStatus(EmploymentStatus.ACTIVE);

            when(employeeRepository.findAll()).thenReturn(List.of(employee, employee2));

            List<EmployeeResponse> results = employeeService.getAllEmployees();

            assertThat(results).hasSize(2);
            assertThat(results.get(0).employeeCode()).isEqualTo("EMP-001");
            assertThat(results.get(1).employeeCode()).isEqualTo("EMP-002");
            verify(employeeRepository).findAll();
        }
    }

    @Nested
    @DisplayName("updateEmployee Tests")
    class UpdateEmployeeTests {

        @Test
        @DisplayName("Should successfully update employee when no duplicates exist")
        void updateEmployee_Success() {
            EmployeeRequest updateRequest = new EmployeeRequest(
                    "EMP-001-MOD",
                    "Alice J. Smith",
                    "alice.new@acme.com",
                    "Engineering",
                    "Senior Software Engineer",
                    "US",
                    EmploymentStatus.ACTIVE
            );

            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            when(employeeRepository.existsByEmployeeCodeAndIdNot("EMP-001-MOD", employeeId)).thenReturn(false);
            when(employeeRepository.existsByEmailAndIdNot("alice.new@acme.com", employeeId)).thenReturn(false);
            when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

            EmployeeResponse response = employeeService.updateEmployee(employeeId, updateRequest);

            assertThat(response.employeeCode()).isEqualTo("EMP-001-MOD");
            assertThat(response.name()).isEqualTo("Alice J. Smith");
            assertThat(response.email()).isEqualTo("alice.new@acme.com");
            assertThat(response.designation()).isEqualTo("Senior Software Engineer");
            verify(employeeRepository).save(employee);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when updating non-existent employee")
        void updateEmployee_NotFound_ThrowsException() {
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.updateEmployee(employeeId, employeeRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Employee not found: " + employeeId);

            verify(employeeRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when updating with another employee's code")
        void updateEmployee_DuplicateCode_ThrowsException() {
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            when(employeeRepository.existsByEmployeeCodeAndIdNot("EMP-001", employeeId)).thenReturn(true);

            assertThatThrownBy(() -> employeeService.updateEmployee(employeeId, employeeRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("Employee code already exists: EMP-001");

            verify(employeeRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when updating with another employee's email")
        void updateEmployee_DuplicateEmail_ThrowsException() {
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            when(employeeRepository.existsByEmployeeCodeAndIdNot("EMP-001", employeeId)).thenReturn(false);
            when(employeeRepository.existsByEmailAndIdNot("alice@acme.com", employeeId)).thenReturn(true);

            assertThatThrownBy(() -> employeeService.updateEmployee(employeeId, employeeRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("Email already exists: alice@acme.com");

            verify(employeeRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteEmployee Tests")
    class DeleteEmployeeTests {

        @Test
        @DisplayName("Should delete employee when found")
        void deleteEmployee_Success() {
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            employeeService.deleteEmployee(employeeId);

            verify(employeeRepository).findById(employeeId);
            verify(employeeRepository).delete(employee);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when deleting non-existent employee")
        void deleteEmployee_NotFound_ThrowsException() {
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.deleteEmployee(employeeId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Employee not found: " + employeeId);

            verify(employeeRepository, never()).delete(any(Employee.class));
        }
    }

    @Nested
    @DisplayName("searchEmployees Tests")
    class SearchEmployeesTests {

        @Test
        @DisplayName("Should return paginated employee responses")
        void searchEmployees_Success() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Employee> page = new PageImpl<>(List.of(employee), pageable, 1);

            when(employeeRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            Page<EmployeeResponse> result = employeeService.searchEmployees("Alice", "EMP", "Engineering", "US", pageable);

            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).employeeCode()).isEqualTo("EMP-001");
            verify(employeeRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should cap page size to 100 if requested page size is larger")
        void searchEmployees_PageSizeCapped() {
            Pageable largePageable = PageRequest.of(0, 500);
            Page<Employee> page = new PageImpl<>(List.of(employee), PageRequest.of(0, 100), 1);

            when(employeeRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            employeeService.searchEmployees(null, null, null, null, largePageable);

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(employeeRepository).findAll(any(Specification.class), pageableCaptor.capture());
            assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(100);
        }
    }
}
