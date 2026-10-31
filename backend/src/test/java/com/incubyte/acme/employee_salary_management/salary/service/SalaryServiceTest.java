package com.incubyte.acme.employee_salary_management.salary.service;

import com.incubyte.acme.employee_salary_management.common.ResourceNotFoundException;
import com.incubyte.acme.employee_salary_management.employee.entity.Employee;
import com.incubyte.acme.employee_salary_management.employee.repository.EmployeeRepository;
import com.incubyte.acme.employee_salary_management.salary.dto.SalaryRequest;
import com.incubyte.acme.employee_salary_management.salary.dto.SalaryResponse;
import com.incubyte.acme.employee_salary_management.salary.entity.Salary;
import com.incubyte.acme.employee_salary_management.salary.repository.SalaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalaryServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private SalaryRepository salaryRepository;

    @InjectMocks
    private SalaryService salaryService;

    private UUID employeeId;
    private UUID salaryId;
    private Employee employee;
    private Salary salary;
    private SalaryRequest salaryRequest;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        salaryId = UUID.randomUUID();

        employee = new Employee();
        employee.setId(employeeId);
        employee.setName("Bob Smith");

        salary = new Salary();
        salary.setId(salaryId);
        salary.setEmployee(employee);
        salary.setAmount(new BigDecimal("85000.00"));
        salary.setCurrency("USD");
        salary.setEffectiveDate(LocalDate.of(2026, 1, 1));

        salaryRequest = new SalaryRequest(
                new BigDecimal("85000.00"),
                "USD",
                LocalDate.of(2026, 1, 1)
        );
    }

    @Nested
    @DisplayName("addSalary Tests")
    class AddSalaryTests {

        @Test
        @DisplayName("Should successfully record salary for existing employee")
        void addSalary_Success() {
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            when(salaryRepository.save(any(Salary.class))).thenAnswer(invocation -> {
                Salary toSave = invocation.getArgument(0);
                toSave.setId(salaryId);
                return toSave;
            });

            SalaryResponse response = salaryService.addSalary(employeeId, salaryRequest);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(salaryId);
            assertThat(response.employeeId()).isEqualTo(employeeId);
            assertThat(response.amount()).isEqualTo(new BigDecimal("85000.00"));
            assertThat(response.currency()).isEqualTo("USD");
            assertThat(response.effectiveDate()).isEqualTo(LocalDate.of(2026, 1, 1));

            ArgumentCaptor<Salary> captor = ArgumentCaptor.forClass(Salary.class);
            verify(salaryRepository).save(captor.capture());
            Salary savedSalary = captor.getValue();
            assertThat(savedSalary.getEmployee()).isEqualTo(employee);
            assertThat(savedSalary.getCreatedBy()).isEqualTo("system");
        }

        @Test
        @DisplayName("Should throw RuntimeException when adding salary for non-existent employee")
        void addSalary_EmployeeNotFound_ThrowsException() {
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> salaryService.addSalary(employeeId, salaryRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Employee not found: " + employeeId);

            verify(salaryRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getSalaryHistory Tests")
    class GetSalaryHistoryTests {

        @Test
        @DisplayName("Should return salary history list ordered by date descending")
        void getSalaryHistory_Success() {
            Salary olderSalary = new Salary();
            olderSalary.setId(UUID.randomUUID());
            olderSalary.setEmployee(employee);
            olderSalary.setAmount(new BigDecimal("75000.00"));
            olderSalary.setCurrency("USD");
            olderSalary.setEffectiveDate(LocalDate.of(2025, 1, 1));

            when(employeeRepository.existsById(employeeId)).thenReturn(true);
            when(salaryRepository.findByEmployeeIdOrderByEffectiveDateDesc(employeeId))
                    .thenReturn(List.of(salary, olderSalary));

            List<SalaryResponse> history = salaryService.getSalaryHistory(employeeId);

            assertThat(history).hasSize(2);
            assertThat(history.get(0).amount()).isEqualTo(new BigDecimal("85000.00"));
            assertThat(history.get(1).amount()).isEqualTo(new BigDecimal("75000.00"));
            verify(salaryRepository).findByEmployeeIdOrderByEffectiveDateDesc(employeeId);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when employee does not exist")
        void getSalaryHistory_EmployeeNotFound_ThrowsException() {
            when(employeeRepository.existsById(employeeId)).thenReturn(false);

            assertThatThrownBy(() -> salaryService.getSalaryHistory(employeeId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Employee not found: " + employeeId);

            verify(salaryRepository, never()).findByEmployeeIdOrderByEffectiveDateDesc(any());
        }
    }
}
