package com.cinema.repositories;

import com.cinema.models.HoaDon;
import com.cinema.repositories.Interface.IHoaDonRepository;
import com.cinema.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HoaDonRepository implements IHoaDonRepository {
    protected Connection conn;
    protected DatabaseConnection dbConnection;

    public HoaDonRepository(DatabaseConnection dbConnection) {
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
    public List<HoaDon> getHoaDonByTenKhachHang(String tenKhachHang) throws SQLException {
        List<HoaDon> hoaDonList = new ArrayList<>();
        String sql = "SELECT hd.* FROM HoaDon hd " +
                "JOIN KhachHang kh ON hd.maKhachHang = kh.maNguoiDung " +
                "JOIN NguoiDung nd ON kh.maNguoiDung = nd.maNguoiDung " +
                "WHERE nd.hoTen LIKE ? AND nd.loaiNguoiDung = 'KhachHang'";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, "%" + tenKhachHang + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                HoaDon hd = new HoaDon();
                hd.setMaHoaDon(rs.getInt("maHoaDon"));
                hd.setMaKhachHang(rs.getInt("maKhachHang"));
                hd.setNgayLap(rs.getTimestamp("ngayLap").toLocalDateTime());
                hd.setTongTien(rs.getBigDecimal("tongTien"));
                hoaDonList.add(hd);
            }
        }
        return hoaDonList;
    }

    @Override
    public List<HoaDon> findByKhachHang(int maKhachHang) throws SQLException {
        List<HoaDon> list = new ArrayList<>();
        String sql = "SELECT * FROM HoaDon WHERE maKhachHang = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maKhachHang);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new HoaDon(
                            rs.getInt("maHoaDon"),
                            rs.getObject("maNhanVien") != null ? rs.getInt("maNhanVien") : null,
                            rs.getObject("maKhachHang") != null ? rs.getInt("maKhachHang") : null,
                            rs.getTimestamp("ngayLap").toLocalDateTime(),
                            rs.getBigDecimal("tongTien")
                    ));
                }
            }
        }
        return list;
    }

    @Override
    public List<String> getAllTenKhachHang() throws SQLException {
        List<String> tenKhachHangList = new ArrayList<>();
        String sql = "SELECT DISTINCT nd.hoTen FROM NguoiDung nd " +
                "JOIN KhachHang kh ON nd.maNguoiDung = kh.maNguoiDung " +
                "WHERE nd.loaiNguoiDung = 'KhachHang'";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tenKhachHangList.add(rs.getString("hoTen"));
            }
        }
        return tenKhachHangList;
    }
}
