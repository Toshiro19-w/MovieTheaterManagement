package com.cinema.models.repositories;

import com.cinema.models.TaiKhoan;
import com.cinema.models.repositories.Interface.ITaiKhoanRepository;
import com.cinema.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;

public class TaiKhoanRepository implements ITaiKhoanRepository {
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

    @Override
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

    @Override
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

    @Override
    public void createTaiKhoan(TaiKhoan taiKhoan) throws SQLException {
        String sql = "INSERT INTO TaiKhoan (tenDangNhap, matKhau, loaiTaiKhoan, maNguoiDung) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, taiKhoan.getTenDangNhap());
            pstmt.setString(2, taiKhoan.getMatKhau());
            pstmt.setString(3, taiKhoan.getLoaiTaiKhoan());
            pstmt.setInt(4, taiKhoan.getMaNguoiDung());
            pstmt.executeUpdate();
        }
    }

    @Override
    public boolean existsByTenDangNhap(String tenDangNhap) throws SQLException {
        String sql = "SELECT COUNT(*) FROM TaiKhoan WHERE tenDangNhap = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tenDangNhap);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        }
    }

    @Override
    public boolean existsByMaNguoiDung(Integer maNguoiDung) throws SQLException {
        String sql = "SELECT COUNT(*) FROM TaiKhoan WHERE maNguoiDung = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, maNguoiDung);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        }
    }

    public boolean verifyUser(String username, String email, String phone) {
        String sql = "SELECT t.tenDangNhap FROM TaiKhoan t " +
                "JOIN NguoiDung n ON t.maNguoiDung = n.maNguoiDung " +
                "WHERE t.tenDangNhap = ? AND n.email = ? AND n.soDienThoai = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, phone);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xác minh người dùng: " + e.getMessage());
        }
    }

    public boolean updatePassword(String username, String hashedPassword) {
        String sql = "UPDATE TaiKhoan SET matKhau = ? WHERE tenDangNhap = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hashedPassword);
            stmt.setString(2, username);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật mật khẩu: " + e.getMessage());
        }
    }
}