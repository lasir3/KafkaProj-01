package com.seminar.kafka.self;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;


public class SimpleProducerSyncWithAcks {
    public static final Logger logger = LoggerFactory.getLogger(SimpleProducerSyncWithAcks.class.getName());
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
        //props.setProperty(ProducerConfig.ACKS_CONFIG, "0");
        // KafkaProducer Object Creation
        KafkaProducer<String, String> kafkaProducer = new KafkaProducer<String, String>(props);

        // ProducerRecord Object Creation
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topicName, "Hello world!");
        long beforeTime = System.currentTimeMillis(); //코드 실행 전에 시간 받아오기
        // KafkaProducer Message Send
        int i = 0;
        while (i++ < 5000) {
            try {
                kafkaProducer.send(producerRecord);
                // logger.info("\n ###### record metadata received ##### \n" +
                //         "partitions:" + recordMetadata.partition() + "\n" +
                //         "offset:" + recordMetadata.offset() + "\n" +
                //         "timestamp:" + recordMetadata.timestamp() + "\n" +
                //         "topic:" + recordMetadata.topic());
                System.out.println(i);

           // } catch (ExecutionException e) {
           //     throw new RuntimeException(e);
           // } catch (InterruptedException e) {
           //     throw new RuntimeException(e);
            } finally {
                //kafkaProducer.close();
            }
        }
        System.out.println("While 종료");
        long afterTime = System.currentTimeMillis(); // 코드 실행 후에 시간 받아오기
        long secDiffTime = (afterTime - beforeTime)/1000; //두 시간에 차 계산
        System.out.println("시간차이(m) : "+secDiffTime);
    }
}

