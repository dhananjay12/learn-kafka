package com.djcodes.spring.kafka.eventsproducer.it;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.djcodes.spring.kafka.eventsproducer.domain.Employee;
import com.djcodes.spring.kafka.eventsproducer.domain.EmployeeEvent;
import com.djcodes.spring.kafka.eventsproducer.domain.EmployeeEventType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = {"employee-events"}, partitions = 3)
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}"})
public class EmployeeControllerIT {

    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    ObjectMapper objectMapper;

    private Consumer<Integer, String> consumer;

    @BeforeEach
    public void intialize() {
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.consumerProps("group1", "true", embeddedKafkaBroker));
        consumer = new DefaultKafkaConsumerFactory<>(configs, new IntegerDeserializer(), new StringDeserializer()).createConsumer();
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
    }

    @AfterEach
    public void teadDown() {
        consumer.close();
    }

    @Test
    @Timeout(5)
    public void createEmployeeTest() {
        //Arrange
        Employee employeeRequest = Employee.builder().name("John").title("Backend Developer")
            .email("john@mycompany.com").build();

        //Act
        ResponseEntity<Employee> response = testRestTemplate
            .postForEntity("/employee", employeeRequest, Employee.class);

        //Assert
        //Testing API call
        assertEquals(201, response.getStatusCodeValue());

        //Testing whether the record actually gets into Kafka Topic by using a Consumer.
        ConsumerRecord<Integer, String> consumerRecord = KafkaTestUtils.getSingleRecord(consumer, "employee-events");
        EmployeeEvent employeeEvent = null;
        try {
            employeeEvent = objectMapper.readValue(consumerRecord.value(), EmployeeEvent.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        assertEquals(EmployeeEventType.NEW, employeeEvent.getEmployeeEventType());
        assertEquals("John", employeeEvent.getEmployee().getName());


    }

}
