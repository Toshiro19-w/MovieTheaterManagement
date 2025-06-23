package com.cinema.repositories;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.cinema.models.ActivityLog;
import com.cinema.utils.DatabaseConnection;

/**
 * Repository xử lý các thao tác với bảng ActivityLog trong database
 */
public class ActivityLogRepository {
    private static final Logger LOGGER = Logger.getLogger(ActivityLogRepository.class.getName());
    private final DatabaseConnection dbConnection;
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    
    public ActivityLogRepository(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }
    
    /**
     * Thiết lập timezone cho connection
     */
    private void configureConnection(Connection conn) throws SQLException {
        // Đặt time_zone thành +7 cho connection hiện tại
        try (PreparedStatement stmt = conn.prepareStatement("SET time_zone = '+07:00'")) {
            stmt.execute();
        }
        // Kiểm tra timezone
        try (PreparedStatement stmt = conn.prepareStatement("SELECT @@session.time_zone");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                LOGGER.log(Level.FINE, "Current timezone: {0}", rs.getString(1));
            }
        }
    }
    
    /**
     * Lấy danh sách log gần đây
     */
    public List<ActivityLog> getRecentLogs(int limit) throws SQLException {
        List<ActivityLog> logs = new ArrayList<>();
        String query = "{CALL GetRecentLogs(?)}";
        
        try (Connection conn = dbConnection.getAutoCommitConnection()) {
            configureConnection(conn);
            
            try (CallableStatement stmt = conn.prepareCall(query)) {
                stmt.setInt(1, limit);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        logs.add(mapResultSetToActivityLog(rs));
                    }
                }
            }
        }
        
        return logs;
    }
    
    /**
     * Thêm log mới
     */
    public int addLog(ActivityLog log) throws SQLException {
        String query = "INSERT INTO ActivityLog (loaiHoatDong, moTa, thoiGian, maNguoiDung) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = dbConnection.getAutoCommitConnection()) {
            configureConnection(conn);
            
            try (PreparedStatement stmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, log.getLoaiHoatDong());
                stmt.setString(2, log.getMoTa());
                
                // Đảm bảo thời gian được lưu với đúng timezone
                LocalDateTime time = log.getThoiGian();
                if (time == null) {
                    // Nếu không có thời gian, sử dụng NOW() của server
                    try (PreparedStatement timeStmt = conn.prepareStatement("SELECT NOW()");
                         ResultSet rs = timeStmt.executeQuery()) {
                        if (rs.next()) {
                            time = rs.getTimestamp(1).toLocalDateTime();
                        }
                    }
                }
                stmt.setTimestamp(3, Timestamp.valueOf(time));
                stmt.setInt(4, log.getMaNguoiDung());
                
                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating log failed, no rows affected.");
                }
                
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Creating log failed, no ID obtained.");
                    }
                }
            }
        }
    }

    /**
     * Lấy thông tin chi tiết của một log
     */
    public ActivityLog getLogById(int logId) throws SQLException {
        String query = "SELECT l.*, nd.hoTen FROM ActivityLog l " +
                      "JOIN NguoiDung nd ON l.maNguoiDung = nd.maNguoiDung " +
                      "WHERE l.maLog = ?";
        
        try (Connection conn = dbConnection.getAutoCommitConnection()) {
            configureConnection(conn);
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, logId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToActivityLog(rs);
                    }
                    return null;
                }
            }
        }
    }

    /**
     * Xóa một log
     */
    public boolean deleteLog(int logId) throws SQLException {
        String query = "DELETE FROM ActivityLog WHERE maLog = ?";
        
        try (Connection conn = dbConnection.getAutoCommitConnection()) {
            configureConnection(conn);
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, logId);
                
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        }
    }

    /**
     * Xóa các log cũ hơn số ngày chỉ định
     */
    public int deleteOldLogs(int days) throws SQLException {
        String query = "DELETE FROM ActivityLog WHERE thoiGian < DATE_SUB(NOW(), INTERVAL ? DAY)";
        
        try (Connection conn = dbConnection.getAutoCommitConnection()) {
            configureConnection(conn);
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, days);
                
                return stmt.executeUpdate();
            }
        }
    }
    
    /**
     * Map ResultSet sang đối tượng ActivityLog
     */
    private ActivityLog mapResultSetToActivityLog(ResultSet rs) throws SQLException {
        ActivityLog log = new ActivityLog();
        log.setMaLog(rs.getInt("maLog"));
        log.setLoaiHoatDong(rs.getString("loaiHoatDong"));
        log.setMoTa(rs.getString("moTa"));
        
        // Lấy timestamp từ database và chuyển đổi về LocalDateTime
        Timestamp timestamp = rs.getTimestamp("thoiGian");
        if (timestamp != null) {
            log.setThoiGian(timestamp.toLocalDateTime());
        }
        
        log.setMaNguoiDung(rs.getInt("maNguoiDung"));
        
        // Kiểm tra xem có cột hoTen không trước khi đọc
        try {
            rs.findColumn("hoTen");
            log.setTenNguoiDung(rs.getString("hoTen"));
        } catch (SQLException e) {
            // Không có cột hoTen, bỏ qua
        }
        
        return log;
    }
}