package com.cinema.models.repositories;

import com.cinema.utils.DatabaseConnection;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class TheLoaiRepository {
    private final DatabaseConnection dbConnection;

    public TheLoaiRepository(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public Map<Integer, String> getAllTheLoaiMap() throws SQLException {
        Map<Integer, String> map = new HashMap<>();
        String sql = "SELECT matheloai, tentheloai FROM theloaiphim";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getInt("matheloai"), rs.getString("tentheloai"));
            }
        }
        return map;
    }

    public void addTheLoai(String tenTheLoai) throws SQLException {
        String sql = "INSERT INTO theloaiphim (tentheloai) VALUES (?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenTheLoai);
            ps.executeUpdate();
        }
    }

    public void updateTheLoai(int maTheLoai, String tenTheLoai) throws SQLException {
        String sql = "UPDATE theloaiphim SET tentheloai = ? WHERE matheloai = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenTheLoai);
            ps.setInt(2, maTheLoai);
            ps.executeUpdate();
        }
    }

    public void deleteTheLoai(int maTheLoai) throws SQLException {
        String sql = "DELETE FROM theloaiphim WHERE matheloai = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maTheLoai);
            ps.executeUpdate();
        }
    }
}