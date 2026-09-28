# API Testing Evidence & Endpoint Catalog

This document provides complete testing evidence, curl commands, JSON request/response payloads, and HTTP status codes for all REST APIs exposed by **`employee-redis-app`**.

---

## 1. REST Endpoint Catalog

| HTTP Method | Endpoint | Description | Cache Behavior | Success Code |
|-------------|----------|-------------|----------------|--------------|
| `POST` | `/api/employees` | Create a new employee | Evicts `employeeList` cache | `201 Created` |
| `GET` | `/api/employees/{id}` | Retrieve employee by ID | `@Cacheable` (`employees::#id`) | `200 OK` |
| `GET` | `/api/employees` | Retrieve all employees | `@Cacheable` (`employeeList::all`) | `200 OK` |
| `GET` | `/api/employees?department={dept}` | Filter employees by department | Database query | `200 OK` |
| `PUT` | `/api/employees/{id}` | Update employee details | `@CachePut` (refreshes cache) | `200 OK` |
| `DELETE` | `/api/employees/{id}` | Delete employee | `@CacheEvict` (removes from cache) | `200 OK` |
| `GET` | `/api/employees/cache/inspect/{id}` | Inspect Redis key, value & TTL | Direct Redis inspection | `200 OK` |
| `POST` | `/api/employees/cache/clear` | Evict all cached keys | `@CacheEvict` (all entries) | `200 OK` |
| `GET` | `/api/cache-demo/test-cycle/{id}` | Automated latency & cache benchmark | Measures miss vs hit speed | `200 OK` |
| `GET` | `/api/cache-demo/explain` | Caching architecture explanation | Informational | `200 OK` |

---

## 2. API Testing Evidence & Payloads

### 1. Create Employee (`POST /api/employees`)
**cURL Command**:
```bash
curl -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Peter",
    "lastName": "Parker",
    "email": "peter.parker@nashtechglobal.com",
    "department": "ENGINEERING",
    "salary": 98000.0,
    "status": "ACTIVE",
    "joiningDate": "2023-05-10"
  }'
```
**HTTP Status**: `201 Created`
**Response Payload**:
```json
{
  "success": true,
  "message": "Employee created successfully",
  "data": {
    "id": 4,
    "employeeCode": "ENG-0004",
    "fullName": "Peter Parker",
    "firstName": "Peter",
    "lastName": "Parker",
    "email": "peter.parker@nashtechglobal.com",
    "maskedEmail": "p***r@nashtechglobal.com",
    "department": "ENGINEERING",
    "salary": 98000.0,
    "estimatedBonus": 14700.0,
    "status": "ACTIVE",
    "joiningDate": "2023-05-10",
    "createdAt": "2026-09-28T17:45:00",
    "updatedAt": "2026-09-28T17:45:00"
  },
  "timestamp": "2026-09-28T17:45:00"
}
```

---

### 2. Get Employee by ID (`GET /api/employees/1`)
**cURL Command**:
```bash
curl http://localhost:8080/api/employees/1
```
**HTTP Status**: `200 OK`
**Response Payload**:
```json
{
  "success": true,
  "message": "Employee retrieved successfully",
  "data": {
    "id": 1,
    "employeeCode": "ENG-0001",
    "fullName": "Alice Johnson",
    "firstName": "Alice",
    "lastName": "Johnson",
    "email": "alice.johnson@nashtechglobal.com",
    "maskedEmail": "a***n@nashtechglobal.com",
    "department": "ENGINEERING",
    "salary": 95000.0,
    "estimatedBonus": 14250.0,
    "status": "ACTIVE",
    "joiningDate": "2023-01-15",
    "createdAt": "2026-09-28T17:40:00",
    "updatedAt": "2026-09-28T17:40:00"
  },
  "timestamp": "2026-09-28T17:45:05"
}
```

---

### 3. Get All Employees (`GET /api/employees`)
**cURL Command**:
```bash
curl http://localhost:8080/api/employees
```
**HTTP Status**: `200 OK`
**Response Payload**:
```json
{
  "success": true,
  "message": "Employees retrieved successfully",
  "data": [
    {
      "id": 1,
      "employeeCode": "ENG-0001",
      "fullName": "Alice Johnson",
      "department": "ENGINEERING",
      "salary": 95000.0,
      "status": "ACTIVE"
    },
    {
      "id": 2,
      "employeeCode": "HR-0002",
      "fullName": "Bob Smith",
      "department": "HUMAN_RESOURCES",
      "salary": 75000.0,
      "status": "ACTIVE"
    },
    {
      "id": 3,
      "employeeCode": "SLS-0003",
      "fullName": "Carol Williams",
      "department": "SALES",
      "salary": 85000.0,
      "status": "ACTIVE"
    }
  ],
  "timestamp": "2026-09-28T17:45:10"
}
```

---

### 4. Update Employee (`PUT /api/employees/1`)
**cURL Command**:
```bash
curl -X PUT http://localhost:8080/api/employees/1 \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Alice",
    "lastName": "Johnson",
    "email": "alice.johnson@nashtechglobal.com",
    "department": "ENGINEERING",
    "salary": 115000.0,
    "status": "ACTIVE",
    "joiningDate": "2023-01-15"
  }'
```
**HTTP Status**: `200 OK`
**Response Payload**:
```json
{
  "success": true,
  "message": "Employee updated and cache refreshed successfully",
  "data": {
    "id": 1,
    "employeeCode": "ENG-0001",
    "fullName": "Alice Johnson",
    "salary": 115000.0,
    "estimatedBonus": 17250.0,
    "status": "ACTIVE"
  },
  "timestamp": "2026-09-28T17:45:15"
}
```

---

### 5. Delete Employee (`DELETE /api/employees/1`)
**cURL Command**:
```bash
curl -X DELETE http://localhost:8080/api/employees/1
```
**HTTP Status**: `200 OK`
**Response Payload**:
```json
{
  "success": true,
  "message": "Employee deleted and evicted from cache successfully",
  "data": null,
  "timestamp": "2026-09-28T17:45:20"
}
```

---

### 6. Validation Error Handling (`POST /api/employees` with invalid data)
**Request Body**:
```json
{
  "firstName": "",
  "lastName": "",
  "email": "invalid-email",
  "department": null,
  "salary": 500.0
}
```
**HTTP Status**: `400 Bad Request`
**Response Payload**:
```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "Request body failed validation constraints",
  "path": "/api/employees",
  "timestamp": "2026-09-28T17:45:25",
  "validationErrors": {
    "firstName": "First name is mandatory",
    "lastName": "Last name is mandatory",
    "email": "Email format is invalid",
    "department": "Department is mandatory",
    "salary": "Salary must be at least 1,000.0"
  }
}
```

---

### 7. Duplicate Email Conflict (`POST /api/employees`)
**HTTP Status**: `409 Conflict`
**Response Payload**:
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Employee with email 'bob.smith@nashtechglobal.com' already exists",
  "path": "/api/employees",
  "timestamp": "2026-09-28T17:45:30"
}
```

---

### 8. Employee Not Found (`GET /api/employees/99999`)
**HTTP Status**: `404 Not Found`
**Response Payload**:
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Employee with ID 99999 not found",
  "path": "/api/employees/99999",
  "timestamp": "2026-09-28T17:45:35"
}
```

---

## 3. Automated Test Suite Execution Summary

Both Maven modules have automated tests with **100% pass rate**:

### `employee-library` Test Results:
```text
[INFO] Running com.nashtech.learning.library.dto.EmployeeDtoTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.nashtech.learning.library.util.EmployeeUtilsTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.nashtech.learning.library.validation.EmployeeValidatorTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] Results: Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### `employee-redis-app` Test Results:
```text
[INFO] Running com.nashtech.learning.redisapp.controller.EmployeeControllerTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.nashtech.learning.redisapp.repository.EmployeeRepositoryTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.nashtech.learning.redisapp.service.EmployeeServiceCacheTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] Results: Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

Total Automated Tests: **27 / 27 Passed (100% Success)**