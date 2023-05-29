package com.practice.kafka.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class FileProducer {
    public static final Logger logger = LoggerFactory.getLogger(FileProducer.class.getName());
    public static void main(String[] args) {
        // KafkaProducer configuration setting
        String topicName = "file-topic";
        Properties props = new Properties();
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.0.73:9092");
        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // KafkaProducer Object Creation
        KafkaProducer<String, String> kafkaProducer = new KafkaProducer<String, String>(props);
        // Producer 파일의 절대경로 지정
        String filePath = "E:\\Kafka-Project\\KafkaProj-01\\parctice\\src\\main\\resources\\pizza_sample.txt";

        // KafkaPrducer 객체생성 -> ProducerRecords 생성 -> send() 비동기 방식 전송
        sendFileMessages(kafkaProducer, topicName, filePath);


        kafkaProducer.close();
    }

    private static void sendFileMessages(KafkaProducer<String, String> kafkaProducer, String topicName, String filePath) {
        String line = "";
        final String delimiter = ",";

        try {
            FileReader fileReader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while( (line = bufferedReader.readLine()) != null) {
                String [] tokens = line.split(delimiter);
                String key = tokens[0];
                StringBuffer value = new StringBuffer();

                for(int i=1; i<tokens.length; i++) {
                    if( i != (tokens.length-1)) {
                        value.append(tokens[i] + ",");
                    } else {
                        value.append(tokens[i]);
                    }
                }
                
                sendFileMessages(kafkaProducer, topicName, key, value.toString());
                
            }
        } catch (IOException e) {
            logger.info(e.getMessage());
        }
    }

    private static void sendFileMessages(KafkaProducer<String, String> kafkaProducer, String topicName, String key, String value) {
        // ProducerRecord Object Creation
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topicName, key, value);
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
