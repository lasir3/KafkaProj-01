package com.seminar.kafka.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

import sunje.goldilocks.jdbc.GoldilocksDriver;
import sunje.goldilocks.jdbc.GoldilocksDataSource;

public class OrderDBHandler {
    public static final Logger logger = LoggerFactory.getLogger(OrderDBHandler.class.getName());
    private Connection connection = null;
    private PreparedStatement insertPrepared = null;
    private static final String INSERT_ORDER_SQL = "INSERT INTO PUBLIC.ORDERS " +
            "(ord_id, shop_id, menu_name, user_name, phone_number, address, order_time) "+ /**/
            "values (?, ?, ?, ?, ?, ?, ?)";

    public OrderDBHandler(String user, String password) {
        try {
            Class.forName(GoldilocksDriver.class.getName());

            GoldilocksDataSource dataSource = new GoldilocksDataSource();
            dataSource.setDatabaseName("test");
            dataSource.setServerName("192.168.0.120");
            dataSource.setPortNumber(42613);
            dataSource.setUser(user);
            dataSource.setPassword(password);

            this.connection = dataSource.getConnection();
            this.insertPrepared = this.connection.prepareStatement(INSERT_ORDER_SQL);
        } catch (SQLException | ClassNotFoundException e) {
            logger.error(e.getMessage());
        }
    }

    public void insertOrder(OrderDTO orderDTO)  {
        try {
            //logger.info("orderId:{},  shopId:{}, menuName:{} userName:{} phoneNumber:{} address:{}",
            //        orderDTO.orderId, orderDTO.shopId, orderDTO.menuName, orderDTO.userName, orderDTO.phoneNumber, orderDTO.address);
            PreparedStatement pstmt = this.connection.prepareStatement(INSERT_ORDER_SQL);
            pstmt.setString(1, orderDTO.orderId);
            pstmt.setString(2, orderDTO.shopId);
            pstmt.setString(3, orderDTO.menuName);
            pstmt.setString(4, orderDTO.userName);
            pstmt.setString(5, orderDTO.phoneNumber);
            pstmt.setString(6, orderDTO.address);
            pstmt.setTimestamp(7, Timestamp.valueOf(orderDTO.orderTime));

            pstmt.executeUpdate();
//            this.connection.commit();
//            pstmt.close();
        } catch(SQLException e) {
            logger.error(e.getMessage());
        }
    }

    public void insertOrders(List<OrderDTO> orders) {
        try {

            PreparedStatement pstmt = this.connection.prepareStatement(INSERT_ORDER_SQL);
            for(OrderDTO orderDTO : orders) {
                logger.info("orderId:{},  shopId:{}, menuName:{} userName:{} phoneNumber:{} address:{}",
                        orderDTO.orderId, orderDTO.shopId, orderDTO.menuName, orderDTO.userName, orderDTO.phoneNumber, orderDTO.address);
                pstmt.setString(1, orderDTO.orderId);
                pstmt.setString(2, orderDTO.shopId);
                pstmt.setString(3, orderDTO.menuName);
                pstmt.setString(4, orderDTO.userName);
                pstmt.setString(5, orderDTO.phoneNumber);
                pstmt.setString(6, orderDTO.address);
                pstmt.setTimestamp(7, Timestamp.valueOf(orderDTO.orderTime));
                // DTO 객체의 내용을 Batch
                pstmt.addBatch();
            }
            pstmt.executeBatch();
//            this.connection.commit();
//            pstmt.close();
        } catch(SQLException e) {
            logger.info(e.getMessage());
        }
    }

    public void close()
    {
        try {
            logger.info("###### OrderDBHandler is closing");
            this.insertPrepared.close();
            this.connection.close();
        }catch(SQLException e) {
            logger.error(e.getMessage());
        }
    }

    // DB Insert Test
//    public static void main(String[] args) {
//        String url = "jdbc:goldilocks://192.168.0.120:42613/edk2613";
//        String user = "edk2613";
//        String password = "edk2613";
//        OrderDBHandler orderDBHandler = new OrderDBHandler(url, user, password);
//
//        LocalDateTime now = LocalDateTime.now();
//        OrderDTO orderDTO = new OrderDTO("ord001", "test_shop", "test_menu",
//                "test_user", "test_phone", "test_address",
//                now);
//
//        orderDBHandler.insertOrder(orderDTO);
//        orderDBHandler.close();
//    }
}