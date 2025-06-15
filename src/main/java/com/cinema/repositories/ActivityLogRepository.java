package com.cinema.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.cinema.models.ActivityLog;
import com.cinema.utils.DatabaseConnection;

public class ActivityLogRepository {
    private final DatabaseConnection dbConnection;

    public ActivityLogRepository(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }
    
    public List<ActivityLog> getRecentLogs(int limit) throws SQLException {
        List<ActivityLog> logs = new ArrayList<>();
        String query = "SELECT l.*, nd.hoTen FROM ActivityLog l " +
                       "JOIN NguoiDung nd ON l.maNguoiDung = nd.maNguoiDung " +
                       "ORDER BY l.thoiGian DESC LIMIT ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ActivityLog log = new ActivityLog();
                    log.setMaLog(rs.getInt("maLog"));
                    log.setLoaiHoatDong(rs.getString("loaiHoatDong"));
                    log.setMoTa(rs.getString("moTa"));
                    log.setThoiGian(rs.getTimestamp("thoiGian"));
                    log.setMaNguoiDung(rs.getInt("maNguoiDung"));
                    log.setTenNguoiDung(rs.getString("hoTen"));
                    
                    logs.add(log);
                }
            }
        }
        
        return logs;
    }
    
    public int addLog(ActivityLog log) throws SQLException {
        String query = "INSERT INTO ActivityLog (loaiHoatDong, moTa, thoiGian, maNguoiDung) " +
                       "VALUES (?, ?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, log.getLoaiHoatDong());
            stmt.setString(2, log.getMoTa());
            stmt.setTimestamp(3, new Timestamp(log.getThoiGian().getTime()));
            stmt.setInt(4, log.getMaNguoiDung());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Thêm log thất bại, không có dòng nào được thêm vào.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Thêm log thất bại, không lấy được ID.");
                }
            }
        }
    }
}