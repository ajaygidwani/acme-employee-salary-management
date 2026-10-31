# ACME Employee & Salary Management System

A production-grade, end-to-end web platform designed for the **HR Manager** persona at ACME Corporation to manage profiles and multi-currency salary revision histories for a **10,000-employee** global workforce, replacing fragile Excel spreadsheets.

---

## 🚀 Quick Start

### 1. Backend (Spring Boot + PostgreSQL)
```bash
cd backend
./mvnw spring-boot:run
```
* **First boot**: Seeds **10,000 employees** and ~12,500 salary records in **~1.5s** on the `dev` profile.
* **Subsequent boots**: Starts instantly in $< 1\text{s}$ (seed-if-empty check).

### 2. Frontend (AngularJS 1.8 SPA)
```bash
cd frontend
npm start
```
* Opens at **[http://localhost:3000](http://localhost:3000)**.
* Built-in Node.js server proxies `/api/*` to `http://localhost:8080/api/*` (zero CORS issues).

### 3. Run Unit Tests (JUnit 5 + Mockito)
```bash
cd backend
./mvnw test
```
* **36 tests**, 0 failures, 100% deterministic in **~2.5 seconds**.

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
