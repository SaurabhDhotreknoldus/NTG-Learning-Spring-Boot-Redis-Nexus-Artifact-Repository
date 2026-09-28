# System Architecture & Technical Design

This document details the architectural design of the end-to-end multi-module solution comprising **`employee-library`**, **Sonatype Nexus 3 Hosted Repository**, **`employee-redis-app`**, **Spring Cache with Redis**, and **Database Persistence**.

---

## 1. High-Level System Architecture

```mermaid
flowchart TB
    subgraph DeveloperWorkstation ["Developer & CI/CD Pipeline"]
        SourceCodeLib["employee-library<br/>(Reusable Components)"]
        SourceCodeApp["employee-redis-app<br/>(Spring Boot REST + Redis)"]
    end

    subgraph NexusRegistry ["Sonatype Nexus OSS 3.x (Port 8081)"]
        NexusHosted["maven-releases (Hosted)<br/>com.nashtech.learning:employee-library:1.0.0"]
        NexusSnapshots["maven-snapshots (Hosted)"]
        NexusPublic["maven-public (Group)"]
    end

    subgraph AppRuntime ["employee-redis-app (Port 8080)"]
        RESTController["REST Controller Layer<br/>(/api/employees, /api/cache-demo)"]
        ServiceLayer["Service Layer with Spring Cache<br/>(@Cacheable, @CachePut, @CacheEvict)"]
        DataAccessLayer["Spring Data JPA Repository Layer<br/>(EmployeeRepository)"]
    end

    subgraph CacheAndData ["Storage & Infrastructure Layer"]
        RedisServer[("Redis Server 7.x<br/>(Port 6379)<br/>Key TTL: 60s / 30s")]
        H2Database[("H2 Database<br/>(In-Memory Persistence)")]
        RedisGUI["Redis Commander<br/>(Port 8082 Web UI)"]
    end

    %% Build & Publish Flow
    SourceCodeLib -->|1. mvn clean deploy| NexusHosted

    %% Consumption Flow
    NexusHosted -->|2. Dependency Resolution| SourceCodeApp

    %% Application Flow
    RESTController --> ServiceLayer
    ServiceLayer <-->|Read / Write Cache| RedisServer
    ServiceLayer <-->|Read / Write Database| DataAccessLayer
    DataAccessLayer <-->|Persist Records| H2Database
    RedisGUI -.->|Inspect Keys| RedisServer
```

---

## 2. Module Breakdown

### Module 1: `employee-library`
- **Purpose**: Encapsulates reusable employee domain models, DTOs, validations, custom exceptions, and utilities.
- **Maven Coordinates**: `com.nashtech.learning:employee-library:1.0.0`
- **Key Responsibilities**:
  - `Department` & `EmployeeStatus` enums.
  - `EmployeeDto`, `EmployeeRequest`, `EmployeeResponse`, `ApiResponse<T>` DTOs.
  - `EmployeeNotFoundException`, `DuplicateEmployeeException`, `InvalidEmployeeDataException`.
  - `EmployeeUtils` (salary bonus calculation, tax deductions, employee code generator `ENG-0001`, email masking).
  - Jakarta Bean Validation constraints (`@ValidEmployeeCode`).
  - Distribution management configured for Nexus hosted releases and snapshots.

### Module 2: Sonatype Nexus 3 Hosted Repository
- **Purpose**: Enterprise artifact repository manager hosting internal company Maven artifacts.
- **Configuration**:
  - Hosted Repository: `maven-releases` (Release Version Policy, Allow Redeploy).
  - Snapshot Repository: `maven-snapshots`.
  - Authentication: Deployment credentials mapped via `settings.xml`.

### Module 3: `employee-redis-app`
- **Purpose**: Spring Boot 3 RESTful microservice consuming `employee-library`, persisting data to database, and utilizing Redis as a cache.
- **Key Responsibilities**:
  - Consumes `employee-library` directly from Nexus.
  - Exposes RESTful CRUD endpoints on `/api/employees`.
  - Spring Data JPA with H2 database (`/h2-console`).
  - Spring Cache abstraction integrated with Lettuce Redis connection pool.
  - Granular Cache management and TTL expiration policies.
  - Comprehensive global exception handling (`@RestControllerAdvice`).

---

## 3. Redis Caching Workflow

```mermaid
sequenceDiagram
    autonumber
    actor Client as HTTP Client (Postman / Browser)
    participant Ctrl as EmployeeController
    participant Cache as Spring Cache Interceptor
    participant Redis as Redis Cache (Port 6379)
    participant Svc as EmployeeServiceImpl
    participant Repo as EmployeeRepository
    participant DB as H2 Database

    Note over Client,DB: Case A: Cache Miss on First GET /api/employees/1
    Client->>Ctrl: GET /api/employees/1
    Ctrl->>Cache: getEmployeeById(1)
    Cache->>Redis: GET "employees::1"
    Redis-->>Cache: null (Cache Miss)
    Cache->>Svc: Invoke method body
    Svc->>Repo: findById(1)
    Repo->>DB: SELECT * FROM employees WHERE id = 1
    DB-->>Repo: EmployeeEntity record
    Repo-->>Svc: EmployeeEntity
    Svc-->>Cache: EmployeeResponse
    Cache->>Redis: SETEX "employees::1" 60s EmployeeResponse
    Cache-->>Ctrl: EmployeeResponse
    Ctrl-->>Client: 200 OK (Data loaded from DB & cached)

    Note over Client,DB: Case B: Cache Hit on Second GET /api/employees/1
    Client->>Ctrl: GET /api/employees/1
    Ctrl->>Cache: getEmployeeById(1)
    Cache->>Redis: GET "employees::1"
    Redis-->>Cache: Cached JSON payload (Cache Hit)
    Cache-->>Ctrl: EmployeeResponse (Method body bypassed!)
    Ctrl-->>Client: 200 OK (Served instantly from Redis)

    Note over Client,DB: Case C: Update via @CachePut (PUT /api/employees/1)
    Client->>Ctrl: PUT /api/employees/1 { new salary: 125000 }
    Ctrl->>Svc: updateEmployee(1, request)
    Svc->>Repo: save(updatedEntity)
    Repo->>DB: UPDATE employees SET salary = 125000 WHERE id = 1
    DB-->>Repo: Updated row
    Svc-->>Cache: Updated EmployeeResponse
    Cache->>Redis: SETEX "employees::1" 60s Updated EmployeeResponse
    Cache->>Redis: DEL "employeeList::all" (Evict list)
    Ctrl-->>Client: 200 OK (DB updated & Redis cache refreshed)

    Note over Client,DB: Case D: Delete via @CacheEvict (DELETE /api/employees/1)
    Client->>Ctrl: DELETE /api/employees/1
    Ctrl->>Svc: deleteEmployee(1)
    Svc->>Repo: deleteById(1)
    Repo->>DB: DELETE FROM employees WHERE id = 1
    Svc-->>Cache: void
    Cache->>Redis: DEL "employees::1" (Evicted from cache)
    Cache->>Redis: DEL "employeeList::all"
    Ctrl-->>Client: 200 OK (Deleted and evicted)
```

---

## 4. Key Design Decisions

1. **JSON Serialization in Redis (`GenericJackson2JsonRedisSerializer`)**:
   Instead of default JDK binary serialization (which is unreadable in Redis CLI and sensitive to JVM class changes), all cached values are stored as human-readable JSON with type headers and ISO-8601 timestamps.
2. **Per-Cache TTL Policies**:
   - `employees` cache: 60 seconds TTL (configurable via `app.cache.employee-ttl-seconds`).
   - `employeeList` cache: 30 seconds TTL (configurable via `app.cache.list-ttl-seconds`).
3. **Resilient Cache Error Handler**:
   If Redis becomes temporarily unavailable, the `SimpleCacheErrorHandler` catches the error, logs a warning, and falls back to querying the database directly rather than failing the client request.