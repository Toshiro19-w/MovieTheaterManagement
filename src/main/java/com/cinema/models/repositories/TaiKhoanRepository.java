package com.cinema.models.repositories;

import com.cinema.models.LoaiTaiKhoan;
import com.cinema.models.TaiKhoan;
import com.cinema.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaiKhoanRepository {
    protected Connection conn;
    protected DatabaseConnection dbConnection;

    public TaiKhoanRepository(DatabaseConnection dbConnection) {
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

    public List<TaiKhoan> getAllTaiKhoan() {
        List<TaiKhoan> list = new ArrayList<>();
        String sql = "SELECT tenDangNhap, loaiTaiKhoan FROM TaiKhoan";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new TaiKhoan(
                        rs.getString("tenDangNhap"),
                        "",  // Không cần lấy mật khẩu
                        (LoaiTaiKhoan) rs.getObject("loaiTaiKhoan"),
                        rs.getInt("maNguoiDung")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean checkUser(String tenDangNhap, String matKhau) {
        String sql = "SELECT matKhau FROM TaiKhoan WHERE tenDangNhap = ? AND loaiTaiKhoan = 'user'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenDangNhap);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String hashedPassword = rs.getString("matKhau");
                    return matKhau.equals(hashedPassword);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void saveResetTokenToDB(String email, String token) {
        String sql = "INSERT INTO ResetToken (email, token, expiration_time) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, token);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now().plusMinutes(15))); // token có hiệu lực 15 phút
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean checkEmailExists(String email) {
        String query = "SELECT COUNT(*) FROM NguoiDung WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // Trả về true nếu email tồn tại
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi kiểm tra email: " + e.getMessage());
        }
    }
}