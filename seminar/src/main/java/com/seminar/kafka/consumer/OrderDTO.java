package com.seminar.kafka.consumer;

import java.time.LocalDateTime;

public class OrderDTO {
    //P001,ord5000, P001, Cheese Pizza, Erick Koelpin, (235) 592-3785 x9190, 6373 Gulgowski Path, 2023-05-31 17:46:07
    public String orderId;
    public String shopId;
    public String menuName;
    public String userName;
    public String phoneNumber;
    public String address;
    public LocalDateTime orderTime;

    public OrderDTO(String orderId, String shopId, String menuName, String userName,
                    String phoneNumber, String address, LocalDateTime orderTime) {
        this.orderId = orderId;
        this.shopId = shopId;
        this.menuName = menuName;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.orderTime = orderTime;
    }

        public String toString() {
        return "OrderDTO{" +
                "orderId='" + orderId + '\'' +
                ", shopId='" + shopId + '\'' +
                ", menuName='" + menuName + '\'' +
                ", userName='" + userName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", address='" + address + '\'' +
                ", orderTime=" + orderTime +
                '}';
    }
}
