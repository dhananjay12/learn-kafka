package com.djcodes.spring.kafka.eventsproducer.controller;


import com.djcodes.spring.kafka.eventsproducer.domain.Employee;
import com.djcodes.spring.kafka.eventsproducer.exceptions.EntityNotFoundException;
import com.djcodes.spring.kafka.eventsproducer.service.EmployeeService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    EmployeeService employeeService;

    @GetMapping("/{id}")
    public Employee employee(@PathVariable int id) throws EntityNotFoundException {

        return employeeService.findEmployeeById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Employee create(@Valid @RequestBody Employee employee) throws IllegalAccessException{

        return employeeService.saveEmployee(employee);
    }

    @PutMapping("/{id}")
    public void update(@PathVariable int id, @Valid @RequestBody Employee employee) throws EntityNotFoundException {

        employeeService.updateEmployee(id, employee);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) throws EntityNotFoundException {

        employeeService.deleteEmployee(id);
    }


}
