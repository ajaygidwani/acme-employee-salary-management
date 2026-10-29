package com.incubyte.acme.employee_salary_management.employee.entity;

import com.incubyte.acme.employee_salary_management.common.BaseEntity;
import com.incubyte.acme.employee_salary_management.employee.enums.EmploymentStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "employee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 100)
    private String department;

    @Column(nullable = false, length = 100)
    private String designation;

    @Column(nullable = false, length = 2) // ISO 3166-1 alpha-2 eg: IN, US, GB
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmploymentStatus employmentStatus;
}
