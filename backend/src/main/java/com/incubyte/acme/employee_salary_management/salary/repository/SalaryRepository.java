package com.incubyte.acme.employee_salary_management.salary.repository;

import com.incubyte.acme.employee_salary_management.salary.entity.Salary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SalaryRepository extends JpaRepository<Salary, UUID> {

    List<Salary> findByEmployeeIdOrderByEffectiveDateDesc(UUID employeeId);

    @Modifying
    @Query("DELETE FROM Salary s WHERE s.employee.id = :employeeId")
    void deleteByEmployeeId(@Param("employeeId") UUID employeeId);

}
