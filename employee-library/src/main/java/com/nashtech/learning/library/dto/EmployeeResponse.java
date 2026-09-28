package com.nashtech.learning.library.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nashtech.learning.library.model.Department;
import com.nashtech.learning.library.model.EmployeeStatus;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Standard response object returned by employee APIs and cached in Redis.
 */
public class EmployeeResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String employeeCode;
    private String fullName;
    private String firstName;
    private String lastName;
    private String email;
    private String maskedEmail;
    private Department department;
    private Double salary;
    private Double estimatedBonus;
    private EmployeeStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate joiningDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public EmployeeResponse() {
    }

    public EmployeeResponse(Long id, String employeeCode, String fullName, String firstName,
                            String lastName, String email, String maskedEmail, Department department,
                            Double salary, Double estimatedBonus, EmployeeStatus status,
                            LocalDate joiningDate, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.employeeCode = employeeCode;
        this.fullName = fullName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.maskedEmail = maskedEmail;
        this.department = department;
        this.salary = salary;
        this.estimatedBonus = estimatedBonus;
        this.status = status;
        this.joiningDate = joiningDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMaskedEmail() {
        return maskedEmail;
    }

    public void setMaskedEmail(String maskedEmail) {
        this.maskedEmail = maskedEmail;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }

    public Double getEstimatedBonus() {
        return estimatedBonus;
    }

    public void setEstimatedBonus(Double estimatedBonus) {
        this.estimatedBonus = estimatedBonus;
    }

    public EmployeeStatus getStatus() {
        return status;
    }

    public void setStatus(EmployeeStatus status) {
        this.status = status;
    }

    public LocalDate getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(LocalDate joiningDate) {
        this.joiningDate = joiningDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "EmployeeResponse{" +
                "id=" + id +
                ", employeeCode='" + employeeCode + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", department=" + department +
                ", salary=" + salary +
                ", status=" + status +
                '}';
    }
}
