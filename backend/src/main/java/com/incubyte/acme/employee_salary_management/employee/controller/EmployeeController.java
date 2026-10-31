package com.incubyte.acme.employee_salary_management.employee.controller;

import com.incubyte.acme.employee_salary_management.employee.dto.EmployeeRequest;
import com.incubyte.acme.employee_salary_management.employee.dto.EmployeeResponse;
import com.incubyte.acme.employee_salary_management.employee.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse createEmployee(
            @Valid @RequestBody EmployeeRequest request) {
        return employeeService.createEmployee(request);
    }

    @PutMapping("/{id}")
    public EmployeeResponse updateEmployee(
            @PathVariable UUID id,
            @Valid @RequestBody EmployeeRequest request) {
        return employeeService.updateEmployee(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEmployee(@PathVariable UUID id) {
        employeeService.deleteEmployee(id);
    }

    @GetMapping
    public Page<EmployeeResponse> searchEmployees(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String employeeCode,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String countryCode,
            @PageableDefault(
                    size = 20,
                    sort = "id",
                    direction = Sort.Direction.ASC
            ) Pageable pageable) {

        return employeeService.searchEmployees(
                name, employeeCode, department, countryCode, pageable);
    }
}
