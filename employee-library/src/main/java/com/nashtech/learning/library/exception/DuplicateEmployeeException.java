package com.nashtech.learning.library.exception;

/**
 * Exception thrown when attempting to create an employee with an email or code that already exists.
 */
public class DuplicateEmployeeException extends RuntimeException {

    private final String conflictField;
    private final String conflictValue;

    public DuplicateEmployeeException(String conflictField, String conflictValue) {
        super(String.format("Employee with %s '%s' already exists", conflictField, conflictValue));
        this.conflictField = conflictField;
        this.conflictValue = conflictValue;
    }

    public DuplicateEmployeeException(String message) {
        super(message);
        this.conflictField = null;
        this.conflictValue = null;
    }

    public String getConflictField() {
        return conflictField;
    }

    public String getConflictValue() {
        return conflictValue;
    }
}
