package com.cinema.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private HikariDataSource dataSource;
    private final Properties properties;

    public DatabaseConnection() throws IOException {
        this.properties = loadProperties();
        initializeDataSource();
    }

    private Properties loadProperties() throws IOException {
        Properties props = new Properties();
        try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("Database.properties")) {
            if (input == null) {
                throw new FileNotFoundException("Không tìm thấy file cấu hình Database.properties!");
            }
            props.load(input);
        }
        return props;
    }

    private void initializeDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(properties.getProperty("url"));
        config.setUsername(properties.getProperty("username"));
        config.setPassword(properties.getProperty("password"));
        config.setDriverClassName(properties.getProperty("driver"));
        
        // Đọc cấu hình connection pool từ file properties
        config.setMaximumPoolSize(Integer.parseInt(properties.getProperty("pool.maximumPoolSize", "20")));
        config.setMinimumIdle(Integer.parseInt(properties.getProperty("pool.minimumIdle", "5")));
        config.setIdleTimeout(Long.parseLong(properties.getProperty("pool.idleTimeout", "60000")));
        config.setConnectionTimeout(Long.parseLong(properties.getProperty("pool.connectionTimeout", "30000")));
        config.setMaxLifetime(Long.parseLong(properties.getProperty("pool.maxLifetime", "1800000")));
        config.setLeakDetectionThreshold(Long.parseLong(properties.getProperty("pool.leakDetectionThreshold", "60000")));
        
        dataSource = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void closeConnection() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
