# ACME Employee & Salary Management System

A production-grade, end-to-end web platform designed for the **HR Manager** persona at ACME Corporation to manage profiles and multi-currency salary revision histories for a **10,000-employee** global workforce, replacing fragile Excel spreadsheets.

---

## 📋 Prerequisites & Local Database Setup

Before running the backend, ensure PostgreSQL is running locally on port `5432`. Create the required database and user matching [`backend/src/main/resources/application.properties`](backend/src/main/resources/application.properties):

### 1. Create PostgreSQL User & Database
Connect to PostgreSQL as a superuser (e.g. via `psql -U postgres`):

```sql
CREATE USER acme_user WITH PASSWORD 'Hello@World';
CREATE DATABASE acme_db OWNER acme_user;
GRANT ALL PRIVILEGES ON DATABASE acme_db TO acme_user;
```

### 2. Database Configuration
The backend connects using the following configuration in `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/acme_db
spring.datasource.username=acme_user
spring.datasource.password=Hello@World
```

*(If your local PostgreSQL instance uses different credentials or a custom port, update `application.properties` accordingly)*.

---

## 🚀 Quick Start

### 1. Backend (Spring Boot)
```bash
cd backend
./mvnw spring-boot:run
```
* **First boot**: Automatically seeds **10,000 employees** and ~12,500 salary records in **~1.5s** on the `dev` profile.
* **Subsequent boots**: Starts instantly in $< 1\text{s}$ (seed-if-empty check preserves your test state).

### 2. Frontend (AngularJS 1.8 SPA)
```bash
cd frontend
npm start
```
* Opens at **[http://localhost:3000](http://localhost:3000)**.
* Built-in Node.js server proxies `/api/*` to `http://localhost:8080/api/*` (zero CORS issues, no build step needed).

### 3. Run Unit Tests (JUnit 5 + Mockito)
```bash
cd backend
./mvnw test
```
* **36 tests**, 0 failures, 100% deterministic in **~2.5 seconds** (no database connection needed).

---

## 📚 Assessment Artifacts & Documentation

Comprehensive documentation has been authored in the [`docs/`](docs/) directory per the assessment evaluation requirements:

- 📄 [**Product Requirements Document (PRD)**](docs/requirements.md): Goal, user persona, functional scope, performance targets, and deliberate exclusions with trade-off reasoning.
- 🏗️ [**System Architecture & Technical Design**](docs/architecture.md): C4 component diagrams, Entity-Relationship (ERD) diagrams, database design, JPA chunked batching for 10k records, and foreign key cascade lifecycle.
- 🤖 [**AI Collaboration & Prompt Log**](docs/ai-collaboration-log.md): Complete chronological prompt history, evolution of the solution, and demonstration of engineering judgment guiding AI acceleration.
- 🎯 [**Assessment Compliance Matrix**](docs/assessment-compliance.md): Item-by-item verification matrix mapping the codebase directly to the assessment expectations.
- 🎬 [**Demonstration & Video Walkthrough Guide**](docs/demo-guide.md): 3-minute video recording script, feature walkthrough, and verification checklist.

---

## 🛠️ Technology Stack

| Layer | Technologies |
|---|---|
| **Backend** | Java 17/21, Spring Boot 4.1.2, Spring Data JPA, Hibernate 7, Jakarta Validation, Lombok |
| **Database** | PostgreSQL 14+, UUID Surrogate Keys, Optimized Batching, Referential Integrity |
| **Frontend** | AngularJS 1.8.2, HTML5, CSS3 Variables, Responsive Design, Zero-Dependency Node.js Reverse Proxy |
| **Testing** | JUnit 5 (Jupiter), Mockito, AssertJ, Spring MockMvc (36 unit tests) |
