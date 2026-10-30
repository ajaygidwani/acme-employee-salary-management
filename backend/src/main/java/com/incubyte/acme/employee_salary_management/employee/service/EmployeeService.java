package com.incubyte.acme.employee_salary_management.employee.service;

import com.incubyte.acme.employee_salary_management.common.ResourceNotFoundException;
import com.incubyte.acme.employee_salary_management.employee.dto.EmployeeRequest;
import com.incubyte.acme.employee_salary_management.employee.dto.EmployeeResponse;
import com.incubyte.acme.employee_salary_management.employee.entity.Employee;
import com.incubyte.acme.employee_salary_management.employee.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        Employee employee = new Employee();
        mapRequestToEntity(request, employee);

        return mapToResponse(employeeRepository.save(employee));
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployee(UUID id) {
        Employee employee = findEmployee(id);
        return mapToResponse(employee);
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public EmployeeResponse updateEmployee(UUID id, EmployeeRequest request) {
        Employee employee = findEmployee(id);
        mapRequestToEntity(request, employee);

        return mapToResponse(employeeRepository.save(employee));
    }

    @Transactional
    public void deleteEmployee(UUID id) {
        Employee employee = findEmployee(id);
        employeeRepository.delete(employee);
    }

    private Employee findEmployee(UUID id) {
        return employeeRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Employee not found: " + id));
    }

    private void mapRequestToEntity(EmployeeRequest request, Employee employee) {
        employee.setEmployeeCode(request.employeeCode());
        employee.setName(request.name());
        employee.setEmail(request.email());
        employee.setDepartment(request.department());
        employee.setDesignation(request.designation());
        employee.setCountry(request.countryCode());
        employee.setEmploymentStatus(request.employmentStatus());
        employee.setCreatedAt(LocalDateTime.now());
        employee.setUpdatedAt(LocalDateTime.now());
        employee.setCreatedBy("system");
    }

    private EmployeeResponse mapToResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getName(),
                employee.getEmail(),
                employee.getDepartment(),
                employee.getDesignation(),
                employee.getCountry(),
                employee.getEmploymentStatus()
        );
    }
}
