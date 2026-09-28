package com.nashtech.learning.redisapp.repository;

import com.nashtech.learning.library.model.Department;
import com.nashtech.learning.library.model.EmployeeStatus;
import com.nashtech.learning.redisapp.entity.EmployeeEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
    }

    @Test
    @DisplayName("Should persist and retrieve EmployeeEntity from database")
    void shouldPersistAndFindEmployee() {
        EmployeeEntity entity = new EmployeeEntity(
                "ENG-0099",
                "Clark",
                "Kent",
                "clark.kent@nashtechglobal.com",
                Department.ENGINEERING,
                90000.0,
                EmployeeStatus.ACTIVE,
                LocalDate.now()
        );

        EmployeeEntity saved = employeeRepository.save(entity);
        assertThat(saved.getId()).isNotNull();

        Optional<EmployeeEntity> found = employeeRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("clark.kent@nashtechglobal.com");
        assertThat(found.get().getEmployeeCode()).isEqualTo("ENG-0099");
    }

    @Test
    @DisplayName("Should check existence and retrieve by email")
    void shouldCheckAndFindByEmail() {
        EmployeeEntity entity = new EmployeeEntity(
                "HR-0088",
                "Diana",
                "Prince",
                "diana.prince@nashtechglobal.com",
                Department.HUMAN_RESOURCES,
                85000.0,
                EmployeeStatus.ACTIVE,
                LocalDate.now()
        );

        employeeRepository.save(entity);

        assertThat(employeeRepository.existsByEmail("diana.prince@nashtechglobal.com")).isTrue();
        assertThat(employeeRepository.existsByEmail("nonexistent@nashtechglobal.com")).isFalse();

        Optional<EmployeeEntity> found = employeeRepository.findByEmail("diana.prince@nashtechglobal.com");
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Diana");
    }

    @Test
    @DisplayName("Should filter employees by department")
    void shouldFindByDepartment() {
        EmployeeEntity eng1 = new EmployeeEntity("ENG-0101", "Dev1", "L", "dev1@test.com", Department.ENGINEERING, 70000.0, EmployeeStatus.ACTIVE, LocalDate.now());
        EmployeeEntity eng2 = new EmployeeEntity("ENG-0102", "Dev2", "L", "dev2@test.com", Department.ENGINEERING, 75000.0, EmployeeStatus.ACTIVE, LocalDate.now());
        EmployeeEntity fin1 = new EmployeeEntity("FIN-0103", "Fin1", "L", "fin1@test.com", Department.FINANCE, 80000.0, EmployeeStatus.ACTIVE, LocalDate.now());

        employeeRepository.saveAll(List.of(eng1, eng2, fin1));

        List<EmployeeEntity> engList = employeeRepository.findByDepartment(Department.ENGINEERING);
        assertThat(engList).hasSize(2);

        List<EmployeeEntity> finList = employeeRepository.findByDepartment(Department.FINANCE);
        assertThat(finList).hasSize(1);
    }
}