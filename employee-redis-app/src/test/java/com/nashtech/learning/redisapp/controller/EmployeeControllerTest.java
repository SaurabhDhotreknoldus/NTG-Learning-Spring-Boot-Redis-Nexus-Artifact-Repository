package com.nashtech.learning.redisapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nashtech.learning.library.dto.EmployeeRequest;
import com.nashtech.learning.library.dto.EmployeeResponse;
import com.nashtech.learning.library.exception.EmployeeNotFoundException;
import com.nashtech.learning.library.model.Department;
import com.nashtech.learning.library.model.EmployeeStatus;
import com.nashtech.learning.redisapp.service.EmployeeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    private EmployeeResponse createSampleResponse(Long id) {
        return new EmployeeResponse(
                id,
                "ENG-000" + id,
                "Tony Stark",
                "Tony",
                "Stark",
                "tony.stark@nashtechglobal.com",
                "t***k@nashtechglobal.com",
                Department.ENGINEERING,
                200000.0,
                30000.0,
                EmployeeStatus.ACTIVE,
                LocalDate.of(2023, 1, 1),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("POST /api/employees: Creates employee and returns 201 CREATED")
    void shouldCreateEmployee() throws Exception {
        EmployeeRequest request = new EmployeeRequest(
                "Tony",
                "Stark",
                "tony.stark@nashtechglobal.com",
                Department.ENGINEERING,
                200000.0,
                EmployeeStatus.ACTIVE,
                LocalDate.of(2023, 1, 1)
        );

        when(employeeService.createEmployee(any(EmployeeRequest.class)))
                .thenReturn(createSampleResponse(1L));

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.employeeCode", is("ENG-0001")))
                .andExpect(jsonPath("$.data.fullName", is("Tony Stark")));
    }

    @Test
    @DisplayName("GET /api/employees/{id}: Returns employee and 200 OK")
    void shouldGetEmployeeById() throws Exception {
        when(employeeService.getEmployeeById(1L))
                .thenReturn(createSampleResponse(1L));

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.fullName", is("Tony Stark")));
    }

    @Test
    @DisplayName("GET /api/employees/{id}: Returns 404 NOT_FOUND when employee does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
        when(employeeService.getEmployeeById(999L))
                .thenThrow(new EmployeeNotFoundException(999L));

        mockMvc.perform(get("/api/employees/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Employee with ID 999 not found")));
    }

    @Test
    @DisplayName("GET /api/employees: Returns all employees")
    void shouldGetAllEmployees() throws Exception {
        when(employeeService.getAllEmployees())
                .thenReturn(List.of(createSampleResponse(1L), createSampleResponse(2L)));

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    @DisplayName("PUT /api/employees/{id}: Updates employee and returns 200 OK")
    void shouldUpdateEmployee() throws Exception {
        EmployeeRequest request = new EmployeeRequest(
                "Tony",
                "Stark",
                "tony.stark@nashtechglobal.com",
                Department.ENGINEERING,
                250000.0,
                EmployeeStatus.ACTIVE,
                LocalDate.of(2023, 1, 1)
        );

        when(employeeService.updateEmployee(eq(1L), any(EmployeeRequest.class)))
                .thenReturn(createSampleResponse(1L));

        mockMvc.perform(put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(1)));
    }

    @Test
    @DisplayName("DELETE /api/employees/{id}: Deletes employee and returns 200 OK")
    void shouldDeleteEmployee() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1L);

        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Employee deleted and evicted from cache successfully")));
    }

    @Test
    @DisplayName("POST /api/employees: Fails validation on invalid email and salary")
    void shouldFailValidation() throws Exception {
        EmployeeRequest invalidRequest = new EmployeeRequest(
                "",
                "",
                "invalid-email",
                null,
                100.0,
                EmployeeStatus.ACTIVE,
                LocalDate.now()
        );

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.validationErrors").isMap());
    }
}