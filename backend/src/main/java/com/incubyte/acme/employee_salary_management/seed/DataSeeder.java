package com.incubyte.acme.employee_salary_management.seed;

import com.incubyte.acme.employee_salary_management.employee.entity.Employee;
import com.incubyte.acme.employee_salary_management.employee.enums.EmploymentStatus;
import com.incubyte.acme.employee_salary_management.employee.repository.EmployeeRepository;
import com.incubyte.acme.employee_salary_management.salary.entity.Salary;
import com.incubyte.acme.employee_salary_management.salary.repository.SalaryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * Production-grade Data Seeder for Development and Demo environments.
 *
 * Runs exclusively when the 'dev' Spring profile is active.
 * Efficiently seeds employee and salary records using JPA batching (EntityManager flush/clear),
 * keeping startup times low and memory consumption minimal.
 *
 * Seeding rule:
 * - If employees already exist in the database, seeding is skipped to preserve state and ensure instant boot.
 * - If the database is empty (count == 0), seeds the configured number of records (default: 10,000).
 * - Optional override: 'app.seed.force-reset=true' clears existing records and re-seeds.
 */
@Component
@Profile("dev")
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {

    private final EmployeeRepository employeeRepository;
    private final SalaryRepository salaryRepository;

    @PersistenceContext
    private final EntityManager entityManager;

    @Value("${app.seed.count:10000}")
    private int seedCount;

    @Value("${app.seed.batch-size:1000}")
    private int batchSize;

    @Value("${app.seed.force-reset:false}")
    private boolean forceReset;

    private static final String SEEDER_USER = "system_seeder";

    private static final String[] FIRST_NAMES = {
            "Alexander", "Amara", "Arjun", "Benjamin", "Carlos", "Chloe", "Daniel", "David",
            "Elena", "Emily", "Ethan", "Fatima", "Gabriel", "Grace", "Hannah", "Hassan",
            "Isabella", "Jack", "James", "Jasmine", "John", "Kavita", "Liam", "Lucas",
            "Maya", "Mei", "Michael", "Mina", "Nathan", "Noah", "Oliver", "Olivia",
            "Priya", "Rahul", "Ravi", "Robert", "Samantha", "Samuel", "Sara", "Siddharth",
            "Sophia", "Sora", "Thomas", "Victoria", "William", "Yuki", "Zara", "Zoe"
    };

    private static final String[] LAST_NAMES = {
            "Anderson", "Brown", "Chen", "Davis", "Dubois", "Garcia", "Gomez", "Gupta",
            "Hernandez", "Ivanov", "Jackson", "Johnson", "Jones", "Kim", "Kowalski", "Kumar",
            "Lee", "Lopez", "Martin", "Miller", "Muller", "Murphy", "Nakamura", "Patel",
            "Perez", "Petrov", "Rao", "Rodriguez", "Rossi", "Sato", "Schmidt", "Sharma",
            "Silva", "Singh", "Smith", "Takahashi", "Tanaka", "Taylor", "Thomas", "Torres",
            "Vargas", "Wang", "Williams", "Wilson", "Yamamoto", "Zhang"
    };

    private static final DepartmentRole[] DEPARTMENTS = {
            new DepartmentRole("Engineering", new String[]{
                    "Software Engineer", "Senior Software Engineer", "DevOps Engineer",
                    "Tech Lead", "Staff Engineer", "Engineering Manager"
            }),
            new DepartmentRole("Product", new String[]{
                    "Associate Product Manager", "Product Manager", "Senior Product Manager", "Group Product Manager"
            }),
            new DepartmentRole("Design", new String[]{
                    "UI/UX Designer", "Product Designer", "Design Lead"
            }),
            new DepartmentRole("Quality Assurance", new String[]{
                    "QA Engineer", "Automation Test Engineer", "QA Lead"
            }),
            new DepartmentRole("Human Resources", new String[]{
                    "HR Coordinator", "Talent Acquisition Specialist", "HR Business Partner", "People Operations Lead"
            }),
            new DepartmentRole("Finance", new String[]{
                    "Financial Analyst", "Senior Accountant", "Finance Controller"
            }),
            new DepartmentRole("Marketing", new String[]{
                    "Marketing Specialist", "Content Strategist", "Growth Lead"
            }),
            new DepartmentRole("Sales", new String[]{
                    "Sales Development Rep", "Account Executive", "Enterprise Sales Lead"
            }),
            new DepartmentRole("Operations", new String[]{
                    "Operations Specialist", "Operations Manager"
            }),
            new DepartmentRole("Legal", new String[]{
                    "Legal Counsel", "Compliance Specialist"
            })
    };

    private static final String[] COUNTRY_CODES = {
            "US", "IN", "GB", "DE", "CA", "SG", "AU", "FR", "NL", "JP"
    };

    private static final String[] CURRENCIES = {
            "USD", "EUR", "INR", "GBP"
    };

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        long currentCount = employeeRepository.count();

        if (forceReset && currentCount > 0) {
            log.warn("Force-reset flag is active. Purging existing salary and employee records...");
            salaryRepository.deleteAllInBatch();
            employeeRepository.deleteAllInBatch();
            entityManager.flush();
            entityManager.clear();
            currentCount = 0;
            log.info("Purge complete.");
        }

        if (currentCount > 0) {
            log.info("Database already contains {} employee records. Skipping data seeding on [dev] profile.", currentCount);
            return;
        }

        executeSeed(seedCount, batchSize);
    }

    private void executeSeed(int count, int batch) {
        log.info("=======================================================================");
        log.info(" Starting Data Seeder on [dev] profile: target = {} employees", count);
        log.info(" Batch size: {}, Memory-optimized EntityManager flush & clear", batch);
        log.info("=======================================================================");

        long startTime = System.currentTimeMillis();
        Random random = new Random(42); // Deterministic seed for reproducible demo datasets
        int totalSalaries = 0;

        for (int i = 1; i <= count; i++) {
            Employee employee = createEmployee(i, random);
            entityManager.persist(employee);

            // Add primary current salary
            Salary currentSalary = createSalary(employee, random, false);
            entityManager.persist(currentSalary);
            totalSalaries++;

            // For ~25% of employees, add a historical past salary to demonstrate revision history
            if (i % 4 == 0) {
                Salary pastSalary = createSalary(employee, random, true);
                entityManager.persist(pastSalary);
                totalSalaries++;
            }

            if (i % batch == 0) {
                entityManager.flush();
                entityManager.clear();
                log.info("Seeded {} / {} employees ({} salaries recorded)...", i, count, totalSalaries);
            }
        }

        entityManager.flush();
        entityManager.clear();

        long durationMs = System.currentTimeMillis() - startTime;
        long throughput = durationMs > 0 ? (count * 1000L / durationMs) : count;

        log.info("=======================================================================");
        log.info(" Seeding successfully completed!");
        log.info(" Total Employees Created : {}", count);
        log.info(" Total Salaries Created  : {}", totalSalaries);
        log.info(" Total Time Elapsed      : {} ms ({} seconds)", durationMs, durationMs / 1000);
        log.info(" Insertion Throughput    : ~{} records/sec", throughput);
        log.info("=======================================================================");
    }

    private Employee createEmployee(int index, Random random) {
        String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
        String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
        String fullName = firstName + " " + lastName;

        DepartmentRole deptRole = DEPARTMENTS[random.nextInt(DEPARTMENTS.length)];
        String designation = deptRole.designations[random.nextInt(deptRole.designations.length)];
        String country = COUNTRY_CODES[random.nextInt(COUNTRY_CODES.length)];

        // Realistic employment status distribution: 82% ACTIVE, 8% ON_LEAVE, 6% INACTIVE, 4% TERMINATED
        int statusRoll = random.nextInt(100);
        EmploymentStatus status;
        if (statusRoll < 82) {
            status = EmploymentStatus.ACTIVE;
        } else if (statusRoll < 90) {
            status = EmploymentStatus.ON_LEAVE;
        } else if (statusRoll < 96) {
            status = EmploymentStatus.INACTIVE;
        } else {
            status = EmploymentStatus.TERMINATED;
        }

        LocalDateTime now = LocalDateTime.now();

        Employee employee = new Employee();
        employee.setEmployeeCode(String.format("EMP-%06d", index));
        employee.setName(fullName);
        employee.setEmail(String.format("emp%06d@acme.corp", index));
        employee.setDepartment(deptRole.department);
        employee.setDesignation(designation);
        employee.setCountry(country);
        employee.setEmploymentStatus(status);
        employee.setCreatedAt(now);
        employee.setUpdatedAt(now);
        employee.setCreatedBy(SEEDER_USER);
        employee.setUpdatedBy(SEEDER_USER);
        return employee;
    }

    private Salary createSalary(Employee employee, Random random, boolean isHistorical) {
        String currency = CURRENCIES[random.nextInt(CURRENCIES.length)];

        // Generate base compensation according to currency
        double baseAmount;
        if ("INR".equals(currency)) {
            baseAmount = 600000 + random.nextInt(2400000); // 6L - 30L INR
        } else if ("GBP".equals(currency)) {
            baseAmount = 40000 + random.nextInt(75000);   // 40k - 115k GBP
        } else if ("EUR".equals(currency)) {
            baseAmount = 45000 + random.nextInt(85000);   // 45k - 130k EUR
        } else {
            baseAmount = 55000 + random.nextInt(110000);  // 55k - 165k USD
        }

        // If historical revision, reduce salary by 10-20% and set date 1-3 years in the past
        LocalDate effectiveDate;
        if (isHistorical) {
            baseAmount = baseAmount * (0.80 + (random.nextDouble() * 0.10));
            int monthsAgo = 14 + random.nextInt(24);
            effectiveDate = LocalDate.now().minusMonths(monthsAgo);
        } else {
            int monthsAgo = random.nextInt(12);
            effectiveDate = LocalDate.now().minusMonths(monthsAgo);
        }

        BigDecimal amount = BigDecimal.valueOf(baseAmount).setScale(2, RoundingMode.HALF_UP);
        LocalDateTime now = LocalDateTime.now();

        Salary salary = new Salary();
        salary.setEmployee(employee);
        salary.setAmount(amount);
        salary.setCurrency(currency);
        salary.setEffectiveDate(effectiveDate);
        salary.setCreatedAt(now);
        salary.setUpdatedAt(now);
        salary.setCreatedBy(SEEDER_USER);
        salary.setUpdatedBy(SEEDER_USER);
        return salary;
    }

    private record DepartmentRole(String department, String[] designations) {}
}
