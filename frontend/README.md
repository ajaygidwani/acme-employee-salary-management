# ACME Employee & Salary Management - Frontend

A minimal, clean, single-page frontend written in **AngularJS (1.8)** to demonstrate the Spring Boot Employee and Salary Management backend.

---

## Features

- **Employee Management**:
  - **Browse & Search**: Filter employees in real-time by Name, Employee Code, Department, or Country Code.
  - **Pagination**: Navigate through pages with customizable page size and total record counts.
  - **Create Employee**: Modal form with field validations matching backend constraints (2-letter country code, email validation, unique code/email checks).
  - **Edit Employee**: Update employee attributes seamlessly.
  - **Delete Employee**: Remove employees with confirmation safety check.
  - **One-Click Autofill**: "⚡ Autofill Demo Data" button to quickly create realistic employees during demos.

- **Salary Management**:
  - **Salary History**: Select any employee to view their salary revision history (ordered newest to oldest).
  - **Add Salary**: Record new salary entries with amount (>= 0.01), currency (e.g. USD, EUR, INR, GBP), and effective date.
  - **Formatted Display**: Amounts formatted with currency symbols and two decimals.

- **Backend Integration & Error Handling**:
  - **Health Indicator**: Real-time status pill in the header showing whether the backend on port 8080 is reachable.
  - **Backend Error Mapping**: Displays detailed validation messages and duplicate conflict errors directly from Spring Boot's `GlobalExceptionHandler`.
  - **Zero-Config CORS Proxy**: Built-in lightweight Node.js server (`server.js`) that forwards `/api/*` to `http://localhost:8080` without requiring backend CORS modifications.

---

## Project Structure

```
frontend/
├── index.html                  # Main Single-Page Application HTML
├── server.js                   # Zero-dependency Node.js dev & reverse-proxy server
├── package.json                # Project manifest & npm start script
├── README.md                   # Frontend documentation
├── css/
│   └── styles.css              # Modern, responsive styles (no heavyweight CSS framework needed)
└── js/
    ├── app.js                  # AngularJS module definition
    ├── controllers/
    │   └── mainController.js   # Main application controller & demo logic
    └── services/
        ├── employeeService.js  # Service for /api/v1/employees endpoints
        └── salaryService.js    # Service for /api/v1/employees/{id}/salaries endpoints
```

---

## How to Run

### Step 1: Ensure Backend is Running

Start the Spring Boot backend on its default port `8080`:

```bash
cd backend
./mvnw spring-boot:run
```

*(Ensure PostgreSQL is running as configured in `backend/src/main/resources/application.properties`)*.

### Step 2: Start the Frontend

In the `frontend` folder, run:

```bash
cd frontend
npm start
```

*(Or simply `node server.js` — no `npm install` needed!)*

### Step 3: Open in Browser

Visit **[http://localhost:3000](http://localhost:3000)** in your browser.

- The header should display **"Backend Connected (:8080)"**.
- If your backend is running on a different port or host, click **"⚙ Settings"** in the header to update the API endpoint.

---

## Demonstrated Backend Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/employees` | Search employees with pagination & filters |
| `POST` | `/api/v1/employees` | Create a new employee record |
| `PUT` | `/api/v1/employees/{id}` | Update an existing employee |
| `DELETE` | `/api/v1/employees/{id}` | Delete an employee |
| `GET` | `/api/v1/employees/{employeeId}/salaries` | Fetch salary history for an employee |
| `POST` | `/api/v1/employees/{employeeId}/salaries` | Add a new salary record for an employee |
