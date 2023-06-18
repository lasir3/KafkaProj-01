package com.seminar.kafka.producer;

import com.seminar.kafka.model.OrderModel;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class OrderSerdeProducer {
    public static final Logger logger = LoggerFactory.getLogger(OrderSerdeProducer.class.getName());
    public static void main(String[] args) {
        // KafkaProducer configuration setting
        String topicName = "order-serde-topic";
        Properties props = new Properties();
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.0.73:9092");
        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, OrderSerializer.class.getName());

        // KafkaProducer Object Creation
        KafkaProducer<String, OrderModel> kafkaProducer = new KafkaProducer<String, OrderModel>(props);
        // Producer 파일의 절대경로 지정
        String filePath = "E:\\Kafka-Project\\KafkaProj-01\\practice\\src\\main\\resources\\pizza_append.txt";

        // KafkaPrducer 객체생성 -> ProducerRecords 생성 -> send() 비동기 방식 전송
        sendFileMessages(kafkaProducer, topicName, filePath);


        kafkaProducer.close();
    }

    private static void sendFileMessages(KafkaProducer<String, OrderModel> kafkaProducer, String topicName, String filePath) {
        String line = "";
        final String delimiter = ",";

        try {
            FileReader fileReader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            while( (line = bufferedReader.readLine()) != null) {
                String [] tokens = line.split(delimiter);
                String key = tokens[0];
                OrderModel orderModel = new OrderModel(tokens[1], tokens[2], tokens[3], tokens[4],
                        tokens[5], tokens[6], LocalDateTime.parse(tokens[7].trim(), formatter));
                sendFileMessages(kafkaProducer, topicName, key, orderModel);
                
            }
        } catch (IOException e) {
            logger.info(e.getMessage());
        }
    }

    private static void sendFileMessages(KafkaProducer<String, OrderModel> kafkaProducer, String topicName, String key, OrderModel value) {
        // ProducerRecord Object Creation
        ProducerRecord<String, OrderModel> producerRecord = new ProducerRecord<>(topicName, key, value);
        logger.info("key:{}, value:{}", key, value);
        // KafkaProducer Message Send with Callback
        kafkaProducer.send(producerRecord, (metadata, exception)-> {
            if(exception == null) {
                logger.info("\n ###### record metadata received ##### \n" +
                        "partitions:" + metadata.partition() + "\n" +
                        "offset:" + metadata.offset() + "\n" +
                        "timestamp:" + metadata.timestamp() + "\n" +
                        "topic:" + metadata.topic());
            } else {
                logger.error("exception error from brocker " + exception.getMessage());
            }
        });
    }
}
