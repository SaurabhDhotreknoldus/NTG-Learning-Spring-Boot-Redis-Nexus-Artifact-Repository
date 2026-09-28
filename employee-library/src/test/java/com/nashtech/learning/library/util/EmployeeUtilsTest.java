package com.nashtech.learning.library.util;

import com.nashtech.learning.library.model.Department;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeeUtilsTest {

    @Test
    @DisplayName("Generate Employee Code with valid department and id")
    void shouldGenerateEmployeeCodeCorrectly() {
        String code = EmployeeUtils.generateEmployeeCode(Department.ENGINEERING, 42L);
        assertThat(code).isEqualTo("ENG-0042");

        String hrCode = EmployeeUtils.generateEmployeeCode(Department.HUMAN_RESOURCES, 7L);
        assertThat(hrCode).isEqualTo("HR-0007");
    }

    @Test
    @DisplayName("Generate Employee Code with null department falls back to GEN")
    void shouldHandleNullDepartmentGracefully() {
        String code = EmployeeUtils.generateEmployeeCode(null, 100L);
        assertThat(code).isEqualTo("GEN-0100");
    }

    @Test
    @DisplayName("Format full name trims and concatenates properly")
    void shouldFormatFullName() {
        String fullName = EmployeeUtils.formatFullName("  John ", " Doe  ");
        assertThat(fullName).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Mask email for privacy")
    void shouldMaskEmail() {
        String masked = EmployeeUtils.maskEmail("john.doe@nashtechglobal.com");
        assertThat(masked).isEqualTo("j***e@nashtechglobal.com");
    }

    @ParameterizedTest
    @CsvSource({
            "ENGINEERING, 100000.0, 15000.0",
            "SALES, 100000.0, 20000.0",
            "FINANCE, 100000.0, 12000.0",
            "HUMAN_RESOURCES, 100000.0, 10000.0",
            "OPERATIONS, 100000.0, 10000.0"
    })
    @DisplayName("Calculate bonus by department")
    void shouldCalculateBonusAccurately(Department department, Double salary, Double expectedBonus) {
        Double bonus = EmployeeUtils.calculateBonus(salary, department);
        assertThat(bonus).isEqualTo(expectedBonus);
    }

    @Test
    @DisplayName("Validate email address format")
    void shouldValidateEmailFormat() {
        assertThat(EmployeeUtils.isValidEmail("alice@company.com")).isTrue();
        assertThat(EmployeeUtils.isValidEmail("invalid-email")).isFalse();
        assertThat(EmployeeUtils.isValidEmail(null)).isFalse();
    }
}