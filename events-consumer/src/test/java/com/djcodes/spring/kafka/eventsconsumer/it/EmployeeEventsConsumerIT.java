package com.djcodes.spring.kafka.eventsconsumer.it;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.djcodes.spring.kafka.eventsconsumer.domain.Employee;
import com.djcodes.spring.kafka.eventsconsumer.domain.EmployeeEvent;
import com.djcodes.spring.kafka.eventsconsumer.domain.EmployeeEventType;
import com.djcodes.spring.kafka.eventsconsumer.listener.EmployeeEventListener;
import com.djcodes.spring.kafka.eventsconsumer.service.AdminService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = {"employee-events"}, partitions = 3)
@TestPropertySource(properties = {"spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}"})
public class EmployeeEventsConsumerIT {

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    KafkaTemplate<Integer, String> kafkaTemplate;

    @Autowired
    KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Autowired
    ObjectMapper objectMapper;

    @SpyBean
    EmployeeEventListener employeeEventListener;

    @SpyBean
    AdminService adminService;

    @BeforeEach
    public void init() {

        for (MessageListenerContainer messageListenerContainer : kafkaListenerEndpointRegistry.getListenerContainers()){
            ContainerTestUtils.waitForAssignment(messageListenerContainer, embeddedKafkaBroker.getPartitionsPerTopic());
        }
    }

    @Test
    public void employeeEventListener() throws JsonProcessingException, ExecutionException, InterruptedException {
        //Arrange
        //Create a random event
        EmployeeEvent employeeEvent = getRandomEvent();
        String json = objectMapper.writeValueAsString(employeeEvent);
        kafkaTemplate.sendDefault(json).get();

        //Act
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await(3, TimeUnit.SECONDS);

        //Assert
        verify(employeeEventListener, times(1)).onMessage(isA(ConsumerRecord.class));
        verify(adminService, times(1)).processEmployeeEvent(isA(ConsumerRecord.class));

    }

    @Test
    public void employeeEventListener_RetryTest_Validation_Exception() throws JsonProcessingException, ExecutionException, InterruptedException {
        //Arrange
        //Create a random event
        EmployeeEvent employeeEvent = getRandomEvent();
        //Purposefully making null
        employeeEvent.setEmployeeEventType(null);
        String json = objectMapper.writeValueAsString(employeeEvent);
        kafkaTemplate.sendDefault(json).get();

        //Act
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await(3, TimeUnit.SECONDS);

        //Assert - No Retry attempt
        verify(employeeEventListener, times(1)).onMessage(isA(ConsumerRecord.class));
        verify(adminService, times(1)).processEmployeeEvent(isA(ConsumerRecord.class));

    }

    @Test
    public void employeeEventListener_RetryTest_RecoverableDataAccessException_Exception() throws JsonProcessingException, ExecutionException, InterruptedException {
        //Arrange
        //Create a random event
        EmployeeEvent employeeEvent = getRandomEvent();
        //Purposefully making null
        employeeEvent.setEmployeeEventId(null);
        String json = objectMapper.writeValueAsString(employeeEvent);
        kafkaTemplate.sendDefault(json).get();

        //Act
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await(3, TimeUnit.SECONDS);

        //Assert - No Retry attempt
        verify(employeeEventListener, times(3)).onMessage(isA(ConsumerRecord.class));
        verify(adminService, times(3)).processEmployeeEvent(isA(ConsumerRecord.class));

    }

    @Test
    @Disabled
    public void employeeEventListener_RetryTest_RecoverableDataAccessException_Exception_and_Recover() throws JsonProcessingException, ExecutionException, InterruptedException {
        //Arrange
        //Create a random event
        EmployeeEvent employeeEvent = getRandomEvent();
        //Purposefully making null
        employeeEvent.setEmployeeEventId(null);
        String json = objectMapper.writeValueAsString(employeeEvent);
        kafkaTemplate.sendDefault(json).get();
        // Making a consumer for recovery topic
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.consumerProps("group1", "true", embeddedKafkaBroker));
        Consumer<Integer, String> consumer; consumer = new DefaultKafkaConsumerFactory<>(configs, new IntegerDeserializer(), new StringDeserializer()).createConsumer();
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);


        //Act
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await(3, TimeUnit.SECONDS);

        //Assert - No Retry attempt
        verify(employeeEventListener, times(3)).onMessage(isA(ConsumerRecord.class));
        verify(adminService, times(3)).processEmployeeEvent(isA(ConsumerRecord.class));

        //Testing whether the record actually gets into Kafka Topic by using a Consumer.
        ConsumerRecord<Integer, String> consumerRecord = KafkaTestUtils.getSingleRecord(consumer, "employee-events-recovery");
        EmployeeEvent recoveryEmployeeEvent = null;
        try {
            recoveryEmployeeEvent = objectMapper.readValue(consumerRecord.value(), EmployeeEvent.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        assertEquals(employeeEvent.getEmployee().getId(), recoveryEmployeeEvent.getEmployee().getId());
    }

    private EmployeeEvent getRandomEvent(){
        Random random  = new Random();
        int id = random.nextInt(20);
        Employee employee = Employee.builder().id(id).email("test@mycompany.com").title("QA").name("Jane").build();
        return EmployeeEvent.builder().employeeEventId(id)
            .employeeEventType(EmployeeEventType.values()[new Random().nextInt(EmployeeEventType.values().length)])
            .employee(employee)
            .build();

    }

}
