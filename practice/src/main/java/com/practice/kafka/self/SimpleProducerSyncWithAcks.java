package com.example.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ExecutionException;


public class SimpleProducerSync {
    public static final Logger logger = LoggerFactory.getLogger(SimpleProducerSync.class.getName());
    public static void main(String[] args) {
        // KafkaProducer configuration setting
        // Todo : sending (null, "Hello world!")
        String topicName = "simple-topic";
        Properties props = new Properties();

        // bootstrap.servers Method 1 - direct statement
        // props.setProperty("bootstrap.servers", "192.168.0.119:9092");

        // bootstrap.servers Method 2 - Using ProducerConfig Class
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.0.73:9092");
        // key.serializer.class, value,serializer.class
        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // KafkaProducer Object Creation
        KafkaProducer<String, String> kafkaProducer = new KafkaProducer<String, String>(props);

        // ProducerRecord Object Creation
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topicName, "Hello world!");

        // KafkaProducer Message Send
        try {
            RecordMetadata recordMetadata = kafkaProducer.send(producerRecord).get();
            logger.info("\n ###### record metadata received ##### \n" +
                "partitions:" + recordMetadata.partition() + "\n" +
                "offset:" + recordMetadata.offset() + "\n" +
                "timestamp:" + recordMetadata.timestamp() + "\n" +
                "topic:" + recordMetadata.topic());
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            kafkaProducer.close();
        }
    }
}
