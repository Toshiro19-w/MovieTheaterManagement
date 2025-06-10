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
    protected DatabaseConnection dbConnection;

    public KhachHangRepository(DatabaseConnection dbConnection) {
        if (dbConnection == null) {
            throw new IllegalArgumentException("DatabaseConnection cannot be null");
        }
        this.dbConnection = dbConnection;
    }

    // Lấy thông tin khách hàng qua maVe
    @Override
    public KhachHang getKhachHangByMaVe(int maVe) throws SQLException {
        // Truy vấn trực tiếp từ bảng Ve thay vì dựa vào view
        String sql = """
                SELECT nd.maNguoiDung, nd.hoTen, nd.soDienThoai, nd.email, kh.diemTichLuy
                FROM Ve v
                JOIN HoaDon hd ON v.maHoaDon = hd.maHoaDon
                JOIN NguoiDung nd ON hd.maKhachHang = nd.maNguoiDung
                JOIN KhachHang kh ON nd.maNguoiDung = kh.maNguoiDung
                WHERE v.maVe = ?""";
        
        try (Connection conn = dbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maVe);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    KhachHang khachHang = new KhachHang();
                    khachHang.setMaNguoiDung(rs.getInt("maNguoiDung"));
                    khachHang.setHoTen(rs.getString("hoTen"));
                    khachHang.setSoDienThoai(rs.getString("soDienThoai"));
                    khachHang.setEmail(rs.getString("email"));
                    khachHang.setDiemTichLuy(rs.getInt("diemTichLuy"));
                    System.out.println("Tìm thấy khách hàng: " + khachHang.getHoTen() + ", Điểm tích lũy: " + khachHang.getDiemTichLuy());
                    return khachHang;
                }
            }
        }
        System.out.println("Không tìm thấy khách hàng cho vé: " + maVe);
        return null;
    }

    @Override
    public KhachHang getKhachHangByUsername(String username) throws SQLException {
        String sql = """
            SELECT nd.maNguoiDung, nd.hoTen, nd.soDienThoai, nd.email
            FROM NguoiDung nd
            JOIN TaiKhoan tk ON nd.maNguoiDung = tk.maNguoiDung
            WHERE tk.tenDangNhap = ?""";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
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
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT nd.maNguoiDung FROM NguoiDung nd JOIN TaiKhoan tk ON nd.maNguoiDung = tk.maNguoiDung " +
                             "WHERE tk.tenDangNhap = ? AND nd.loaiNguoiDung = 'KhachHang'")) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("maNguoiDung");
                }
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
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement(); 
             ResultSet rs = stmt.executeQuery(sql)) {
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
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
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
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
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