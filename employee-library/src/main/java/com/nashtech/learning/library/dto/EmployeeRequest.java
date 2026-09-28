package com.nashtech.learning.library.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nashtech.learning.library.model.Department;
import com.nashtech.learning.library.model.EmployeeStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * Request payload for creating or updating an employee.
 */
public class EmployeeRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "First name is mandatory")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is mandatory")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email format is invalid")
    private String email;

    @NotNull(message = "Department is mandatory")
    private Department department;

    @NotNull(message = "Salary is mandatory")
    @DecimalMin(value = "1000.0", message = "Salary must be at least 1,000.0")
    private Double salary;

    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    @PastOrPresent(message = "Joining date cannot be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate joiningDate;

    public EmployeeRequest() {
    }

    public EmployeeRequest(String firstName, String lastName, String email,
                           Department department, Double salary, EmployeeStatus status,
                           LocalDate joiningDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.department = department;
        this.salary = salary;
        this.status = status != null ? status : EmployeeStatus.ACTIVE;
        this.joiningDate = joiningDate;
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
}
