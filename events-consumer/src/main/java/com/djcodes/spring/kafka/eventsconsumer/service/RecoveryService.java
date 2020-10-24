package com.djcodes.spring.kafka.eventsconsumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
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
public class RecoveryService {

    @Autowired
    KafkaTemplate kafkaTemplate;

    public ListenableFuture<SendResult<Integer, String>> sendEventToRecovery(
        ConsumerRecord<Integer, String> consumerRecord, String reason) throws JsonProcessingException {

        Integer key = consumerRecord.key();
        String value = consumerRecord.value();

        //ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.send(,key, value);

        List<Header> recordHeaders = List.of(new RecordHeader("reason", reason.getBytes()));
        ProducerRecord<Integer, String> producerRecord = new ProducerRecord<>("employee-events-recovery", null, key,
            value,
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

        return listenableFuture;

    }

}
