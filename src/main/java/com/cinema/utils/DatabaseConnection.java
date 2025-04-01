package com.cinema.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static Connection connection;

    static {
        try {
            connectDB();
        } catch (Exception e) {
            throw new RuntimeException("Không thể kết nối CSDL!", e);
        }
    }

    private static void connectDB() throws ClassNotFoundException, IOException, SQLException {
        Properties properties = new Properties();
        InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("Database.properties");
        if (input == null) {
            throw new FileNotFoundException("Không tìm thấy file cấu hình Database.properties!");
        }
        properties.load(input);

        String url = properties.getProperty("url");
        String username = properties.getProperty("username");
        String password = properties.getProperty("password");
        String driver = properties.getProperty("driver");

        Class.forName(driver);
        connection = DriverManager.getConnection(url, username, password);
        System.out.println("Kết nối CSDL thành công!");
    }

    public static Connection getConnection() {
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Đã đóng kết nối CSDL.");
            } catch (SQLException e) {
                System.err.println("Lỗi khi đóng kết nối: " + e.getMessage());
            }
        }
    }
}