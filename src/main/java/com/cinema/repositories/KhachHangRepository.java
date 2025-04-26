package com.cinema.repositories;

import com.cinema.models.KhachHang;
import com.cinema.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
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
        String sql = "SELECT \n" + "nd.maNguoiDung,\n" + "kh.maKhachHang,\n" + "nd.hoTen,\n" +
                "nd.soDienThoai,\n" + "nd.email,\n" + "kh.diemTichLuy\n" +
                "FROM \n" + "NguoiDung nd\n" +
                "JOIN \n" + "KhachHang kh ON nd.maNguoiDung = kh.maNguoiDung\n" +
                "WHERE \n" + "nd.loaiNguoiDung = 'KhachHang';";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                KhachHang khachHang = new KhachHang();
                khachHang.setMaNguoiDung(rs.getInt("maNguoiDung"));
                khachHang.setMaKhachHang(rs.getInt("maKhachHang"));
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

    public int getIdByEmail(String email) {
        String sql = "SELECT maKhachHang FROM KhachHang WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("maKhachHang");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    public KhachHang getKhachHangInfoById(int maKhachHang) {
        String sql = "SELECT nd.hoTen nd.email, nd.soDienThoai, kh.diemTichLuy " +
                     "FROM KhachHang kh " +
                     "JOIN NguoiDung nd ON kh.maNguoiDung = nd.maNguoiDung " +
                     "WHERE kh.maKhachHang = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maKhachHang);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    KhachHang kh = new KhachHang();
                    kh.setHoTen(rs.getString("hoTen"));
                    kh.setEmail(rs.getString("email"));
                    kh.setSoDienThoai(rs.getString("soDienThoai"));
                    kh.setDiemTichLuy(rs.getInt("diemTichLuy"));
                    return kh;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}