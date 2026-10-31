package com.incubyte.acme.employee_salary_management.employee.repository;

import com.incubyte.acme.employee_salary_management.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID>, JpaSpecificationExecutor<Employee> {

    boolean existsByEmployeeCode(String employeeCode);
    boolean existsByEmail(String email);
    boolean existsByEmployeeCodeAndIdNot(String employeeCode, UUID id);
    boolean existsByEmailAndIdNot(String email, UUID id);

}
