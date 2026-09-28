package com.nashtech.learning.library.validation;

import com.nashtech.learning.library.dto.EmployeeRequest;
import com.nashtech.learning.library.exception.InvalidEmployeeDataException;
import com.nashtech.learning.library.model.Department;
import com.nashtech.learning.library.model.EmployeeStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmployeeValidatorTest {

    @Test
    @DisplayName("Validate succeeds for valid employee request")
    void shouldPassForValidRequest() {
        EmployeeRequest request = new EmployeeRequest(
                "Jane",
                "Smith",
                "jane.smith@nashtechglobal.com",
                Department.ENGINEERING,
                95000.0,
                EmployeeStatus.ACTIVE,
                LocalDate.of(2023, 1, 15)
        );

        assertThatCode(() -> EmployeeValidator.validate(request))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Validate fails when salary is below minimum")
    void shouldThrowWhenSalaryTooLow() {
        EmployeeRequest request = new EmployeeRequest(
                "Jane",
                "Smith",
                "jane.smith@nashtechglobal.com",
                Department.ENGINEERING,
                500.0,
                EmployeeStatus.ACTIVE,
                LocalDate.now()
        );

        assertThatThrownBy(() -> EmployeeValidator.validate(request))
                .isInstanceOf(InvalidEmployeeDataException.class)
                .hasMessageContaining("Salary must be between");
    }

    @Test
    @DisplayName("Validate fails when email format is invalid")
    void shouldThrowWhenEmailInvalid() {
        EmployeeRequest request = new EmployeeRequest(
                "Jane",
                "Smith",
                "invalid-email-string",
                Department.FINANCE,
                50000.0,
                EmployeeStatus.ACTIVE,
                LocalDate.now()
        );

        assertThatThrownBy(() -> EmployeeValidator.validate(request))
                .isInstanceOf(InvalidEmployeeDataException.class)
                .hasMessageContaining("Invalid email format");
    }
}