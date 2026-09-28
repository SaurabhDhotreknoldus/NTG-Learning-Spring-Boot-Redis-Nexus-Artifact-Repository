package com.nashtech.learning.redisapp.controller;

import com.nashtech.learning.library.dto.ApiResponse;
import com.nashtech.learning.library.dto.EmployeeRequest;
import com.nashtech.learning.library.dto.EmployeeResponse;
import com.nashtech.learning.library.model.Department;
import com.nashtech.learning.redisapp.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(
            @Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse created = employeeService.createEmployee(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employee created successfully", created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployeeById(@PathVariable Long id) {
        EmployeeResponse employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.success("Employee retrieved successfully", employee));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getAllEmployees(
            @RequestParam(required = false) Department department) {
        List<EmployeeResponse> employees;
        if (department != null) {
            employees = employeeService.getEmployeesByDepartment(department);
        } else {
            employees = employeeService.getAllEmployees();
        }
        return ResponseEntity.ok(ApiResponse.success("Employees retrieved successfully", employees));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse updated = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(ApiResponse.success("Employee updated and cache refreshed successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(ApiResponse.success("Employee deleted and evicted from cache successfully", null));
    }

    @GetMapping("/cache/inspect/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> inspectCache(@PathVariable Long id) {
        Map<String, Object> inspection = employeeService.inspectCache(id);
        return ResponseEntity.ok(ApiResponse.success("Cache inspection completed", inspection));
    }

    @PostMapping("/cache/clear")
    public ResponseEntity<ApiResponse<Void>> clearAllCache() {
        employeeService.clearAllCache();
        return ResponseEntity.ok(ApiResponse.success("All employee caches cleared successfully", null));
    }
}
