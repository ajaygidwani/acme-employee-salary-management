package com.incubyte.acme.employee_salary_management.salary.service;

import com.incubyte.acme.employee_salary_management.common.ResourceNotFoundException;
import com.incubyte.acme.employee_salary_management.employee.entity.Employee;
import com.incubyte.acme.employee_salary_management.employee.repository.EmployeeRepository;
import com.incubyte.acme.employee_salary_management.salary.dto.SalaryRequest;
import com.incubyte.acme.employee_salary_management.salary.dto.SalaryResponse;
import com.incubyte.acme.employee_salary_management.salary.entity.Salary;
import com.incubyte.acme.employee_salary_management.salary.repository.SalaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SalaryService {
    private final EmployeeRepository employeeRepository;
    private final SalaryRepository salaryRepository;

    public SalaryService(
            EmployeeRepository employeeRepository,
            SalaryRepository salaryRepository) {
        this.employeeRepository = employeeRepository;
        this.salaryRepository = salaryRepository;
    }

    @Transactional
    public SalaryResponse addSalary(UUID employeeId, SalaryRequest request) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new RuntimeException("Employee not found: " + employeeId));

        Salary salary = new Salary();
        salary.setEmployee(employee);
        salary.setAmount(request.amount());
        salary.setCurrency(request.currency());
        salary.setEffectiveDate(request.effectiveDate());
        salary.setCreatedAt(LocalDateTime.now());
        salary.setUpdatedAt(LocalDateTime.now());
        salary.setCreatedBy("system");

        return mapToResponse(salaryRepository.save(salary));
    }

    @Transactional(readOnly = true)
    public List<SalaryResponse> getSalaryHistory(UUID employeeId) {

        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found: " + employeeId);
        }

        return salaryRepository
                .findByEmployeeIdOrderByEffectiveDateDesc(employeeId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private SalaryResponse mapToResponse(Salary salary) {
        return new SalaryResponse(
                salary.getId(),
                salary.getEmployee().getId(),
                salary.getAmount(),
                salary.getCurrency(),
                salary.getEffectiveDate()
        );
    }
}
