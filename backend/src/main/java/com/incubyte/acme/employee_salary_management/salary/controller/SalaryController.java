package com.incubyte.acme.employee_salary_management.salary.controller;

import com.incubyte.acme.employee_salary_management.salary.dto.SalaryRequest;
import com.incubyte.acme.employee_salary_management.salary.dto.SalaryResponse;
import com.incubyte.acme.employee_salary_management.salary.service.SalaryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employees/{employeeId}/salaries")
public class SalaryController {

    private final SalaryService salaryService;

    public SalaryController(SalaryService salaryService) {
        this.salaryService = salaryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SalaryResponse addSalary(
            @PathVariable UUID employeeId,
            @Valid @RequestBody SalaryRequest request) {

        return salaryService.addSalary(employeeId, request);
    }

    @GetMapping
    public List<SalaryResponse> getSalaryHistory(
            @PathVariable UUID employeeId) {

        return salaryService.getSalaryHistory(employeeId);
    }

}
