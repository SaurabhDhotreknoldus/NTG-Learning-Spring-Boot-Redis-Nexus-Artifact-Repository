package com.nashtech.learning.library.exception;

/**
 * Exception thrown when employee business validations fail.
 */
public class InvalidEmployeeDataException extends RuntimeException {

    public InvalidEmployeeDataException(String message) {
        super(message);
    }

    public InvalidEmployeeDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
