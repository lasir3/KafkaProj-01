package com.seminar.kafka.producer;

import com.seminar.kafka.event.EventHandler;
import com.seminar.kafka.event.FileEventHandler;
import com.seminar.kafka.event.FileEventSource;
import com.seminar.kafka.model.OrderModel;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;

public class OrderSerdeProducer {
    public static final Logger logger = LoggerFactory.getLogger(OrderSerdeProducer.class.getName());

    public static void main(String[] args) {
        // 토픽명 설정
        String topicName = "topic-to-goldi";
        // Kafka Property 설정
        Properties props = new Properties();
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.0.73:9092, 192.168.0.73:9093");
        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, OrderSerializer.class.getName());

        // custom partition으로 P001에 해당하는 레코드만 Partition1에 저장.
        props.setProperty("custom.specialKey", "P001");
        // props.setProperty("partitioner.class", "CustomPartitioner"); or
        props.setProperty(ProducerConfig.PARTITIONER_CLASS_CONFIG, "com.seminar.kafka.producer.CustomPartitioner");

        // KafkaProducer 객체 생성
        // Key : String, Value : OrderModel 객체
        KafkaProducer<String, OrderModel> kafkaProducer = new KafkaProducer<String, OrderModel>(props);
        // Async 방식으로 동작하도록 설정
        boolean sync = false;

        // Producer 파일의 절대경로 지정
        File file = new File("E:\\Kafka-Project\\KafkaProj-01\\seminar\\src\\main\\resources\\pizza_append.txt");
        EventHandler eventHandler = new FileEventHandler(kafkaProducer, topicName, sync);
        FileEventSource fileEventSource = new FileEventSource(1000, file, eventHandler);

        // 파일의 변화를 감지하는 Event Thread 생성
        Thread fileEventSourceThread = new Thread(fileEventSource);
        fileEventSourceThread.start();
        
        try {
            //
            fileEventSourceThread.join();
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        } finally {
            kafkaProducer.close();
        }

    }
}
