package com.cinema.repositories;

import com.cinema.repositories.Interface.IChiTietHoaDonRepository;
import com.cinema.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ChiTietHoaDonRepository implements IChiTietHoaDonRepository {
    protected Connection conn;
    protected DatabaseConnection dbConnection;

    public ChiTietHoaDonRepository(DatabaseConnection dbConnection) {
        if (dbConnection == null) {
            throw new IllegalArgumentException("DatabaseConnection cannot be null");
        }
        this.dbConnection = dbConnection;
        try {
            this.conn = dbConnection.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Không thể lấy kết nối cơ sở dữ liệu", e);
        }
    }

    public void createChiTietHoaDon(int maHoaDon, int maVe) throws SQLException {
        String sql = "INSERT INTO ChiTietHoaDon (maHoaDon, maVe) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maHoaDon);
            stmt.setInt(2, maVe);
            stmt.executeUpdate();
        }
    }
}