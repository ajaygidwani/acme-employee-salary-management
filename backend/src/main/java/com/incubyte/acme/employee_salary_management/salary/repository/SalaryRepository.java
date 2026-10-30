package com.incubyte.acme.employee_salary_management.salary.repository;

import com.incubyte.acme.employee_salary_management.salary.entity.Salary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalaryRepository extends JpaRepository<Salary, Long> {

    List<Salary> findByEmployeeIdOrderByEffectiveDateDesc(Long employeeId);

}
