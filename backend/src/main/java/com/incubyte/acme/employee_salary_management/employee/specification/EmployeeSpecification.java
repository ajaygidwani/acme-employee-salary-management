package com.incubyte.acme.employee_salary_management.employee.specification;

import com.incubyte.acme.employee_salary_management.employee.entity.Employee;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class EmployeeSpecification {
    public static Specification<Employee> searchEmployees(
            String name,
            String employeeCode,
            String department,
            String countryCode) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(name)) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("name")),
                                "%" + name.trim().toLowerCase() + "%"
                        )
                );
            }

            if (StringUtils.hasText(employeeCode)) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("employeeCode")),
                                "%" + employeeCode.trim().toLowerCase() + "%"
                        )
                );
            }

            if (StringUtils.hasText(department)) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("department")),
                                "%" + department.trim().toLowerCase() + "%"
                        )
                );
            }

            if (StringUtils.hasText(countryCode)) {
                predicates.add(
                        criteriaBuilder.equal(
                                criteriaBuilder.upper(root.get("country")),
                                countryCode.trim().toUpperCase()
                        )
                );
            }

            return criteriaBuilder.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }
}
