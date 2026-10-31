# Product Requirements Document (PRD)

**Project**: ACME Employee & Salary Management System  
**Persona**: HR Manager  
**Scale**: 10,000 Employees  

---

## 1. Goal & Context

ACME Corporation's HR team currently tracks compensation and profile data for 10,000 employees across multiple countries using manual, error-prone Excel spreadsheets.

**Goal**: Deliver a reliable, web-based system allowing the HR Manager to look up employees, manage profiles, track salary revision histories across countries and currencies, and answer compensation questions with sub-second response times.

---

## 2. Scope & Core Features

### 2.1. Employee Management
* **Unique Identification**: Each employee has a unique UUID and employee code (e.g. `EMP-000001`).
* **Multi-Country Support**: Standard ISO 3166-1 alpha-2 country codes (e.g. `US`, `IN`, `GB`).
* **Status Tracking**: Employment status tracking (`ACTIVE`, `INACTIVE`, `ON_LEAVE`, `TERMINATED`).
* **Search & Server-side Pagination**: Filter by Name, Employee Code, Department, and Country Code across the 10,000-record dataset.
* **Full Lifecycle**: Create, view, edit, and delete employee profiles.

### 2.2. Multi-Currency Salary Management
* **Salary History Timeline**: View historical compensation revisions for an employee, ordered newest to oldest.
* **Multi-Currency**: Support local ISO currencies (`USD`, `EUR`, `INR`, `GBP`).
* **Add Salary Revision**: Record new compensation adjustments with effective dates (`YYYY-MM-DD`).
* **Referential Integrity**: Deleting an employee automatically cleans up associated salary records.

### 2.3. Automated Seeding
* Built-in `dev` profile data seeder that generates 10,000 employees and salary histories on first startup when the database is empty (`count == 0`), enabling instant subsequent restarts.

---

## 3. What We Are Deliberately Leaving Out (And Reasoning)

To keep this project focused, reliable, and grounded, the following technical and feature items were deliberately excluded:

1. **Custom DB Indexing on Filter Columns (`department`, `country`, `name`)**:
   * *Reasoning*: For 10,000 records, PostgreSQL primary key and unique constraint indexes (`employeeCode`, `email`) already yield sub-50ms lookups. Adding composite or trigram indexes for `LIKE` searches is deferred to future optimization once the dataset exceeds 100,000+ rows.

2. **Bulk CSV / Excel Upload & Export**:
   * *Reasoning*: While HR currently uses Excel, building a file upload and parsing pipeline was set aside in favor of automated backend seeding (`DataSeeder`) and direct web UI forms, which fully demonstrate system capabilities without file-parsing edge cases.

3. **Soft Deletes (`deleted_at` flag)**:
   * *Reasoning*: Kept the data model clean with direct, transactional cascading hard deletes (`deleteByEmployeeId` before `deleteEmployee`), avoiding the complexity of Hibernate `@SQLDelete` filters and unique constraint conflicts on re-hiring.

4. **Multi-Currency Exchange Rate Conversion**:
   * *Reasoning*: Salaries are stored and displayed in their paid local currency (`USD`, `EUR`, `INR`, `GBP`). Converting across currencies requires live FX rate tracking, which is unnecessary for basic compensation record-keeping.
