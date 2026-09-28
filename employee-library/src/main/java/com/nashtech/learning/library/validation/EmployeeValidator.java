package com.nashtech.learning.library.validation;

import com.nashtech.learning.library.dto.EmployeeRequest;
import com.nashtech.learning.library.exception.InvalidEmployeeDataException;
import com.nashtech.learning.library.util.EmployeeUtils;

import java.time.LocalDate;

/**
 * Programmatic validator for employee business rules and constraints.
 */
public final class EmployeeValidator {

    public static final double MIN_SALARY = 1000.0;
    public static final double MAX_SALARY = 10_000_000.0;

    private EmployeeValidator() {
    }

    /**
     * Validates employee request against business rules.
     *
     * @param request EmployeeRequest
     * @throws InvalidEmployeeDataException if validation fails
     */
    public static void validate(EmployeeRequest request) {
        if (request == null) {
            throw new InvalidEmployeeDataException("Employee request payload cannot be null");
        }

        if (request.getFirstName() == null || request.getFirstName().trim().length() < 2) {
            throw new InvalidEmployeeDataException("First name must have at least 2 characters");
        }

        if (request.getLastName() == null || request.getLastName().trim().length() < 2) {
            throw new InvalidEmployeeDataException("Last name must have at least 2 characters");
        }

        if (!EmployeeUtils.isValidEmail(request.getEmail())) {
            throw new InvalidEmployeeDataException(String.format("Invalid email format: '%s'", request.getEmail()));
        }

        if (request.getDepartment() == null) {
            throw new InvalidEmployeeDataException("Department must be specified");
        }

        if (request.getSalary() == null || request.getSalary() < MIN_SALARY || request.getSalary() > MAX_SALARY) {
            throw new InvalidEmployeeDataException(
                    String.format("Salary must be between %.2f and %.2f", MIN_SALARY, MAX_SALARY)
            );
        }

        if (request.getJoiningDate() != null && request.getJoiningDate().isAfter(LocalDate.now())) {
            throw new InvalidEmployeeDataException("Joining date cannot be in the future");
        }
    }
}
