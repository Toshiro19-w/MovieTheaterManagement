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

    public List<KhachHang> getAllKhachHang() {
        List<KhachHang> list = new ArrayList<>();
        String sql = """
                SELECT\s
                nd.maNguoiDung,
                nd.hoTen,
                nd.soDienThoai,
                nd.email,
                kh.diemTichLuy
                FROM\s
                NguoiDung nd
                JOIN\s
                KhachHang kh ON nd.maNguoiDung = kh.maNguoiDung
                WHERE\s
                nd.loaiNguoiDung = 'KhachHang';""";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                KhachHang khachHang = new KhachHang();
                khachHang.setMaNguoiDung(rs.getInt("maNguoiDung"));
                khachHang.setHoTen(rs.getString("hoTen"));
                khachHang.setSoDienThoai(rs.getString("soDienThoai"));
                khachHang.setEmail(rs.getString("email"));
                khachHang.setDiemTichLuy(rs.getInt("diemTichLuy"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public KhachHang getKhachHangByMaVe(int maVe) throws SQLException {
        // Giả lập: Truy vấn cơ sở dữ liệu để lấy thông tin khách hàng qua maHoaDon
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
}