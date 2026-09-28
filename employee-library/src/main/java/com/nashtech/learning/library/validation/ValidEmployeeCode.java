package com.nashtech.learning.library.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation to validate employee code format.
 * Format must be 2-4 uppercase characters, followed by a hyphen, and 4 digits (e.g. ENG-0001, HR-0123).
 */
@Documented
@Constraint(validatedBy = EmployeeCodeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEmployeeCode {

    String message() default "Invalid employee code format. Expected format: [DEPT]-[0000] (e.g. ENG-0001)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
