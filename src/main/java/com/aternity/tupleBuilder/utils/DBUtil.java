package com.aternity.tupleBuilder.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUtil {

    public static Statement getStatement(String epmIP, String userDB, String passwordDB) {
        try {
            Connection conn;
            conn = getDBConnection(epmIP, userDB, passwordDB);
            Statement statement = conn.createStatement();
            return statement;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;

    }
    public static Connection getDBConnection(String epmIP, String userDB, String passwordDB) throws Exception {
        return getConnection(epmIP, userDB, passwordDB);
    }
    public static Connection getConnection(String epmIp, String dbUser, String dbPassword) throws Exception {

        Connection connection = null;
        try {
            // Load the JDBC driver
            String driverName = "oracle.jdbc.driver.OracleDriver";
            Class.forName(driverName);
            String url = "jdbc:oracle:thin:@" + epmIp;
            connection = DriverManager.getConnection(url, dbUser, dbPassword);
        } catch (ClassNotFoundException e) {
            System.out.println("getConnection:ClassNotFoundException" + e.getMessage());
            // Could not find the database driver
        } catch (SQLException e) {
            System.out.println("getConnection:SQLException" + e.getMessage());
        }

        return connection;
    }
}
