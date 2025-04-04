package com.cinema.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private Connection connection;
    private final Properties properties;

    public DatabaseConnection() throws IOException {
        this.properties = loadProperties();
    }

    private Properties loadProperties() throws IOException {
        Properties props = new Properties();
        InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("Database.properties");
        try (input) {
            if (input == null) {
                throw new FileNotFoundException("Không tìm thấy file cấu hình Database.properties!");
            }
            props.load(input);
        }
        return props;
    }

    public Connection getConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return connection;
        }

        String url = properties.getProperty("url");
        String username = properties.getProperty("username");
        String password = properties.getProperty("password");
        String driver = properties.getProperty("driver");

        try {
            Class.forName(driver);
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Kết nối CSDL thành công!");
            return connection;
        } catch (ClassNotFoundException e) {
            throw new SQLException("Không tìm thấy driver JDBC: " + driver, e);
        }
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Đã đóng kết nối CSDL.");
            } catch (SQLException e) {
                System.err.println("Lỗi khi đóng kết nối: " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }
}