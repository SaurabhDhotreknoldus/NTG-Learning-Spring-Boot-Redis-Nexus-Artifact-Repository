package com.nashtech.learning.library.dto;

import com.nashtech.learning.library.model.Department;
import com.nashtech.learning.library.model.EmployeeStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeeDtoTest {

    @Test
    @DisplayName("EmployeeDto should be Java Serializable for Redis and caching")
    void shouldBeSerializable() throws Exception {
        EmployeeDto dto = new EmployeeDto(
                1L,
                "ENG-0001",
                "Bruce",
                "Wayne",
                "bruce.wayne@nashtechglobal.com",
                Department.ENGINEERING,
                150000.0,
                EmployeeStatus.ACTIVE,
                LocalDate.of(2022, 5, 20),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(byteOut)) {
            out.writeObject(dto);
        }

        byte[] bytes = byteOut.toByteArray();
        assertThat(bytes).isNotEmpty();

        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            Object deserialized = in.readObject();
            assertThat(deserialized).isInstanceOf(EmployeeDto.class);
            EmployeeDto result = (EmployeeDto) deserialized;
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getEmail()).isEqualTo("bruce.wayne@nashtechglobal.com");
            assertThat(result.getDepartment()).isEqualTo(Department.ENGINEERING);
        }
    }
}