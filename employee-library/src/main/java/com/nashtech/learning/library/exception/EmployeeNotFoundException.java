package com.nashtech.learning.library.exception;

/**
 * Exception thrown when a requested employee is not found in database or cache.
 */
public class EmployeeNotFoundException extends RuntimeException {

    private final Long employeeId;

    public EmployeeNotFoundException(Long id) {
        super(String.format("Employee with ID %d not found", id));
        this.employeeId = id;
    }

    public EmployeeNotFoundException(String message) {
        super(message);
        this.employeeId = null;
    }

    public Long getEmployeeId() {
        return employeeId;
    }
}
