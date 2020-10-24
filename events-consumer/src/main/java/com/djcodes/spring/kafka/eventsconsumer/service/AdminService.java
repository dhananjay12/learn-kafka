package com.djcodes.spring.kafka.eventsconsumer.service;

import com.djcodes.spring.kafka.eventsconsumer.domain.EmployeeEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AdminService {

    @Autowired
    ObjectMapper objectMapper;

    public void processEmployeeEvent(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        EmployeeEvent employeeEvent = objectMapper.readValue(consumerRecord.value(), EmployeeEvent.class);
        switch (employeeEvent.getEmployeeEventType()) {
            case NEW:
                log.info("New Employee Created");
                log.info(employeeEvent.getEmployee().toString());
                break;
            case UPDATE:
                log.info("Employee Update");
                log.info(employeeEvent.getEmployee().toString());
                break;
            case DELETE:
                log.info("Employee Deleted");
                log.info(employeeEvent.getEmployee().toString());
                break;
            default:
                log.info("Invalid Event Type");
        }


    }


}
