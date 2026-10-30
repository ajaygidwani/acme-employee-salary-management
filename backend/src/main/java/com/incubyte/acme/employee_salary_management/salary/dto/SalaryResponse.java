package com.incubyte.acme.employee_salary_management.salary.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SalaryResponse(
        Long id,
        Long employeeId,
        BigDecimal amount,
        String currency,
        LocalDate effectiveDate
) {}
