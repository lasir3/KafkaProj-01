package com.seminar.kafka.event;

import com.seminar.kafka.model.OrderModel;

public class MessageEvent {

    public String key;
    public OrderModel value;
    public MessageEvent(String key, OrderModel value) {
        this.key = key;
        this.value = value;
    }

}
