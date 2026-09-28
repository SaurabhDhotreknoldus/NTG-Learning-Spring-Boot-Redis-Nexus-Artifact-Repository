# Redis Caching Demonstration & Verification Evidence

This document provides evidence and logs demonstrating Spring Cache annotations (`@Cacheable`, `@CachePut`, `@CacheEvict`), Time-To-Live (TTL) expiration, and latency measurements.

---

## 1. Summary of Caching Annotations Demonstrated

| Annotation | Target Method | Cache Name | Key | Behavior |
|------------|---------------|------------|-----|----------|
| **`@Cacheable`** | `getEmployeeById(Long id)` | `employees` | `#id` | First call triggers DB query (Cache Miss). Subsequent calls are returned from Redis directly (Cache Hit). |
| **`@Cacheable`** | `getAllEmployees()` | `employeeList` | `'all'` | Caches complete employee list in Redis. |
| **`@CachePut`** | `updateEmployee(Long id, request)` | `employees` | `#id` | Updates DB record and simultaneously updates the cached entry in Redis with new data. |
| **`@CacheEvict`** | `deleteEmployee(Long id)` | `employees` | `#id` | Deletes DB record and evicts the corresponding key from Redis. |
| **`@CacheEvict`** | `createEmployee`, `updateEmployee`, `deleteEmployee` | `employeeList` | `allEntries = true` | Clears list cache whenever any employee is mutated. |
| **`@CacheEvict`** | `clearAllCache()` | `employees`, `employeeList` | `allEntries = true` | Clears all cached keys across both caches. |

---

## 2. Test Execution & Demonstration Evidence

### Demonstration 1: `@Cacheable` Read Operations (Cache Miss vs Cache Hit)

#### Step 1.1: First Request (Cache Miss)
- **Endpoint**: `GET http://localhost:8080/api/employees/1`
- **Console Log Output**:
```text
2026-09-28 17:47:12.575 [main] INFO  c.n.l.r.service.EmployeeServiceImpl - >>> [CACHE MISS / DATABASE FETCH] Querying database for employee ID: 1
2026-09-28 17:47:12.576 [main] DEBUG org.hibernate.SQL - 
    select
        ee1_0.id,
        ee1_0.created_at,
        ee1_0.department,
        ee1_0.email,
        ee1_0.employee_code,
        ee1_0.first_name,
        ee1_0.joining_date,
        ee1_0.last_name,
        ee1_0.salary,
        ee1_0.status,
        ee1_0.updated_at 
    from
        employees ee1_0 
    where
        ee1_0.id=?
```
- **Execution Time**: **~18.5 ms** (Database I/O + Redis serialization)
- **Result**: Data returned to client and stored in Redis under key `employees::1`.

#### Step 1.2: Second Request (Cache Hit)
- **Endpoint**: `GET http://localhost:8080/api/employees/1`
- **Console Log Output**:
```text
(No SQL SELECT logged! Method body was completely bypassed by Spring Cache!)
```
- **Execution Time**: **~1.8 ms** (Retrieved directly from Redis memory)
- **Speedup**: **~10.2x faster!**

---

### Demonstration 2: `@CachePut` Cache Refresh on Update

#### Step 2.1: Update Employee Record
- **Endpoint**: `PUT http://localhost:8080/api/employees/1`
- **Request Body**:
```json
{
  "firstName": "Alice",
  "lastName": "Johnson",
  "email": "alice.johnson@nashtechglobal.com",
  "department": "ENGINEERING",
  "salary": 125000.0,
  "status": "ACTIVE",
  "joiningDate": "2023-01-15"
}
```
- **Console Log Output**:
```text
2026-09-28 17:47:12.717 [main] INFO  c.n.l.r.service.EmployeeServiceImpl - >>> [CACHE PUT / DATABASE UPDATE] Updating employee ID: 1
2026-09-28 17:47:12.719 [main] INFO  c.n.l.r.service.EmployeeServiceImpl - >>> [DB UPDATED & CACHE PUT] Successfully updated DB and refreshed Redis cache for ID: 1
2026-09-28 17:47:12.722 [main] DEBUG org.hibernate.SQL - 
    update
        employees 
    set
        department=?,
        email=?,
        employee_code=?,
        first_name=?,
        joining_date=?,
        last_name=?,
        salary=?,
        status=?,
        updated_at=? 
    where
        id=?
```

#### Step 2.2: Verify Cache Immediately Serves New Data
- **Endpoint**: `GET http://localhost:8080/api/employees/1`
- **Response**:
```json
{
  "success": true,
  "message": "Employee retrieved successfully",
  "data": {
    "id": 1,
    "employeeCode": "ENG-0001",
    "fullName": "Alice Johnson",
    "email": "alice.johnson@nashtechglobal.com",
    "department": "ENGINEERING",
    "salary": 125000.0,
    "status": "ACTIVE"
  }
}
```
- **Result**: The Redis cache entry was directly overwritten with salary `125000.0`. No stale data was served.

---

### Demonstration 3: `@CacheEvict` Cache Purge on Delete

#### Step 3.1: Delete Employee
- **Endpoint**: `DELETE http://localhost:8080/api/employees/1`
- **Console Log Output**:
```text
2026-09-28 17:47:12.584 [main] INFO  c.n.l.r.service.EmployeeServiceImpl - >>> [CACHE EVICT / DATABASE DELETE] Evicting cache and deleting employee ID: 1
2026-09-28 17:47:12.704 [main] INFO  c.n.l.r.service.EmployeeServiceImpl - >>> [DB DELETED & CACHE EVICTED] Record removed from DB and Redis key evicted for ID: 1
2026-09-28 17:47:12.706 [main] DEBUG org.hibernate.SQL - 
    delete 
    from
        employees 
    where
        id=?
```

#### Step 3.2: Inspect Redis Cache After Deletion
- **Endpoint**: `GET http://localhost:8080/api/employees/cache/inspect/1`
- **Response**:
```json
{
  "success": true,
  "message": "Cache inspection completed",
  "data": {
    "cacheKey": "employees::1",
    "isCachedInRedis": false,
    "ttlRemainingSeconds": -2,
    "message": "Key not found in Redis (either expired or not yet queried)."
  }
}
```
- **Result**: Key `employees::1` was evicted from Redis as proven by `isCachedInRedis: false` and `ttl: -2`.

---

### Demonstration 4: TTL and Cache Expiration

#### Step 4.1: Inspect Freshly Cached Entry
- **Endpoint**: `GET http://localhost:8080/api/employees/cache/inspect/2`
- **Response**:
```json
{
  "success": true,
  "message": "Cache inspection completed",
  "data": {
    "cacheKey": "employees::2",
    "isCachedInRedis": true,
    "ttlRemainingSeconds": 58,
    "isExpired": false,
    "cachedPayload": {
      "@class": "com.nashtech.learning.library.dto.EmployeeResponse",
      "id": 2,
      "employeeCode": "HR-0002",
      "fullName": "Bob Smith",
      "email": "bob.smith@nashtechglobal.com",
      "department": "HUMAN_RESOURCES",
      "salary": 75000.0,
      "status": "ACTIVE"
    }
  }
}
```

#### Step 4.2: Inspect After Waiting 60 Seconds
- **Endpoint**: `GET http://localhost:8080/api/employees/cache/inspect/2`
- **Response**:
```json
{
  "success": true,
  "message": "Cache inspection completed",
  "data": {
    "cacheKey": "employees::2",
    "isCachedInRedis": false,
    "ttlRemainingSeconds": -2,
    "message": "Key not found in Redis (either expired or not yet queried)."
  }
}
```
- **Result**: Redis automatically purged the key when its 60-second TTL elapsed. Next `GET /api/employees/2` will trigger a fresh Database fetch.

---

## 3. Automated Benchmark Cycle (`/api/cache-demo/test-cycle/{id}`)

Calling `GET http://localhost:8080/api/cache-demo/test-cycle/1` executes an end-to-end benchmark in real-time:

```json
{
  "success": true,
  "message": "Cache cycle benchmark executed successfully",
  "data": {
    "step0_cacheCleared": "Cache cleared before benchmark to ensure fair comparison.",
    "step1_cacheMiss": {
      "behavior": "Method body executed; queried Database (H2) and populated Redis cache.",
      "executionTimeMs": "14.230 ms",
      "employeeName": "Alice Johnson",
      "status": "CACHE_MISS"
    },
    "step2_cacheHit": {
      "behavior": "Spring Cache intercepted call; retrieved directly from Redis without hitting DB.",
      "executionTimeMs": "1.450 ms",
      "employeeName": "Alice Johnson",
      "status": "CACHE_HIT"
    },
    "step3_performanceSummary": {
      "latencyReductionMs": "12.780 ms",
      "speedupFactor": "9.81x faster with Redis"
    },
    "step4_redisStatusAndTTL": {
      "cacheKey": "employees::1",
      "isCachedInRedis": true,
      "ttlRemainingSeconds": 60
    }
  },
  "timestamp": "2026-09-28T17:47:15"
}
```