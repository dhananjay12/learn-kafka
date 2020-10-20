package com.djcodes.spring.kafka.eventsproducer.service;

import com.djcodes.spring.kafka.eventsproducer.domain.Employee;
import com.djcodes.spring.kafka.eventsproducer.exceptions.EntityNotFoundException;
import com.djcodes.spring.kafka.eventsproducer.exceptions.ValidationException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {

    //Mimicking DB ...Not Fully Thread safe
    private static final AtomicInteger count = new AtomicInteger(0);
    int id = count.incrementAndGet();
    private Map<Integer, Employee> employeeMap = new ConcurrentHashMap<>();

    public Employee findEmployeeById(int id) throws EntityNotFoundException {
        return checkEmployee(id);
    }

    public Employee saveEmployee(Employee employee) throws IllegalAccessException{

        for (Map.Entry<Integer, Employee> entry : employeeMap.entrySet()) {
            if (entry.getValue().getEmail().equalsIgnoreCase(employee.getEmail())) {
                throw new ValidationException("Email already registered");
            }
        }

        employee.setId(id);
        employeeMap.put(id, employee);
        return employee;

    }

    public void updateEmployee(int id, Employee employee) throws EntityNotFoundException {
        checkEmployee(id);
        employeeMap.put(id, employee);
    }

    public void deleteEmployee(int id) throws EntityNotFoundException {
        checkEmployee(id);
        employeeMap.remove(id);
    }

    private Employee checkEmployee(int id) throws EntityNotFoundException {
        Employee result = employeeMap.get(id);
        if (result == null) {
            throw new EntityNotFoundException("Could not find entity with id: " + id);
        }
        return result;
    }


}
