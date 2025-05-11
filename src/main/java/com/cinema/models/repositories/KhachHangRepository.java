package com.cinema.models.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.cinema.models.KhachHang;
import com.cinema.models.repositories.Interface.IKhachHangRepository;
import com.cinema.utils.DatabaseConnection;

public class KhachHangRepository implements IKhachHangRepository {
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

    // Lấy thông tin khách hàng qua maHoaDon
    @Override
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

    @Override
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

    @Override
    public int getMaKhachHangFromSession(String username) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                     "SELECT nd.maNguoiDung FROM NguoiDung nd JOIN TaiKhoan tk ON nd.maNguoiDung = tk.maNguoiDung " +
                             "WHERE tk.tenDangNhap = ? AND nd.loaiNguoiDung = 'KhachHang'")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("maNguoiDung");
            }
        }
        return -1;
    }

    // Phương thức lấy tất cả khách hàng
    @Override
    public List<KhachHang> findAll() throws SQLException {
        List<KhachHang> list = new ArrayList<>();
        String sql = """
                SELECT nd.maNguoiDung, nd.hoTen, nd.soDienThoai, nd.email, kh.diemTichLuy
                FROM NguoiDung nd
                JOIN KhachHang kh ON nd.maNguoiDung = kh.maNguoiDung""";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                KhachHang khachHang = new KhachHang();
                khachHang.setMaNguoiDung(rs.getInt("maNguoiDung"));
                khachHang.setHoTen(rs.getString("hoTen"));
                khachHang.setSoDienThoai(rs.getString("soDienThoai"));
                khachHang.setEmail(rs.getString("email"));
                khachHang.setDiemTichLuy(rs.getInt("diemTichLuy"));
                list.add(khachHang);
            }
        }
        return list;
    }

    @Override
    public List<KhachHang> searchKhachHang(String keyword) throws SQLException {
        List<KhachHang> list = new ArrayList<>();
        String sql = """
                SELECT nd.maNguoiDung, nd.hoTen, nd.soDienThoai, nd.email
                FROM NguoiDung nd
                JOIN KhachHang kh ON nd.maNguoiDung = kh.maNguoiDung
                WHERE LOWER(nd.hoTen) LIKE LOWER(?) 
                OR nd.soDienThoai LIKE ?
                OR LOWER(nd.email) LIKE LOWER(?)
                LIMIT 10""";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    KhachHang khachHang = new KhachHang();
                    khachHang.setMaNguoiDung(rs.getInt("maNguoiDung"));
                    khachHang.setHoTen(rs.getString("hoTen"));
                    khachHang.setSoDienThoai(rs.getString("soDienThoai"));
                    khachHang.setEmail(rs.getString("email"));
                    list.add(khachHang);
                }
            }
        }
        return list;
    }

    @Override
    public List<KhachHang> findRecentKhachHang(int limit) throws SQLException {
        List<KhachHang> list = new ArrayList<>();
        String sql = """
                SELECT nd.maNguoiDung, nd.hoTen, nd.soDienThoai, nd.email
                FROM NguoiDung nd
                JOIN KhachHang kh ON nd.maNguoiDung = kh.maNguoiDung
                ORDER BY nd.maNguoiDung DESC
                LIMIT ?""";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    KhachHang khachHang = new KhachHang();
                    khachHang.setMaNguoiDung(rs.getInt("maNguoiDung"));
                    khachHang.setHoTen(rs.getString("hoTen"));
                    khachHang.setSoDienThoai(rs.getString("soDienThoai"));
                    khachHang.setEmail(rs.getString("email"));
                    list.add(khachHang);
                }
            }
        }
        return list;
    }
}