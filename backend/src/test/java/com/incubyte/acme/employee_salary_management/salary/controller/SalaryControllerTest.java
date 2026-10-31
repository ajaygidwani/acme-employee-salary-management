package com.incubyte.acme.employee_salary_management.salary.controller;

import com.incubyte.acme.employee_salary_management.common.GlobalExceptionHandler;
import com.incubyte.acme.employee_salary_management.common.ResourceNotFoundException;
import com.incubyte.acme.employee_salary_management.salary.dto.SalaryRequest;
import com.incubyte.acme.employee_salary_management.salary.dto.SalaryResponse;
import com.incubyte.acme.employee_salary_management.salary.service.SalaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SalaryControllerTest {

    @Mock
    private SalaryService salaryService;

    @InjectMocks
    private SalaryController salaryController;

    private MockMvc mockMvc;
    private UUID employeeId;
    private UUID salaryId;
    private SalaryResponse salaryResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(salaryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        employeeId = UUID.randomUUID();
        salaryId = UUID.randomUUID();

        salaryResponse = new SalaryResponse(
                salaryId,
                employeeId,
                new BigDecimal("95000.00"),
                "USD",
                LocalDate.of(2026, 1, 15)
        );
    }

    @Nested
    @DisplayName("POST /api/v1/employees/{employeeId}/salaries")
    class AddSalaryTests {

        @Test
        @DisplayName("Should return 201 Created and SalaryResponse when valid")
        void addSalary_Success() throws Exception {
            when(salaryService.addSalary(eq(employeeId), any(SalaryRequest.class)))
                    .thenReturn(salaryResponse);

            String requestJson = """
                    {
                        "amount": 95000.00,
                        "currency": "USD",
                        "effectiveDate": "2026-01-15"
                    }
                    """;

            mockMvc.perform(post("/api/v1/employees/{employeeId}/salaries", employeeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", is(salaryId.toString())))
                    .andExpect(jsonPath("$.employeeId", is(employeeId.toString())))
                    .andExpect(jsonPath("$.amount", is(95000.00)))
                    .andExpect(jsonPath("$.currency", is("USD")))
                    .andExpect(jsonPath("$.effectiveDate", is("2026-01-15")));

            verify(salaryService).addSalary(eq(employeeId), any(SalaryRequest.class));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when validation fails (amount <= 0, invalid currency)")
        void addSalary_ValidationError() throws Exception {
            String invalidJson = """
                    {
                        "amount": 0.00,
                        "currency": "TOOLONG",
                        "effectiveDate": null
                    }
                    """;

            mockMvc.perform(post("/api/v1/employees/{employeeId}/salaries", employeeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.error", is("Bad Request")));

            verify(salaryService, never()).addSalary(any(), any());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/employees/{employeeId}/salaries")
    class GetSalaryHistoryTests {

        @Test
        @DisplayName("Should return 200 OK with list of salary revisions")
        void getSalaryHistory_Success() throws Exception {
            when(salaryService.getSalaryHistory(employeeId)).thenReturn(List.of(salaryResponse));

            mockMvc.perform(get("/api/v1/employees/{employeeId}/salaries", employeeId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id", is(salaryId.toString())))
                    .andExpect(jsonPath("$[0].amount", is(95000.00)))
                    .andExpect(jsonPath("$[0].currency", is("USD")));

            verify(salaryService).getSalaryHistory(employeeId);
        }

        @Test
        @DisplayName("Should return 404 Not Found when employee does not exist")
        void getSalaryHistory_NotFound() throws Exception {
            when(salaryService.getSalaryHistory(employeeId))
                    .thenThrow(new ResourceNotFoundException("Employee not found: " + employeeId));

            mockMvc.perform(get("/api/v1/employees/{employeeId}/salaries", employeeId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.message", is("Employee not found: " + employeeId)));
        }
    }
}
