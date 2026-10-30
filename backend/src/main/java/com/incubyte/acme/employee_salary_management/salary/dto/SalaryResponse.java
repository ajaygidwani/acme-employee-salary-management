package com.incubyte.acme.employee_salary_management.salary.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record SalaryResponse(
        UUID id,
        UUID employeeId,
        BigDecimal amount,
        String currency,
        LocalDate effectiveDate
) {}
