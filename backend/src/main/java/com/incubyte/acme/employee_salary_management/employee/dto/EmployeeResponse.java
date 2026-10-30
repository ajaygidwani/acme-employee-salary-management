package com.incubyte.acme.employee_salary_management.employee.dto;

import com.incubyte.acme.employee_salary_management.employee.enums.EmploymentStatus;

import java.util.UUID;

public record EmployeeResponse(
        UUID id,
        String employeeCode,
        String name,
        String email,
        String department,
        String designation,
        String countryCode,
        EmploymentStatus employmentStatus
) {}
