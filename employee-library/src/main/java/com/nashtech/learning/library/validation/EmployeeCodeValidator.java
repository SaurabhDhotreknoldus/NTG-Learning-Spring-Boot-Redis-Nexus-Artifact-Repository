package com.nashtech.learning.library.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * ConstraintValidator implementation for ValidEmployeeCode.
 */
public class EmployeeCodeValidator implements ConstraintValidator<ValidEmployeeCode, String> {

    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Z]{2,4}-\\d{4}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true; // Use @NotBlank / @NotNull for null checks
        }
        return CODE_PATTERN.matcher(value.trim()).matches();
    }
}
