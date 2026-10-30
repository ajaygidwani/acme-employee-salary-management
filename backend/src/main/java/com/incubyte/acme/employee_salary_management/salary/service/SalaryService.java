package com.incubyte.acme.employee_salary_management.salary.service;

import com.incubyte.acme.employee_salary_management.employee.entity.Employee;
import com.incubyte.acme.employee_salary_management.employee.repository.EmployeeRepository;
import com.incubyte.acme.employee_salary_management.salary.entity.Salary;
import com.incubyte.acme.employee_salary_management.salary.repository.SalaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public Salary addSalary(
            Long employeeId, Salary salary) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Employee not found with ID: " + employeeId));

        salary.setEmployee(employee);

        return salaryRepository.save(salary);
    }

    @Transactional(readOnly = true)
    public List<Salary> getSalaryHistory(Long employeeId) {

        if (!employeeRepository.existsById(employeeId)) {
            throw new RuntimeException(
                    "Employee not found with ID: " + employeeId);
        }

        return salaryRepository
                .findByEmployeeIdOrderByEffectiveDateDesc(employeeId);
    }
}
