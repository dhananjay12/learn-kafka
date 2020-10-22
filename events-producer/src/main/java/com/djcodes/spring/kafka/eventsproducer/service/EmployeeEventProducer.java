package com.djcodes.spring.kafka.eventsproducer.service;

import com.djcodes.spring.kafka.eventsproducer.domain.EmployeeEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
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
    KafkaTemplate<Integer, String> kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    public void sendEventDefault(EmployeeEvent employeeEvent) throws JsonProcessingException {

        Integer key = employeeEvent.getEmployeeEventId();
        String value = objectMapper.writeValueAsString(employeeEvent);

        ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.sendDefault(key, value);
        //Async Behaviour
        listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
            @Override
            public void onFailure(Throwable throwable) {
                log.warn("Message failed for the key {} and value is {}", key, value);
                log.error("Error sending Message {}", throwable.getMessage());
            }

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                log.info("Message Sent SuccessFully for the key {} and value is {} to partition {}", key, value,
                    result.getRecordMetadata().partition());
            }
        });

    }

    public SendResult<Integer, String> sendEventSynchronous(EmployeeEvent employeeEvent)
        throws JsonProcessingException {

        Integer key = employeeEvent.getEmployeeEventId();
        String value = objectMapper.writeValueAsString(employeeEvent);
        SendResult<Integer, String> sendResult = null;
        try {
            sendResult = kafkaTemplate.sendDefault(key, value).get(); // or .get(1, TimeUnit.SECONDS)
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error sending Message {}", e.getMessage());
        }
        return sendResult;

    }

    public void sendEvent(EmployeeEvent employeeEvent) throws JsonProcessingException {

        Integer key = employeeEvent.getEmployeeEventId();
        String value = objectMapper.writeValueAsString(employeeEvent);

        //ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.send(,key, value);

        List<Header> recordHeaders = List.of(new RecordHeader("my-header", "my-value".getBytes()));
        ProducerRecord<Integer, String> producerRecord = new ProducerRecord<>("employee-events", null, key, value,
            recordHeaders);

        ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.send(producerRecord);
        //Async Behaviour
        listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
            @Override
            public void onFailure(Throwable throwable) {
                log.warn("Message failed for the key {} and value is {}", key, value);
                log.error("Error sending Message {}", throwable.getMessage());
            }

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                log.info("Message Sent SuccessFully for the key {} and value is {} to partition {}", key, value,
                    result.getRecordMetadata().partition());
            }
        });

    }

}
