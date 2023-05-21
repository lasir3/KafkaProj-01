package com.example.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javafaker.Faker;

import java.util.HashMap;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;


public class PizzaProducer {
    public static final Logger logger = LoggerFactory.getLogger(PizzaProducer.class.getName());

    public static void sendPizzaMessage(KafkaProducer<String, String> kafkaProducer,
                                        String topicName,
                                        int iterCount, /* loop count */
                                        int interIntervalMillis, /* Interval By Single Message */
                                        int intervalMillis, /* Interval By Multi Messages*/
                                        int intervalCount, /* Interval By Count */
                                        boolean sync) {

        PizzaMessage pizzaMessage = new PizzaMessage();
        int iterSeq = 0;
        long seed = 2022;
        Random random = new Random(seed);
        Faker faker = Faker.instance(random);

        while( iterSeq++ != iterCount ) {
            HashMap<String, String> messageMap = pizzaMessage.produce_msg(faker, random, iterSeq);
            ProducerRecord<String, String> producerRecord =
                    new ProducerRecord<String,String>(topicName, messageMap.get("key"), messageMap.get("value"));

            sendMessage(kafkaProducer, producerRecord, messageMap, sync);
            
            if((intervalCount > 0) && (iterSeq % intervalCount == 0)) {
                try {
                    logger.info("##### IntervalCount : " + intervalCount + 
                                " intervalMillis:" + intervalMillis + " #########");
                    Thread.sleep(intervalMillis);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage());
                }
            }

            if(interIntervalMillis > 0) {
                try {
                    logger.info("interIntervalMillis:" + interIntervalMillis);
                    Thread.sleep(interIntervalMillis);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage());
                } 
            }
        }
    }

    public static void sendMessage (KafkaProducer<String, String> kafkaProducer,
                                    ProducerRecord<String, String> producerRecord,
                                    HashMap<String, String> messageMap, boolean sync) {
        if(!sync) {
            kafkaProducer.send(producerRecord, (metadata, exception) -> {
                if(exception == null) {
                    logger.info("async message:" + messageMap.get("key") + " partitions:" + metadata.partition()
                            + " offset:" + metadata.offset() + "\n");
                } else {
                    logger.error("exception error from brocker " + exception.getMessage());
                }
            });
        } else {
            try {
                // Get Metadata if Sync
                RecordMetadata metadata = kafkaProducer.send(producerRecord).get();
                logger.info("sync message:" + messageMap.get("key") + " partitions:" + metadata.partition()
                        + " offset:" + metadata.offset() + "\n");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        // KafkaProducer configuration setting
        String topicName = "pizza-topic";
        Properties props = new Properties();

        // bootstrap.servers Method 1 - direct statement
        // props.setProperty("bootstrap.servers", "192.168.0.119:9092");

        // bootstrap.servers Method 2 - Using ProducerConfig Class
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.0.73:9092");
        // key.serializer.class, value,serializer.class
        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        // props.setProperty(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, "50000");
        // props.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "6");
        props.setProperty(ProducerConfig.ACKS_CONFIG, "0");
        props.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");

        // Acks Property
        // props.setProperty(ProducerConfig.ACKS_CONFIG, "0");

        // batch Setting
        // props.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, "32000");
        // props.setProperty(ProducerConfig.LINGER_MS_CONFIG, "20");

        // KafkaProducer Object Creation
        KafkaProducer<String, String> kafkaProducer = new KafkaProducer<String, String>(props);

        sendPizzaMessage(kafkaProducer, topicName,
                -1, 1000, 0, 0, true);

        kafkaProducer.close();
    }
}
