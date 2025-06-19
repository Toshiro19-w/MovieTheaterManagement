package com.cinema.utils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Lớp quản lý connection pool đến cơ sở dữ liệu sử dụng HikariCP
 */
public class DatabaseConnection implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static final int MAX_RETRY = 3;
    private static final long INITIAL_RETRY_DELAY = 2000; // 2 seconds
    private static final long MAX_RETRY_DELAY = 10000; // 10 seconds
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private HikariDataSource dataSource;
    private final Properties properties;
    private volatile boolean isInitialized = false;
    private LocalDateTime lastFailedAttempt;
    private Duration serverTimeDiff; // Lưu chênh lệch thời gian với server

    /**
     * Constructor mặc định
     * @throws IOException nếu không thể load properties
     */
    public DatabaseConnection() throws IOException {
        this.properties = loadProperties();
        initializeDataSource();
    }

    /**
     * Constructor cho phép inject properties (hữu ích cho testing)
     * @param properties Properties đã được cấu hình
     */
    public DatabaseConnection(Properties properties) {
        this.properties = properties;
        initializeDataSource();
    }

    private Properties loadProperties() throws IOException {
        Properties props = new Properties();
        try {
            props.load(getClass().getClassLoader().getResourceAsStream("Database.properties"));
            if (props.isEmpty()) {
                throw new IOException("Database.properties is empty or not found");
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load database properties", e);
            throw e;
        }
        return props;
    }

    private synchronized void initializeDataSource() {
        if (!isInitialized) {            // Nếu thất bại gần đây, đợi một khoảng thời gian
            if (lastFailedAttempt != null) {
                Duration duration = Duration.between(lastFailedAttempt, LocalDateTime.now());
                long elapsedSeconds = duration.getSeconds();
                if (elapsedSeconds < 30) { // Đợi ít nhất 30 giây giữa các lần thử
                    LOGGER.log(Level.WARNING, "Waiting before retry. Last attempt was {0} seconds ago", elapsedSeconds);
                    try {
                        Thread.sleep(1000); // Đợi 1 giây
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }

            for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
                try {
                    if (dataSource != null && !dataSource.isClosed()) {
                        try {
                            dataSource.close();
                        } catch (Exception e) {
                            LOGGER.warning("Error closing previous datasource: " + e.getMessage());
                        }
                    }

                    LOGGER.info("Attempting to initialize database connection pool (attempt " + attempt + ")");
                    dataSource = createDataSource();
                    
                    // Test connection với timeout ngắn hơn
                    try (Connection conn = dataSource.getConnection()) {
                        if (conn.isValid(5)) { // 5 seconds timeout
                            isInitialized = true;
                            lastFailedAttempt = null;
                            LOGGER.info("Database connection pool initialized successfully on attempt " + attempt);
                            return;
                        }
                    }
                } catch (Exception e) {
                    lastFailedAttempt = LocalDateTime.now();
                    LOGGER.log(Level.WARNING, "Attempt " + attempt + " failed to initialize pool", e);
                    
                    if (attempt < MAX_RETRY) {
                        long delay = Math.min(INITIAL_RETRY_DELAY * attempt, MAX_RETRY_DELAY);
                        LOGGER.info("Waiting " + delay + "ms before next attempt...");
                        try {
                            Thread.sleep(delay);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
            
            if (!isInitialized) {
                String error = "Failed to initialize database pool after " + MAX_RETRY + " attempts";
                LOGGER.severe(error);
                throw new RuntimeException(error);
            }
        }
    }

    private HikariConfig createHikariConfig() {
        HikariConfig config = new HikariConfig();
        
        // Cấu hình cơ bản
        config.setJdbcUrl(properties.getProperty("url"));
        config.setUsername(properties.getProperty("username"));
        config.setPassword(properties.getProperty("password"));
        config.setDriverClassName(properties.getProperty("driver"));
        
        // Cấu hình pool - giảm các thông số để tránh blocking quá lâu
        config.setMaximumPoolSize(Integer.parseInt(properties.getProperty("pool.maximumPoolSize", "10")));
        config.setMinimumIdle(Integer.parseInt(properties.getProperty("pool.minimumIdle", "2")));
        config.setIdleTimeout(Long.parseLong(properties.getProperty("pool.idleTimeout", "30000"))); // 30 seconds
        config.setConnectionTimeout(Long.parseLong(properties.getProperty("pool.connectionTimeout", "5000"))); // 5 seconds
        config.setMaxLifetime(Long.parseLong(properties.getProperty("pool.maxLifetime", "1800000"))); // 30 minutes
        config.setLeakDetectionThreshold(Long.parseLong(properties.getProperty("pool.leakDetectionThreshold", "60000")));
        
        // Cấu hình kiểm tra connection
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(3000); // 3 seconds
        config.setInitializationFailTimeout(1000); // 1 second
        config.setKeepaliveTime(30000); // 30 seconds
        
        // Cấu hình reconnect
        config.addDataSourceProperty("autoReconnect", "true");
        config.addDataSourceProperty("serverTimezone", "Asia/Ho_Chi_Minh");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("useUnicode", "true");
        config.addDataSourceProperty("characterEncoding", "utf8");
        config.addDataSourceProperty("connectTimeout", "5000"); // 5 seconds
        config.addDataSourceProperty("socketTimeout", "30000"); // 30 seconds
        
        config.setPoolName("MovieTheaterPool");

        return config;
    }

    private HikariDataSource createDataSource() {
        return new HikariDataSource(createHikariConfig());
    }

    public Connection getConnection() throws SQLException {
        if (!isInitialized || (dataSource != null && dataSource.isClosed())) {
            initializeDataSource();
        }
        
        SQLException lastException = null;
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try {
                Connection conn = dataSource.getConnection();
                if (conn.isValid(5)) { // 5 seconds timeout
                    LOGGER.fine("Database connection obtained successfully");
                    return conn;
                }
                conn.close();
            } catch (SQLException e) {
                lastException = e;
                LOGGER.log(Level.WARNING, "Attempt " + attempt + " failed to get connection", e);
                
                if (attempt < MAX_RETRY) {
                    long delay = Math.min(INITIAL_RETRY_DELAY * attempt, MAX_RETRY_DELAY);
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw e;
                    }
                    
                    // Thử khởi tạo lại pool nếu có vẻ pool đã bị hỏng
                    if (dataSource.isClosed() || !isPoolHealthy()) {
                        LOGGER.info("Reinitializing connection pool...");
                        initializeDataSource();
                    }
                }
            }
        }
        
        throw new SQLException("Failed to obtain valid connection after " + MAX_RETRY + " attempts", lastException);
    }

    private boolean isPoolHealthy() {
        try {
            return dataSource != null && 
                   !dataSource.isClosed() && 
                   dataSource.getHikariPoolMXBean().getActiveConnections() < dataSource.getHikariPoolMXBean().getTotalConnections();
        } catch (Exception e) {
            return false;
        }
    }

    public Connection getAutoCommitConnection() throws SQLException {
        Connection conn = getConnection();
        conn.setAutoCommit(true);
        return conn;
    }

    /**
     * Lấy thời gian hiện tại của database server
     * @return LocalDateTime của server hoặc local time nếu không lấy được server time
     */
    public LocalDateTime getServerTime() {
        String query = "SELECT NOW()";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Timestamp serverTimestamp = rs.getTimestamp(1);
                LocalDateTime serverTime = serverTimestamp.toLocalDateTime();
                
                // Cập nhật chênh lệch thời gian
                LocalDateTime localNow = LocalDateTime.now();
                serverTimeDiff = Duration.between(localNow, serverTime);
                
                LOGGER.log(Level.FINE, "Server time obtained: {0}", serverTime.format(DATE_TIME_FORMATTER));
                return serverTime;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Could not get server time: {0}", e.getMessage());
        }
        
        // Nếu không lấy được server time, trả về local time
        return LocalDateTime.now();
    }

    /**
     * Lấy thời gian hiện tại, ưu tiên sử dụng server time
     * @return LocalDateTime đã được điều chỉnh theo server time
     */
    public LocalDateTime getCurrentTime() {
        if (serverTimeDiff != null) {
            return LocalDateTime.now().plus(serverTimeDiff);
        }
        return getServerTime(); // Lấy và cập nhật server time nếu chưa có
    }

    /**
     * Format thời gian theo định dạng chuẩn của hệ thống
     * @param dateTime thời gian cần format
     * @return String đã được format
     */
    public String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    /**
     * Lấy thông tin về chênh lệch thời gian với server
     * @return String mô tả chênh lệch thời gian
     */
    public String getTimeOffset() {
        if (serverTimeDiff != null) {
            long seconds = serverTimeDiff.getSeconds();
            return String.format("Time offset from server: %d seconds", seconds);
        }
        return "Time offset not yet calculated";
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            try {
                dataSource.close();
                isInitialized = false;
                serverTimeDiff = null; // Reset server time difference
                LOGGER.info("Database connection pool closed successfully");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error while closing connection pool", e);
            }
        }
    }

    /**
     * Synchronize thời gian với server
     * @return true nếu đồng bộ thành công, false nếu thất bại
     */
    public boolean synchronizeTime() {
        try {
            LocalDateTime serverTime = getServerTime();
            if (serverTime != null) {
                LocalDateTime localTime = LocalDateTime.now();
                serverTimeDiff = Duration.between(localTime, serverTime);
                LOGGER.log(Level.INFO, "Time synchronized with server. Offset: {0} seconds", 
                          serverTimeDiff.getSeconds());
                return true;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to synchronize time with server: {0}", e.getMessage());
        }
        return false;
    }

    public String getPoolStats() {
        if (dataSource != null && !dataSource.isClosed()) {
            return String.format(
                "Pool Stats: Active=%d, Idle=%d, Total=%d, Waiting=%d",
                dataSource.getHikariPoolMXBean().getActiveConnections(),
                dataSource.getHikariPoolMXBean().getIdleConnections(),
                dataSource.getHikariPoolMXBean().getTotalConnections(),
                dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection()
            );
        }
        return "Pool is not available";
    }

    public boolean isPoolActive() {
        return isInitialized && dataSource != null && !dataSource.isClosed() && isPoolHealthy();
    }

    public void reinitialize() {
        close();
        initializeDataSource();
    }
}
