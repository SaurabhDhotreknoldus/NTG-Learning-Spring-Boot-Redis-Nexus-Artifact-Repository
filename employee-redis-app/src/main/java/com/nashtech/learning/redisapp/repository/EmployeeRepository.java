package com.nashtech.learning.redisapp.repository;

import com.nashtech.learning.library.model.Department;
import com.nashtech.learning.library.model.EmployeeStatus;
import com.nashtech.learning.redisapp.entity.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long> {

    Optional<EmployeeEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<EmployeeEntity> findByEmployeeCode(String employeeCode);

    List<EmployeeEntity> findByDepartment(Department department);

    List<EmployeeEntity> findByStatus(EmployeeStatus status);
}
