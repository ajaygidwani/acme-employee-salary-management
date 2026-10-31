# Demonstration & Evaluation Guide

This guide provides instructions to run the application and a **step-by-step walkthrough script** for demonstrating or recording a video evaluation of the **ACME Employee & Salary Management System**.

---

## 1. Quick Start (2 Steps)

### Step 1: Start the Backend (Spring Boot)
Ensure PostgreSQL is running on port `5432` with database `acme_db` and user `acme_user` created:

```sql
-- In PostgreSQL (psql -U postgres):
CREATE USER acme_user WITH PASSWORD 'Hello@World';
CREATE DATABASE acme_db OWNER acme_user;
GRANT ALL PRIVILEGES ON DATABASE acme_db TO acme_user;
```

Then run the backend:

```bash
cd backend
./mvnw spring-boot:run
```

* **First run**: The `dev` profile data seeder automatically populates **10,000 employees** and ~12,500 salary revision records in **~1.5 seconds**.
* **Subsequent restarts**: Starts instantly in $< 1\text{ second}$ with `Database already contains 10000 employee records. Skipping data seeding.`

### Step 2: Start the Frontend (AngularJS)
In a separate terminal:

```bash
cd frontend
npm start
```

* Opens at **[http://localhost:3000](http://localhost:3000)**.
* Standard Node.js library automatically proxies `/api/*` to `http://localhost:8080/api/*` (zero CORS issues).

---

## 2. Walkthrough Script for Video Demo (3 Minutes)

| Timestamp | Screen / Action | Talking Points for Evaluation Video |
|---|---|---|
| **0:00 - 0:30** | **Header & Health Check** | - Welcome & introduction: ACME Employee & Salary Management system for 10,000 employees.<br>- Highlight the green **"Backend Connected (:8080)"** status pill in the top navigation bar.<br>- Mention the zero-dependency Node proxy serving the AngularJS 1.8 SPA. |
| **0:30 - 1:00** | **10,000 Records & Live Search** | - Point to the total count: **10,000 records**.<br>- Demonstrate rapid pagination across pages (Page 1, 2, 3...).<br>- Type into the filter inputs:<br>&nbsp;&nbsp;• Department: `Engineering`<br>&nbsp;&nbsp;• Country: `IN` or `US`<br>&nbsp;&nbsp;• Name search: `Alice`<br>- Show how Spring Data Specifications execute sub-second server-side filtered lookups. |
| **1:00 - 1:40** | **Master-Detail & Salary History** | - Click on any employee row.<br>- The right-hand panel instantly displays their profile summary and **Salary Revision History**.<br>- Point out multi-currency support (`USD`, `EUR`, `INR`, `GBP`) and chronological dates.<br>- Highlight employees who have multiple salary revisions demonstrating promotion/raise tracking. |
| **1:40 - 2:10** | **Record a Salary Revision** | - Click **"+ Add Salary"** on the selected employee.<br>- Click **"⚡ Autofill Demo Data"** (or enter: Amount `95,000`, Currency `USD`, Date `Today`).<br>- Click **"Record Salary"**.<br>- Show the success notification toast and observe the salary table immediately update with the new top entry. |
| **2:10 - 2:40** | **Create & Edit Employee** | - Click **"+ New Employee"**.<br>- Click **"⚡ Autofill Demo Data"** to auto-populate valid attributes.<br>- Click **"Create Employee"**.<br>- Show that the new employee appears in the table.<br>- Click **"Edit"** on the new employee, update their designation (e.g. to `Principal Engineer`), and save. |
| **2:40 - 3:00** | **Cascading Delete (Referential Integrity)** | - Click **"Delete"** on the employee with salaries.<br>- Show confirmation modal and confirm.<br>- Explain how the backend atomic transaction purges child salary records before deleting the employee, cleanly eliminating foreign key violations. |

---

## 3. Running Unit Tests

To run the complete suite of 36 fast, deterministic unit tests:

```bash
cd backend
./mvnw test
```

**Expected output**:
```
Tests run: 36, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
Total time: ~2.5 s
```
