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

public class FileAppendProducer {
    public static final Logger logger = LoggerFactory.getLogger(FileAppendProducer.class.getName());
    public static void main(String[] args) {

        String topicName = "file-topic";
        Properties props = new Properties();
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.0.73:9092");
        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, OrderSerializer.class.getName());

        // KafkaProducer 객체 생성
        KafkaProducer<String, OrderModel> kafkaProducer = new KafkaProducer<String, OrderModel>(props);
        boolean sync = false;

        // Producer 파일의 절대경로 지정
        File file = new File( "E:\\Kafka-Project\\KafkaProj-01\\seminar\\src\\main\\resources\\pizza_append.txt");
        EventHandler eventHandler = new FileEventHandler(kafkaProducer, topicName, sync);
        FileEventSource fileEventSource = new FileEventSource(1000, file, eventHandler);

        // 파일 데이터 사이즈 변화 Monitoring 하는 Thread 생성
        Thread fileEventSourceThread = new Thread(fileEventSource);
        fileEventSourceThread.start();

        try { 
            fileEventSourceThread.join();
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        } finally {
            kafkaProducer.close();
        }

    }
}
