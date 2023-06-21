package com.seminar.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.seminar.kafka.model.OrderModel;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class OrderSerdeConsumer<K extends Serializable, V extends Serializable> {
    public static final Logger logger = LoggerFactory.getLogger(OrderSerdeConsumer.class.getName());
    protected KafkaConsumer<K, V> kafkaConsumer;
    protected List<String> topics;

    private OrderDBHandler orderDBHandler;
    public OrderSerdeConsumer(Properties consumerProps, List<String> topics,
                            OrderDBHandler orderDBHandler) {
        this.kafkaConsumer = new KafkaConsumer<K, V>(consumerProps);
        this.topics = topics;
        this.orderDBHandler = orderDBHandler;
    }
    public void initConsumer() {
        this.kafkaConsumer.subscribe(this.topics);
        shutdownHookToRuntime(this.kafkaConsumer);
    }

    private void shutdownHookToRuntime(KafkaConsumer<K, V> kafkaConsumer) {
        //main thread
        Thread mainThread = Thread.currentThread();

        //main thread 종료시 별도의 thread로 KafkaConsumer wakeup()메소드를 호출하게 함.
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                logger.info(" main program starts to exit by calling wakeup");
                kafkaConsumer.wakeup();
                try {
                    mainThread.join();
                } catch(InterruptedException e) { e.printStackTrace();}
            }
        });

    }
    // 미사용
    // private void processRecord(ConsumerRecord<K, V> record) throws Exception {
        // OrderDTO orderDTO = makeOrderDTO(record);
        // orderDBHandler.insertOrder(orderDTO);
    // }

    private OrderDTO makeOrderDTO(ConsumerRecord<K,V> record) throws Exception {
        String messageValue = (String)record.value();
        logger.info("###### messageValue:" + messageValue);

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode rootNode = objectMapper.readValue(messageValue, ObjectNode.class);

        // JSON Array 형식으로 저장된 orderTime을 LocalDateTime 객체로 변환
        int[] orderTimeArray = objectMapper.convertValue(rootNode.get("orderTime"), int[].class);
        LocalDateTime orderTime;
        if (orderTimeArray.length == 6) {
            orderTime = LocalDateTime.of(orderTimeArray[0], orderTimeArray[1], orderTimeArray[2], orderTimeArray[3], orderTimeArray[4], orderTimeArray[5]);
        } else if (orderTimeArray.length == 5) {
            orderTime = LocalDateTime.of(orderTimeArray[0], orderTimeArray[1], orderTimeArray[2], orderTimeArray[3], orderTimeArray[4], 0);
        } else {
            throw new Exception("Invalid orderTime array length");
        }

        OrderDTO orderDTO = new OrderDTO(
                rootNode.get("orderId").asText(),
                rootNode.get("shopId").asText(),
                rootNode.get("menuName").asText(),
                rootNode.get("userName").asText(),
                rootNode.get("phoneNumber").asText(),
                rootNode.get("address").asText(),
                orderTime);

        return orderDTO;
    }


    private void processRecords(ConsumerRecords<K, V> records) throws Exception{
        List<OrderDTO> orders = makeOrders(records);
        orderDBHandler.insertOrders(orders);
    }

    private List<OrderDTO> makeOrders(ConsumerRecords<K,V> records) throws Exception {
        List<OrderDTO> orders = new ArrayList<>();
        //records.forEach(record -> orders.add(makeOrderDTO(record)));
        for(ConsumerRecord<K, V> record : records) {
            OrderDTO orderDTO = makeOrderDTO(record);
            orders.add(orderDTO);
        }
        return orders;
    }


    public void pollConsumes(long durationMillis, String commitMode) {
        if (commitMode.equals("sync")) {
            pollCommitSync(durationMillis);
        } else {
            pollCommitAsync(durationMillis);
        }
    }
    private void pollCommitAsync(long durationMillis) {
        try {
            while (true) {
                ConsumerRecords<K, V> consumerRecords = this.kafkaConsumer.poll(Duration.ofMillis(durationMillis));
                logger.info("consumerRecords count:" + consumerRecords.count());
                if(consumerRecords.count() > 0) {
                    try {
                        processRecords(consumerRecords);
                    } catch(Exception e) {
                        logger.error(e.getMessage());
                    }
                }

                //commitAsync의 OffsetCommitCallback을 lambda 형식으로 변경
                this.kafkaConsumer.commitAsync((offsets, exception) -> {
                    if (exception != null) {
                        logger.error("offsets {} is not completed, error:{}", offsets, exception.getMessage());
                    }
                });
            }
        }catch(WakeupException e) {
            logger.error("wakeup exception has been called");
        }catch(Exception e) {
            logger.error(e.getMessage());
        }finally {
            logger.info("##### commit sync before closing");
            kafkaConsumer.commitSync();
            logger.info("finally consumer is closing");
            close();
        }
    }

    protected void pollCommitSync(long durationMillis) {
        try {
            while (true) {
                ConsumerRecords<K, V> consumerRecords = this.kafkaConsumer.poll(Duration.ofMillis(durationMillis));
                processRecords(consumerRecords);
                try {
                    if (consumerRecords.count() > 0) {
                        this.kafkaConsumer.commitSync();
                        logger.info("commit sync has been called");
                    }
                } catch (CommitFailedException e) {
                    logger.error(e.getMessage());
                }
            }
        }catch(WakeupException e) {
            logger.error("wakeup exception has been called");
        }catch(Exception e) {
            logger.error(e.getMessage());
        }finally {
            logger.info("##### commit sync before closing");
            kafkaConsumer.commitSync();
            logger.info("finally consumer is closing");
            close();
        }
    }
    protected void close() {
        this.kafkaConsumer.close();
        this.orderDBHandler.close();
    }

    public static void main(String[] args) {
        String topicName = "topic-to-goldi";

        Properties props = new Properties();
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.0.73:9092, 192.168.0.73:9093");
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "goldi-01");
        props.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        String user = "sys";
        String password = "gliese";
        OrderDBHandler orderDBHandler = new OrderDBHandler(user, password);

        OrderSerdeConsumer<String, OrderModel> fileToDBConsumer = new
                OrderSerdeConsumer<String, OrderModel>(props, List.of(topicName), orderDBHandler);
        fileToDBConsumer.initConsumer();
        String commitMode = "async";

        fileToDBConsumer.pollConsumes(1000, commitMode);

    }

}