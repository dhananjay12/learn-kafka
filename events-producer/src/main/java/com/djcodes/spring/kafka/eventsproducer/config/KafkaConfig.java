package com.djcodes.spring.kafka.eventsproducer.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic events() {
        return TopicBuilder.name("employee-events")
            .partitions(3)
            .replicas(3)
            .build();
    }
}