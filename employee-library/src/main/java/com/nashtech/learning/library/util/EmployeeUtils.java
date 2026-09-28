package com.nashtech.learning.library.util;

import com.nashtech.learning.library.model.Department;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Pattern;

/**
 * Reusable utility methods for Employee processing, formatting, and calculations.
 */
public final class EmployeeUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    );

    private EmployeeUtils() {
        // Private constructor for utility class
    }

    /**
     * Generates a standardized employee code based on department and numeric ID.
     * Example: ENG-0042
     *
     * @param department Department
     * @param id         Numeric ID
     * @return Formatted employee code
     */
    public static String generateEmployeeCode(Department department, Long id) {
        String deptCode = (department != null) ? department.getDepartmentCode() : "GEN";
        long numericId = (id != null) ? id : 0L;
        return String.format("%s-%04d", deptCode, numericId);
    }

    /**
     * Formats full name with proper capitalization and trimming.
     *
     * @param firstName First name
     * @param lastName  Last name
     * @return Formatted full name
     */
    public static String formatFullName(String firstName, String lastName) {
        String f = (firstName != null) ? firstName.trim() : "";
        String l = (lastName != null) ? lastName.trim() : "";
        return (f + " " + l).trim();
    }

    /**
     * Masks an email address for privacy and logging.
     * Example: john.doe@nashtechglobal.com -> j***e@nashtechglobal.com
     *
     * @param email Email address
     * @return Masked email
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        int atIndex = email.indexOf('@');
        String localPart = email.substring(0, atIndex);
        String domainPart = email.substring(atIndex);

        if (localPart.length() <= 2) {
            return localPart.charAt(0) + "***" + domainPart;
        }

        return localPart.charAt(0) + "***" + localPart.charAt(localPart.length() - 1) + domainPart;
    }

    /**
     * Calculates department-specific bonus percentage.
     * Engineering: 15%, Sales: 20%, Finance: 12%, Marketing: 12%, HR: 10%, Operations: 10%
     *
     * @param salary     Base salary
     * @param department Department
     * @return Estimated bonus amount rounded to 2 decimal places
     */
    public static Double calculateBonus(Double salary, Department department) {
        if (salary == null || salary <= 0) {
            return 0.0;
        }

        double percentage = switch (department != null ? department : Department.OPERATIONS) {
            case SALES -> 0.20;
            case ENGINEERING -> 0.15;
            case FINANCE, MARKETING -> 0.12;
            case HUMAN_RESOURCES, OPERATIONS -> 0.10;
        };

        BigDecimal bonus = BigDecimal.valueOf(salary * percentage)
                .setScale(2, RoundingMode.HALF_UP);
        return bonus.doubleValue();
    }

    /**
     * Calculates estimated tax deduction based on standard tax brackets.
     *
     * @param salary Base salary
     * @return Tax amount
     */
    public static Double calculateTaxDeduction(Double salary) {
        if (salary == null || salary <= 0) {
            return 0.0;
        }

        double rate;
        if (salary <= 30000) {
            rate = 0.05;
        } else if (salary <= 60000) {
            rate = 0.10;
        } else if (salary <= 100000) {
            rate = 0.20;
        } else {
            rate = 0.30;
        }

        return BigDecimal.valueOf(salary * rate)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * Validates whether an email string matches standard email formatting.
     *
     * @param email Email string
     * @return true if valid
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }
}
