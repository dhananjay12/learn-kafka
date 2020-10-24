package com.djcodes.spring.kafka.eventsconsumer.config;

import com.djcodes.spring.kafka.eventsconsumer.service.RecoveryService;
import java.util.HashMap;
import java.util.Map;
import javax.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
@EnableKafka
@Slf4j
public class KafkaConfig {

    @Autowired
    KafkaProperties kafkaProperties;

    @Autowired
    RecoveryService recoveryService;

    @Bean
    ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
        ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
        ConsumerFactory<Object, Object> kafkaConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory();
        configurer.configure(factory, kafkaConsumerFactory);
        factory.setErrorHandler((exception, data) -> {
            log.error("Exception in consumer {} with data {}", exception.getMessage(), data);
        });
        factory.setRetryTemplate(retryTemplate());
        factory.setRecoveryCallback(retryContext -> {
            if(retryContext.getLastThrowable().getCause() instanceof RecoverableDataAccessException){
                log.error("Recoverable");
                ConsumerRecord<Integer, String> consumerRecord = (ConsumerRecord<Integer, String>) retryContext.getAttribute("record");
                recoveryService.sendEventToRecovery(consumerRecord, retryContext.getLastThrowable().getMessage());
            }else{
                log.error("Not Recoverable");
                throw new RuntimeException(retryContext.getLastThrowable().getMessage());
            }
            return null;
        });
        return factory;
    }

    private RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(1000l);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

        //SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        //retryPolicy.setMaxAttempts(3);

        Map<Class<? extends Throwable>, Boolean> exceptionsMap = new HashMap<>();
        exceptionsMap.put(ValidationException.class, false); // We dont want to retry
        exceptionsMap.put(RecoverableDataAccessException.class, true); // We want to retry
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(3,exceptionsMap,true);
        retryTemplate.setRetryPolicy(retryPolicy);
        return retryTemplate;
    }


/*    @Bean
    ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
        ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
        ConsumerFactory<Object, Object> kafkaConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory();
        configurer.configure(factory,kafkaConsumerFactory);
        factory.getContainerProperties().setAckMode(AckMode.MANUAL); //Manual
        //factory.setConcurrency(3); //Concurrency if nor on cloud
        return factory;
    }*/


}
