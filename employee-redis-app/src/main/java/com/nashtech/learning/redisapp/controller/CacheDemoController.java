package com.nashtech.learning.redisapp.controller;

import com.nashtech.learning.library.dto.ApiResponse;
import com.nashtech.learning.library.dto.EmployeeResponse;
import com.nashtech.learning.redisapp.config.CacheProperties;
import com.nashtech.learning.redisapp.service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cache-demo")
public class CacheDemoController {

    private final EmployeeService employeeService;
    private final CacheProperties cacheProperties;

    public CacheDemoController(EmployeeService employeeService, CacheProperties cacheProperties) {
        this.employeeService = employeeService;
        this.cacheProperties = cacheProperties;
    }

    @GetMapping("/test-cycle/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> demonstrateCacheCycle(@PathVariable Long id) {
        Map<String, Object> report = new LinkedHashMap<>();

        employeeService.clearAllCache();
        report.put("step0_cacheCleared", "Cache cleared before benchmark to ensure fair comparison.");

        long startMiss = System.nanoTime();
        EmployeeResponse missResponse = employeeService.getEmployeeById(id);
        long elapsedMissNanos = System.nanoTime() - startMiss;
        double missMillis = elapsedMissNanos / 1_000_000.0;

        report.put("step1_cacheMiss", Map.of(
                "behavior", "Method body executed; queried Database (H2) and populated Redis cache.",
                "executionTimeMs", String.format("%.3f ms", missMillis),
                "employeeName", missResponse.getFullName(),
                "status", "CACHE_MISS"
        ));

        long startHit = System.nanoTime();
        EmployeeResponse hitResponse = employeeService.getEmployeeById(id);
        long elapsedHitNanos = System.nanoTime() - startHit;
        double hitMillis = elapsedHitNanos / 1_000_000.0;

        report.put("step2_cacheHit", Map.of(
                "behavior", "Spring Cache intercepted call; retrieved directly from Redis without hitting DB.",
                "executionTimeMs", String.format("%.3f ms", hitMillis),
                "employeeName", hitResponse.getFullName(),
                "status", "CACHE_HIT"
        ));

        double speedupFactor = (hitMillis > 0) ? (missMillis / hitMillis) : 1.0;
        report.put("step3_performanceSummary", Map.of(
                "latencyReductionMs", String.format("%.3f ms", (missMillis - hitMillis)),
                "speedupFactor", String.format("%.2fx faster with Redis", speedupFactor)
        ));

        Map<String, Object> cacheStatus = employeeService.inspectCache(id);
        report.put("step4_redisStatusAndTTL", cacheStatus);

        return ResponseEntity.ok(ApiResponse.success("Cache cycle benchmark executed successfully", report));
    }

    @GetMapping("/explain")
    public ResponseEntity<ApiResponse<Map<String, Object>>> explainCaching() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("architecture", "Spring Boot 3 + Spring Data Redis (Lettuce) + Spring Cache Abstraction");
        info.put("configuredTtlSeconds", cacheProperties.getEmployeeTtlSeconds());
        info.put("listTtlSeconds", cacheProperties.getListTtlSeconds());

        info.put("annotationsUsed", Map.of(
                "@Cacheable", "Used on getEmployeeById(id) - Caches result in 'employees::#id' upon cache miss.",
                "@CachePut", "Used on updateEmployee(id, request) - Updates DB and refreshes 'employees::#id' cache entry.",
                "@CacheEvict", "Used on deleteEmployee(id) - Removes 'employees::#id' from Redis when entity is deleted.",
                "@Caching", "Combines multiple cache evictions/puts across 'employees' and 'employeeList' caches."
        ));

        info.put("ttlDemonstrationSteps", List.of(
                "1. GET /api/employees/{id} -> Result is cached in Redis with " + cacheProperties.getEmployeeTtlSeconds() + "s TTL.",
                "2. GET /api/employees/cache/inspect/{id} -> Inspects Redis key and remaining TTL count down in real-time.",
                "3. Wait for TTL duration -> Redis automatically evicts the expired key.",
                "4. GET /api/employees/cache/inspect/{id} -> Shows key no longer exists in Redis.",
                "5. GET /api/employees/{id} -> Cache Miss occurs, DB is queried again, and cache is refreshed."
        ));

        return ResponseEntity.ok(ApiResponse.success("Caching strategies and TTL explanation", info));
    }
}
