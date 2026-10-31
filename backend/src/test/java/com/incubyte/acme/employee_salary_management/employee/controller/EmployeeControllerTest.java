package com.incubyte.acme.employee_salary_management.employee.controller;

import com.incubyte.acme.employee_salary_management.common.DuplicateResourceException;
import com.incubyte.acme.employee_salary_management.common.GlobalExceptionHandler;
import com.incubyte.acme.employee_salary_management.common.ResourceNotFoundException;
import com.incubyte.acme.employee_salary_management.employee.dto.EmployeeRequest;
import com.incubyte.acme.employee_salary_management.employee.dto.EmployeeResponse;
import com.incubyte.acme.employee_salary_management.employee.enums.EmploymentStatus;
import com.incubyte.acme.employee_salary_management.employee.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    private MockMvc mockMvc;
    private UUID employeeId;
    private EmployeeResponse employeeResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(employeeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        employeeId = UUID.randomUUID();
        employeeResponse = new EmployeeResponse(
                employeeId,
                "EMP-001",
                "Alice Johnson",
                "alice@acme.com",
                "Engineering",
                "Software Engineer",
                "US",
                EmploymentStatus.ACTIVE
        );
    }

    @Nested
    @DisplayName("POST /api/v1/employees")
    class CreateEmployeeTests {

        @Test
        @DisplayName("Should return 201 Created and EmployeeResponse when request is valid")
        void createEmployee_Success() throws Exception {
            when(employeeService.createEmployee(any(EmployeeRequest.class))).thenReturn(employeeResponse);

            String requestJson = """
                    {
                        "employeeCode": "EMP-001",
                        "name": "Alice Johnson",
                        "email": "alice@acme.com",
                        "department": "Engineering",
                        "designation": "Software Engineer",
                        "countryCode": "US",
                        "employmentStatus": "ACTIVE"
                    }
                    """;

            mockMvc.perform(post("/api/v1/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", is(employeeId.toString())))
                    .andExpect(jsonPath("$.employeeCode", is("EMP-001")))
                    .andExpect(jsonPath("$.name", is("Alice Johnson")))
                    .andExpect(jsonPath("$.email", is("alice@acme.com")))
                    .andExpect(jsonPath("$.employmentStatus", is("ACTIVE")));

            verify(employeeService).createEmployee(any(EmployeeRequest.class));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when request body has validation errors")
        void createEmployee_ValidationError() throws Exception {
            // Missing name, invalid email, invalid country code length
            String invalidJson = """
                    {
                        "employeeCode": "EMP-001",
                        "name": "",
                        "email": "not-an-email",
                        "department": "Engineering",
                        "designation": "Software Engineer",
                        "countryCode": "USA",
                        "employmentStatus": "ACTIVE"
                    }
                    """;

            mockMvc.perform(post("/api/v1/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.error", is("Bad Request")));

            verify(employeeService, never()).createEmployee(any());
        }

        @Test
        @DisplayName("Should return 409 Conflict when duplicate resource exception is thrown")
        void createEmployee_DuplicateConflict() throws Exception {
            when(employeeService.createEmployee(any(EmployeeRequest.class)))
                    .thenThrow(new DuplicateResourceException("Employee code already exists: EMP-001"));

            String requestJson = """
                    {
                        "employeeCode": "EMP-001",
                        "name": "Alice Johnson",
                        "email": "alice@acme.com",
                        "department": "Engineering",
                        "designation": "Software Engineer",
                        "countryCode": "US",
                        "employmentStatus": "ACTIVE"
                    }
                    """;

            mockMvc.perform(post("/api/v1/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status", is(409)))
                    .andExpect(jsonPath("$.message", containsString("Employee code already exists: EMP-001")));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/employees/{id}")
    class UpdateEmployeeTests {

        @Test
        @DisplayName("Should return 200 OK and updated EmployeeResponse")
        void updateEmployee_Success() throws Exception {
            when(employeeService.updateEmployee(eq(employeeId), any(EmployeeRequest.class)))
                    .thenReturn(employeeResponse);

            String requestJson = """
                    {
                        "employeeCode": "EMP-001",
                        "name": "Alice Johnson",
                        "email": "alice@acme.com",
                        "department": "Engineering",
                        "designation": "Software Engineer",
                        "countryCode": "US",
                        "employmentStatus": "ACTIVE"
                    }
                    """;

            mockMvc.perform(put("/api/v1/employees/{id}", employeeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(employeeId.toString())))
                    .andExpect(jsonPath("$.employeeCode", is("EMP-001")));

            verify(employeeService).updateEmployee(eq(employeeId), any(EmployeeRequest.class));
        }

        @Test
        @DisplayName("Should return 404 Not Found when employee does not exist")
        void updateEmployee_NotFound() throws Exception {
            when(employeeService.updateEmployee(eq(employeeId), any(EmployeeRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Employee not found: " + employeeId));

            String requestJson = """
                    {
                        "employeeCode": "EMP-001",
                        "name": "Alice Johnson",
                        "email": "alice@acme.com",
                        "department": "Engineering",
                        "designation": "Software Engineer",
                        "countryCode": "US",
                        "employmentStatus": "ACTIVE"
                    }
                    """;

            mockMvc.perform(put("/api/v1/employees/{id}", employeeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.message", containsString("Employee not found")));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/employees/{id}")
    class DeleteEmployeeTests {

        @Test
        @DisplayName("Should return 204 No Content on successful deletion")
        void deleteEmployee_Success() throws Exception {
            doNothing().when(employeeService).deleteEmployee(employeeId);

            mockMvc.perform(delete("/api/v1/employees/{id}", employeeId))
                    .andExpect(status().isNoContent());

            verify(employeeService).deleteEmployee(employeeId);
        }

        @Test
        @DisplayName("Should return 404 Not Found when deleting non-existent employee")
        void deleteEmployee_NotFound() throws Exception {
            doThrow(new ResourceNotFoundException("Employee not found: " + employeeId))
                    .when(employeeService).deleteEmployee(employeeId);

            mockMvc.perform(delete("/api/v1/employees/{id}", employeeId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/employees")
    class SearchEmployeesTests {

        @Test
        @DisplayName("Should return 200 OK with paginated list of employees")
        void searchEmployees_Success() throws Exception {
            Page<EmployeeResponse> page = new PageImpl<>(List.of(employeeResponse), org.springframework.data.domain.PageRequest.of(0, 10), 1);

            when(employeeService.searchEmployees(
                    eq("Alice"), eq("EMP"), eq("Engineering"), eq("US"), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/v1/employees")
                            .param("name", "Alice")
                            .param("employeeCode", "EMP")
                            .param("department", "Engineering")
                            .param("countryCode", "US")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].employeeCode", is("EMP-001")))
                    .andExpect(jsonPath("$.content[0].name", is("Alice Johnson")));

            verify(employeeService).searchEmployees(
                    eq("Alice"), eq("EMP"), eq("Engineering"), eq("US"), any(Pageable.class));
        }
    }
}
