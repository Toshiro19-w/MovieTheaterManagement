package com.cinema.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Xử lý khóa lạc quan (optimistic locking) cho các bảng
 */
public class OptimisticLockingHandler {
    
    /**
     * Kiểm tra và cập nhật phiên bản của bản ghi
     * @param connection kết nối cơ sở dữ liệu
     * @param tableName tên bảng
     * @param idColumnName tên cột ID
     * @param idValue giá trị ID
     * @param expectedVersion phiên bản dự kiến
     * @return true nếu cập nhật thành công
     * @throws SQLException nếu có lỗi SQL
     * @throws OptimisticLockingException nếu phiên bản không khớp
     */
    public static boolean checkAndUpdateVersion(Connection connection, String tableName, 
                                              String idColumnName, Object idValue, 
                                              int expectedVersion) throws SQLException, OptimisticLockingException {
        // Kiểm tra phiên bản hiện tại
        String selectSql = "SELECT phienBan FROM " + tableName + " WHERE " + idColumnName + " = ?";
        
        try (PreparedStatement selectStmt = connection.prepareStatement(selectSql)) {
            selectStmt.setObject(1, idValue);
            
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    int currentVersion = rs.getInt("phienBan");
                    
                    if (currentVersion != expectedVersion) {
                        throw new OptimisticLockingException("Dữ liệu đã bị thay đổi bởi người dùng khác. Vui lòng làm mới và thử lại.");
                    }
                    
                    // Cập nhật phiên bản
                    String updateSql = "UPDATE " + tableName + " SET phienBan = ? WHERE " + idColumnName + " = ? AND phienBan = ?";
                    
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                        updateStmt.setInt(1, expectedVersion + 1);
                        updateStmt.setObject(2, idValue);
                        updateStmt.setInt(3, expectedVersion);
                        
                        int rowsAffected = updateStmt.executeUpdate();
                        return rowsAffected > 0;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Lấy phiên bản hiện tại của bản ghi
     * @param connection kết nối cơ sở dữ liệu
     * @param tableName tên bảng
     * @param idColumnName tên cột ID
     * @param idValue giá trị ID
     * @return phiên bản hiện tại
     * @throws SQLException nếu có lỗi SQL
     */
    public static int getCurrentVersion(Connection connection, String tableName, 
                                      String idColumnName, Object idValue) throws SQLException {
        String sql = "SELECT phienBan FROM " + tableName + " WHERE " + idColumnName + " = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, idValue);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("phienBan");
                }
            }
        }
        
        return 0;
    }
    
    /**
     * Exception cho khóa lạc quan
     */
    public static class OptimisticLockingException extends Exception {
        public OptimisticLockingException(String message) {
            super(message);
        }
    }
}