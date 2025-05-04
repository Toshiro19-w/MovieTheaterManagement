package com.cinema.models.repositories;

import com.cinema.models.KhachHang;
import com.cinema.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KhachHangRepository {
    protected Connection conn;
    protected DatabaseConnection dbConnection;

    public KhachHangRepository(DatabaseConnection dbConnection) {
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

    //Lấy thông tin khách hàng qua maHoaDon
    public KhachHang getKhachHangByMaVe(int maVe) throws SQLException {
        String sql = """
                SELECT nd.maNguoiDung, nd.hoTen, nd.soDienThoai, nd.email\s
                FROM NguoiDung nd\s
                JOIN HoaDon hd ON nd.maNguoiDung = hd.maKhachHang
                JOIN Ve v ON v.maHoaDon = hd.maHoaDon
                WHERE v.maVe = ?;""";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maVe);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    KhachHang khachHang = new KhachHang();
                    khachHang.setMaNguoiDung(rs.getInt("maNguoiDung"));
                    khachHang.setHoTen(rs.getString("hoTen"));
                    khachHang.setSoDienThoai(rs.getString("soDienThoai"));
                    khachHang.setEmail(rs.getString("email"));
                    return khachHang;
                }
            }
        }
        return null; // Nếu không tìm thấy khách hàng
    }

    public KhachHang getKhachHangByUsername(String username) throws SQLException {
        String sql = """
            SELECT nd.maNguoiDung, nd.hoTen, nd.soDienThoai, nd.email
            FROM NguoiDung nd
            JOIN TaiKhoan tk ON nd.maNguoiDung = tk.maNguoiDung
            WHERE tk.tenDangNhap = ?""";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    KhachHang khachHang = new KhachHang();
                    khachHang.setMaNguoiDung(rs.getInt("maNguoiDung"));
                    khachHang.setHoTen(rs.getString("hoTen"));
                    khachHang.setSoDienThoai(rs.getString("soDienThoai"));
                    khachHang.setEmail(rs.getString("email"));
                    return khachHang;
                }
            }
        }
        return null;
    }
}