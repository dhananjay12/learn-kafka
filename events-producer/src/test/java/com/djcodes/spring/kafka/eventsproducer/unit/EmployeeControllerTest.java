package com.djcodes.spring.kafka.eventsproducer.unit;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.djcodes.spring.kafka.eventsproducer.controller.EmployeeController;
import com.djcodes.spring.kafka.eventsproducer.domain.Employee;
import com.djcodes.spring.kafka.eventsproducer.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EmployeeController.class)
@AutoConfigureMockMvc
public class EmployeeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    EmployeeService employeeService;

    @Test
    public void createEmployee_201() throws Exception {

        //Arrange
        Employee employeeRequest = Employee.builder().name("John").title("Backend Developer")
            .email("john@mycompany.com").build();
        String requestJson = objectMapper.writeValueAsString(employeeRequest);
        when(employeeService.saveEmployee(isA(Employee.class))).thenReturn(null);

        //Act
        mockMvc.perform(post("/employee").contentType(MediaType.APPLICATION_JSON)
            .content(requestJson))
            .andExpect(status().isCreated());

    }

    @Test
    public void createEmployee_400() throws Exception {

        //Arrange

        //Empty employee
        Employee employeeRequest = Employee.builder().build();
        String requestJson = objectMapper.writeValueAsString(employeeRequest);
        when(employeeService.saveEmployee(isA(Employee.class))).thenReturn(null);

        //Act
        mockMvc.perform(post("/employee").contentType(MediaType.APPLICATION_JSON)
            .content(requestJson))
            .andExpect(status().isBadRequest())
        .andExpect(content().string("email - Email can not be null!, name - Name can not be null!, title - Title can not be null!"));;

    }

}
