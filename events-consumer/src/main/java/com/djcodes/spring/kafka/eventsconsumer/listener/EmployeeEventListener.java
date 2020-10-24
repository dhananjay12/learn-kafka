package com.djcodes.spring.kafka.eventsconsumer.listener;

import com.djcodes.spring.kafka.eventsconsumer.service.AdminService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmployeeEventListener {

    @Autowired
    private AdminService adminService;


    @KafkaListener(topics = {"employee-events"})
    public void onMessage(ConsumerRecord<Integer,String> consumerRecord) throws JsonProcessingException {

        log.info("ConsumerRecord : {} ", consumerRecord );
        adminService.processEmployeeEvent(consumerRecord);

    }

}
