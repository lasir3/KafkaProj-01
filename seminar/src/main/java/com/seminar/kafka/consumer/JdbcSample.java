package com.seminar.kafka.consumer;

import java.sql.*;

public class JdbcSample
{
    protected static final String GOLDILOCKS_DRIVER_CLASS = "sunje.goldilocks.jdbc.GoldilocksDriver";
    protected static final String URL_BASIC = "jdbc:goldilocks://192.168.0.120:42613/test";
    protected static final String URL_NAMED = "jdbc:goldilocks://192.168.0.120:42613/test?program=MySample";
    protected static final String URL_FOR_DEBUGGING = URL_BASIC + "?global_logger=console&trace_log=on&query_log=on&protocol_log=on";

    public static Connection createConnectionByDriverManager(String id, String password) throws SQLException
    {
        try
        {
            Class.forName(GOLDILOCKS_DRIVER_CLASS);
        }
        catch (ClassNotFoundException sException)
        {
        }

        return DriverManager.getConnection(URL_BASIC, "SYS", "gliese");
    }

    public static Connection createConnectionByDataSource(String id, String password) throws SQLException
    {
        sunje.goldilocks.jdbc.GoldilocksDataSource sDataSource = new sunje.goldilocks.jdbc.GoldilocksDataSource();

        sDataSource.setDatabaseName("test");
        sDataSource.setServerName("192.168.0.120");
        sDataSource.setPortNumber(42613);
        sDataSource.setUser(id);
        sDataSource.setPassword(password);

        return sDataSource.getConnection();
    }

    public static void main(String[] args) throws SQLException {
        Connection con = createConnectionByDriverManager("sys", "gliese");
        Statement stmt = con.createStatement();
//        stmt.execute("CREATE TABLE SAMPLE_TABLE ( ID INTEGER, NAME CHAR(20) )");
        for (int i = 0; i < 100; i++) {
                PreparedStatement pstmt = con.prepareStatement("INSERT INTO PUBLIC.ORDERS " +
                        "(ord_id, shop_id, menu_name, user_name, phone_number, address, order_time) " + /**/
                        "values (?, ?, ?, ?, ?, ?, ?)");
            pstmt.setString(1, "5006");
            pstmt.setString(2, "P001");
            pstmt.setString(3, "Cheese Pizza");
            pstmt.setString(4, "Alline Jacobs");
            pstmt.setString(5, "1-420-465-3970");
            pstmt.setString(6, "9605 Randy Shore");
            pstmt.setTimestamp(7, Timestamp.valueOf("2023-06-18 17:32:41"));
            pstmt.executeUpdate();
        }
        ResultSet rs = stmt.executeQuery("SELECT * FROM ORDERS@G1");
        while (rs.next())
        {
            System.out.println("ID = " + rs.getInt(1) + ": " + rs.getString(2));
        }
//        rs.close();
//        stmt.close();
//        pstmt.close();
//        con.close();
//        Connection con2 = createConnectionByDataSource("edk2613", "edk2613");
//        Statement stmt2 = con2.createStatement();
//        stmt2.execute("DROP TABLE SAMPLE_TABLE");
//        stmt2.close();
//        con2.close();
    }
}