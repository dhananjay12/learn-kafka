package com.djcodes.spring.kafka.eventsproducer.service;

import com.djcodes.spring.kafka.eventsproducer.domain.EmployeeEvent;
import com.djcodes.spring.kafka.eventsproducer.domain.EmployeeEventType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Service
@Slf4j
public class EmployeeEventProducer {

    @Autowired
    KafkaTemplate<Integer,String> kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    public void sendEventDefault(EmployeeEvent employeeEvent) throws JsonProcessingException {


        Integer key = employeeEvent.getEmployeeEventId();
        String value = objectMapper.writeValueAsString(employeeEvent);

        ListenableFuture<SendResult<Integer,String>> listenableFuture =  kafkaTemplate.sendDefault(key,value);

        listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
            @Override
            public void onFailure(Throwable throwable) {
                log.info("Error sending Message {}", throwable.getMessage());
            }

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                log.info("Message Sent SuccessFully for the key {} and value is {} to partition {}", key, value, result.getRecordMetadata().partition());
            }
        });

    }


}
