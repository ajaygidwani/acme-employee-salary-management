package com.incubyte.acme.employee_salary_management.employee.dto;

import com.incubyte.acme.employee_salary_management.employee.enums.EmploymentStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EmployeeRequest (
    @NotBlank
    @Size(max = 100)
    String employeeCode,

    @NotBlank
    @Size(max = 100)
    String name,

    @NotBlank
    @Email
    String email,

    @NotBlank
    String department,

    @NotBlank
    String designation,

    @NotBlank
    @Size(min = 2, max = 2)
    String countryCode,

    @NotNull
    EmploymentStatus employmentStatus
) {}
