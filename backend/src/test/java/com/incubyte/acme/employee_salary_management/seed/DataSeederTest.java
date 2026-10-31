package com.incubyte.acme.employee_salary_management.seed;

import com.incubyte.acme.employee_salary_management.employee.entity.Employee;
import com.incubyte.acme.employee_salary_management.employee.repository.EmployeeRepository;
import com.incubyte.acme.employee_salary_management.salary.entity.Salary;
import com.incubyte.acme.employee_salary_management.salary.repository.SalaryRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private SalaryRepository salaryRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private DataSeeder dataSeeder;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(dataSeeder, "seedCount", 10);
        ReflectionTestUtils.setField(dataSeeder, "batchSize", 5);
        ReflectionTestUtils.setField(dataSeeder, "forceReset", false);
    }

    @Test
    @DisplayName("Should skip seeding when database already contains employees")
    void shouldSkipSeedingWhenDatabaseIsNotEmpty() {
        when(employeeRepository.count()).thenReturn(100L);

        dataSeeder.run(new DefaultApplicationArguments(new String[]{}));

        verify(employeeRepository).count();
        verify(entityManager, never()).persist(any());
        verify(entityManager, never()).flush();
        verify(entityManager, never()).clear();
    }

    @Test
    @DisplayName("Should seed employees and salaries when database is empty")
    void shouldSeedWhenDatabaseIsEmpty() {
        when(employeeRepository.count()).thenReturn(0L);

        dataSeeder.run(new DefaultApplicationArguments(new String[]{}));

        verify(employeeRepository).count();
        // Should persist at least 10 employees
        verify(entityManager, atLeast(10)).persist(any(Employee.class));
        // Should persist at least 10 salaries
        verify(entityManager, atLeast(10)).persist(any(Salary.class));
        // Should execute batch flush and clear
        verify(entityManager, atLeastOnce()).flush();
        verify(entityManager, atLeastOnce()).clear();
    }

    @Test
    @DisplayName("Should purge and re-seed when forceReset is true even if records exist")
    void shouldPurgeAndReseedWhenForceResetIsTrue() {
        ReflectionTestUtils.setField(dataSeeder, "forceReset", true);
        when(employeeRepository.count()).thenReturn(50L);

        dataSeeder.run(new DefaultApplicationArguments(new String[]{}));

        verify(salaryRepository).deleteAllInBatch();
        verify(employeeRepository).deleteAllInBatch();
        verify(entityManager, atLeast(10)).persist(any(Employee.class));
    }
}
