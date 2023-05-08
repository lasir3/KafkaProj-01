package com.example.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;


public class ProducerASyncCustomCB {
    public static final Logger logger = LoggerFactory.getLogger(ProducerASyncCustomCB.class.getName());
    public static void main(String[] args) {
        // KafkaProducer configuration setting
        // Todo : sending (null, "Hello world!")
        String topicName = "multipart-topic";
        Properties props = new Properties();

        // bootstrap.servers Method 1 - direct statement
        // props.setProperty("bootstrap.servers", "192.168.0.119:9092");

        // bootstrap.servers Method 2 - Using ProducerConfig Class
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.0.73:9092");
        // key.serializer.class, value,serializer.class
        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // KafkaProducer Object Creation
        KafkaProducer<Integer, String> kafkaProducer = new KafkaProducer<Integer, String>(props);

        for (int seq = 0; seq < 20; seq++) {
        
            // ProducerRecord Object Creation     
            ProducerRecord<Integer, String> producerRecord = new ProducerRecord<>(topicName, seq, "Hello world! " + seq);

            // Use CustomCallback Interface
            CustomCallback callback = new CustomCallback(seq);
            //logger.info("seq:" + seq);
            // KafkaProducer Message Send with Callback
            kafkaProducer.send(producerRecord, callback);
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        kafkaProducer.close();
    }
}
