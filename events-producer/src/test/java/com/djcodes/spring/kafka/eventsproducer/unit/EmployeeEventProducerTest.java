package com.djcodes.spring.kafka.eventsproducer.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

import com.djcodes.spring.kafka.eventsproducer.domain.Employee;
import com.djcodes.spring.kafka.eventsproducer.domain.EmployeeEvent;
import com.djcodes.spring.kafka.eventsproducer.domain.EmployeeEventType;
import com.djcodes.spring.kafka.eventsproducer.service.EmployeeEventProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;

@ExtendWith(MockitoExtension.class)
public class EmployeeEventProducerTest {

    @Mock
    KafkaTemplate<Integer, String> kafkaTemplate;

    @InjectMocks
    EmployeeEventProducer employeeEventProducer;

    @Test
    public void sendEventDefault_failure() {
        //Arrange
        Employee employeeRequest = Employee.builder().name("John").title("Backend Developer")
            .email("john@mycompany.com").build();
        EmployeeEvent employeeEventInput = EmployeeEvent.builder().employeeEventId(employeeRequest.getId())
            .employeeEventType(
                EmployeeEventType.NEW).employee(employeeRequest).build();

        SettableListenableFuture future = new SettableListenableFuture();
        future.setException(new RuntimeException("Connection error"));
        when(kafkaTemplate.sendDefault(any())).thenReturn(future);

        //Act and Assert
        assertThrows(Exception.class, () -> employeeEventProducer.sendEventDefault(employeeEventInput).get());
    }


    @Test
    public void sendEvent_success() throws JsonProcessingException, ExecutionException, InterruptedException {
        //Arrange
        Employee employeeRequest = Employee.builder().name("John").title("Backend Developer")
            .email("john@mycompany.com").build();
        EmployeeEvent employeeEventInput = EmployeeEvent.builder().employeeEventId(employeeRequest.getId())
            .employeeEventType(
                EmployeeEventType.NEW).employee(employeeRequest).build();

        SettableListenableFuture future = new SettableListenableFuture();
        ProducerRecord<Integer, String> producerRecord = new ProducerRecord("employee-events",
            employeeEventInput.getEmployeeEventId(), employeeEventInput);
        RecordMetadata recordMetadata = new RecordMetadata(new TopicPartition("employee-events", 0),
            1, 1, 123123, System.currentTimeMillis(), 1, 1);
        SendResult<Integer, String> sendResult = new SendResult<Integer, String>(producerRecord, recordMetadata);
        future.set(sendResult);

        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);

        //Act
        ListenableFuture<SendResult<Integer, String>> listenableFuture = employeeEventProducer
            .sendEvent(employeeEventInput);

        //Assert
        SendResult<Integer, String> result = listenableFuture.get();
        assertEquals(0, result.getRecordMetadata().partition());

    }

}
