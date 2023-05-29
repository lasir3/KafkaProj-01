package com.example.kafka;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ConsumerPartitionAssignSeek {
    public static final Logger logger = LoggerFactory.getLogger(ConsumerPartitionAssignSeek.class.getName());

    public static void main(String[] args) {

        String topicName = "pizza-topic";

        Properties props = new Properties();
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.0.73:9092");
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        // props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "group_01");
        // props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "group_pizza_assign_seek");
        // props.setProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000");
        props.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        // poll Timeout property
        props.setProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "60000");

        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<String, String>(props);
        TopicPartition topicPartition = new TopicPartition(topicName, 0);
        // kafkaConsumer.subscribe(List.of(topicName));
        kafkaConsumer.assign(List.of(topicPartition));
        kafkaConsumer.seek(topicPartition, 10L);

        // main thread
        Thread mainThread = Thread.currentThread();

        // When Main() is closed, call Wakeup() Exception
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                logger.info("main program starts to exit by calling wakeup");
                kafkaConsumer.wakeup();

                try {
                    // wait until main() is closed.
                    mainThread.join();
                } catch(InterruptedException e) { e.printStackTrace();}
            }
        });

        // kafkaConsumer.close();
        // pollAutoCommit(kafkaConsumer);
        // pollCommitSync(kafkaConsumer);
        // pollCommitAsync(kafkaConsumer);
        poolNoCommit(kafkaConsumer);
    }

    private static void poolNoCommit(KafkaConsumer<String, String> kafkaConsumer) {
        int loopCnt = 0;

        try {
            while (true) {
                ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.ofMillis(1000));
                logger.info(" ######## loopCnt: {} consumerRecords count:{}", loopCnt++, consumerRecords.count());
                for (ConsumerRecord record : consumerRecords) {
                    logger.info("record key:{}, partition:{}, record offset:{}, record value:{}",
                            record.key(), record.partition(), record.offset(), record.value());
                }
            }
        }catch(WakeupException e) {
            logger.error("wakeup exception has been called");
        } catch(Exception e) {
            logger.error(e.getMessage());
        }finally {
            logger.info("finally consumer is closing");
            kafkaConsumer.close();
        }
    }

    private static void pollCommitAsync(KafkaConsumer<String, String> kafkaConsumer) {
        int loopCnt = 0;

        try {
            while (true) {
                ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.ofMillis(1000));
                logger.info(" ######## loopCnt: {} consumerRecords count:{}", loopCnt++, consumerRecords.count());
                for (ConsumerRecord record : consumerRecords) {
                    logger.info("record key:{}, partition:{}, record offset:{}, record value:{}",
                            record.key(), record.partition(), record.offset(), record.value());
                }
                kafkaConsumer.commitAsync(new OffsetCommitCallback() {
                    @Override
                    public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception exception) {
                        // exception이 발생할 경우 Callback 발생.
                        if(exception != null) {
                            logger.error("offsets {} is not completed, error:{}", offsets, exception.getMessage());
                        }
                    }
                });
            }
        }catch(WakeupException e) {
            logger.error("wakeup exception has been called");
        } catch(Exception e) {
            logger.error(e.getMessage());
        }finally {
            logger.error("#### commit sync before closing");
            kafkaConsumer.commitSync();
            logger.info("finally consumer is closing");
            kafkaConsumer.close();
        }
    }

    private static void pollCommitSync(KafkaConsumer<String, String> kafkaConsumer) {
        int loopCnt = 0;

        try {
            while (true) {
                ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.ofMillis(1000));
                logger.info(" ######## loopCnt: {} consumerRecords count:{}", loopCnt++, consumerRecords.count());
                for (ConsumerRecord record : consumerRecords) {
                    logger.info("record key:{}, partition:{}, record offset:{}, record value:{}",
                            record.key(), record.partition(), record.offset(), record.value());
                }
                try {
                    // consumer Record가 존재할 경우 커밋 진행.
                    if(consumerRecords.count() > 0) {
                        kafkaConsumer.commitSync();
                        logger.info("commit sync has been called.");
                    }
                }catch (CommitFailedException e) {
                    logger.error(e.getMessage());
                }
            }
        }catch(WakeupException e) {
            logger.error("wakeup exception has been called");
        } catch(Exception e) {
            logger.error(e.getMessage());
        }finally {
            logger.info("finally consumer is closing");
            kafkaConsumer.close();
        }
    }

    // Make AutoCommit Method
    public static void pollAutoCommit(KafkaConsumer<String, String> kafkaConsumer) {
        int loopCnt = 0;

        try {
            while (true) {
                ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.ofMillis(1000));
                logger.info(" ######## loopCnt: {} consumerRecords count:{}", loopCnt++, consumerRecords.count());
                for (ConsumerRecord record : consumerRecords) {
                    logger.info("record key:{}, partition:{}, record offset:{}, record value:{}",
                            record.key(), record.partition(), record.offset(), record.value());

                }
                try {
                    logger.info("main thread is sleep {} ms during while loop", 10000);
                    Thread.sleep(10000);
                }catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }catch(WakeupException e) {
            logger.error("wakeup exception has been called");
        }finally {
            logger.info("finally consumer is closing");
            kafkaConsumer.close();
        }
    }
}
