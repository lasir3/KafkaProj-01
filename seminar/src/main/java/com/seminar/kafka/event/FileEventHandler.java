package com.seminar.kafka.event;

import com.seminar.kafka.model.OrderModel;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;


public class FileEventHandler implements EventHandler{
    public static final Logger logger = LoggerFactory.getLogger(FileEventHandler.class.getName());
    private final KafkaProducer<String, OrderModel> kafkaProducer;
    private final String topicName;
    private final boolean sync;
    public FileEventHandler(KafkaProducer<String, OrderModel> kafkaProducer, String topicName, boolean sync) {
        this.kafkaProducer = kafkaProducer;
        this.topicName = topicName;
        this.sync = sync;
    }

    @Override
    public void onMessage(MessageEvent messageEvent) throws InterruptedException, ExecutionException {
        ProducerRecord<String, OrderModel> producerRecord = new ProducerRecord<>(this.topicName, messageEvent.key, messageEvent.value);

        // Sync가 True 일때
        if(this.sync) {
            RecordMetadata recordMetadata = this.kafkaProducer.send(producerRecord).get();
            logger.info("\n ###### record metadata received ##### \n" +
                    "partitions:" + recordMetadata.partition() + "\n" +
                    "offset:" + recordMetadata.offset() + "\n" +
                    "timestamp:" + recordMetadata.timestamp() + "\n" +
                    "topic:" + recordMetadata.topic());
        }
        // Async 일때
        else {
            this.kafkaProducer.send(producerRecord, (metadata, exception)-> {
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

//    public static void main(String[] args) throws Exception {
//        String topicName = "file-topic";
//
//        Properties props = new Properties();
//        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.0.73:9092");
//        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
//        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
//
//        KafkaProducer<String, OrderModel> kafkaProducer = new KafkaProducer<>(props);
//        boolean sync = true;
//
//        //FileEventHandler fileEventHandler = new FileEventHandler(kafkaProducer, topicName, sync);
//        MessageEvent messageEvent = new MessageEvent("key00001", null);
//        fileEventHandler.onMessage(messageEvent);
//    }
}
