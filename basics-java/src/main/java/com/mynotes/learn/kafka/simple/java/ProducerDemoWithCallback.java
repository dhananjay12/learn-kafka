package com.mynotes.learn.kafka.simple.java;

import java.util.Properties;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProducerDemoWithCallback {

	public static void main(String[] args) {

		final Logger logger = LoggerFactory.getLogger(ProducerDemoWithCallback.class);

		// create Producer properties
		Properties properties = new Properties();
		properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
		properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

		// create the producer
		KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);

		for (int i = 0; i < 10; i++) {

			// create a producer record
			ProducerRecord<String, String> record = new ProducerRecord<String, String>("first_topic",
					"hello world" + Integer.toString(i));

			// send data - asynchronous
			producer.send(record, new Callback() {

				public void onCompletion(RecordMetadata metadata, Exception exception) {
					if (exception == null) {
						// the record was successfully sent
						logger.info("Received new metadata. \n" + "Topic:" + metadata.topic() + "\n" + "Partition: "
								+ metadata.partition() + "\n" + "Offset: " + metadata.offset() + "\n" + "Timestamp: "
								+ metadata.timestamp());
					} else {
						logger.error("Error while producing", exception);
					}

				}
			});

		}

		// flush data
		producer.flush();
		// flush and close producer
		producer.close();

	}
}
