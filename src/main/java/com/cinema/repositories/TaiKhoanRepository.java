package com.cinema.repositories;

import com.cinema.models.TaiKhoan;
import com.cinema.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TaiKhoanRepository implements ITaiKhoanRepository {
    private final Connection conn;

    public TaiKhoanRepository() {
        conn = DatabaseConnection.getConnection();
    }

    @Override
    public List<TaiKhoan> getAllTaiKhoan() {
        List<TaiKhoan> list = new ArrayList<>();
        String sql = "SELECT tenDangNhap, loaiTaiKhoan FROM TaiKhoan";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new TaiKhoan(
                        rs.getString("tenDangNhap"),
                        "",  // Không cần lấy mật khẩu
                        rs.getString("loaiTaiKhoan")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
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

    @Override
    public boolean dangKyTaiKhoan(TaiKhoan tk) {
        String checkSql = "SELECT COUNT(*) FROM TaiKhoan WHERE tenDangNhap = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, tk.getTenDangNhap());
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return false;  // Tên đăng nhập đã tồn tại
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String sql = "INSERT INTO TaiKhoan (tenDangNhap, matKhau, loaiTaiKhoan) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tk.getTenDangNhap());
            stmt.setString(2, tk.getMatKhau());
            stmt.setString(3, tk.getLoaiTaiKhoan());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}