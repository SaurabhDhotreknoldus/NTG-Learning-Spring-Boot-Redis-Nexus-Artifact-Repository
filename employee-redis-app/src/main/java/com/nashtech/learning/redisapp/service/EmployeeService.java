package com.nashtech.learning.redisapp.service;

import com.nashtech.learning.library.dto.EmployeeRequest;
import com.nashtech.learning.library.dto.EmployeeResponse;
import com.nashtech.learning.library.model.Department;

import java.util.List;
import java.util.Map;

public interface EmployeeService {

    EmployeeResponse createEmployee(EmployeeRequest request);

    EmployeeResponse getEmployeeById(Long id);

    List<EmployeeResponse> getAllEmployees();

    List<EmployeeResponse> getEmployeesByDepartment(Department department);

    EmployeeResponse updateEmployee(Long id, EmployeeRequest request);

    void deleteEmployee(Long id);

    void clearAllCache();

    Map<String, Object> inspectCache(Long id);
}
