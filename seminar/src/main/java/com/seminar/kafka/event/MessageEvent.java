package com.seminar.kafka.event;

import com.seminar.kafka.model.OrderModel;
import org.apache.kafka.clients.producer.KafkaProducer;

public class MessageEvent {

    public String key;
    public OrderModel value;
    public MessageEvent(String key, OrderModel value) {
        this.key = key;
        this.value = value;
    }

}
