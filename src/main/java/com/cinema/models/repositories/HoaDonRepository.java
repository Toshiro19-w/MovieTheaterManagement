package com.cinema.models.repositories;

import com.cinema.models.HoaDon;
import com.cinema.models.ChiTietHoaDon;
import com.cinema.models.repositories.Interface.IHoaDonRepository;
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

    // Lấy danh sách tất cả hóa đơn với thông tin nhân viên và khách hàng
    @Override
    public List<HoaDon> findAll() throws SQLException {
        List<HoaDon> hoaDons = new ArrayList<>();
        String sql = "SELECT h.maHoaDon, h.maNhanVien, h.maKhachHang, h.ngayLap, h.tongTien, " +
                "nd_nv.hoTen AS tenNhanVien, nd_kh.hoTen AS tenKhachHang " +
                "FROM HoaDon h " +
                "LEFT JOIN NhanVien nv ON h.maNhanVien = nv.maNguoiDung " +
                "LEFT JOIN NguoiDung nd_nv ON nv.maNguoiDung = nd_nv.maNguoiDung " +
                "LEFT JOIN KhachHang kh ON h.maKhachHang = kh.maNguoiDung " +
                "LEFT JOIN NguoiDung nd_kh ON kh.maNguoiDung = nd_kh.maNguoiDung";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                HoaDon hoaDon = new HoaDon();
                hoaDon.setMaHoaDon(rs.getInt("maHoaDon"));
                hoaDon.setMaNhanVien(rs.getInt("maNhanVien"));
                hoaDon.setMaKhachHang(rs.getInt("maKhachHang"));
                hoaDon.setNgayLap(rs.getTimestamp("ngayLap").toLocalDateTime());
                hoaDon.setTongTien(rs.getBigDecimal("tongTien"));
                hoaDon.setTenNhanVien(rs.getString("tenNhanVien"));
                hoaDon.setTenKhachHang(rs.getString("tenKhachHang"));
                hoaDons.add(hoaDon);
            }
        }
        return hoaDons;
    }

    // Lấy chi tiết hóa đơn theo maHoaDon
    @Override
    public List<ChiTietHoaDon> findChiTietByMaHoaDon(int maHoaDon) throws SQLException {
        List<ChiTietHoaDon> chiTietList = new ArrayList<>();
        String sql = "SELECT c.maHoaDon, c.maVe, v.soGhe, v.giaVe, p.tenPhim, sc.ngayGioChieu " +
                "FROM ChiTietHoaDon c " +
                "JOIN Ve v ON c.maVe = v.maVe " +
                "JOIN SuatChieu sc ON v.maSuatChieu = sc.maSuatChieu " +
                "JOIN Phim p ON sc.maPhim = p.maPhim " +
                "WHERE c.maHoaDon = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maHoaDon);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ChiTietHoaDon chiTiet = new ChiTietHoaDon(
                        rs.getInt("maHoaDon"),
                        rs.getInt("maVe")
                );
                chiTiet.setSoGhe(rs.getString("soGhe"));
                chiTiet.setGiaVe(rs.getBigDecimal("giaVe"));
                chiTiet.setTenPhim(rs.getString("tenPhim"));
                chiTiet.setNgayGioChieu(rs.getTimestamp("ngayGioChieu").toLocalDateTime());
                chiTietList.add(chiTiet);
            }
        }
        return chiTietList;
    }

    public List<ChiTietHoaDon> findChiTietByUsername(String username) throws SQLException {
        List<ChiTietHoaDon> chiTietList = new ArrayList<>();
        String sql = """
            SELECT c.maHoaDon, c.maVe, v.soGhe, v.giaVe, p.tenPhim, sc.ngayGioChieu
            FROM ChiTietHoaDon c
            JOIN Ve v ON c.maVe = v.maVe
            JOIN SuatChieu sc ON v.maSuatChieu = sc.maSuatChieu
            JOIN Phim p ON sc.maPhim = p.maPhim
            JOIN HoaDon h ON c.maHoaDon = h.maHoaDon
            JOIN TaiKhoan tk ON h.maKhachHang = tk.maNguoiDung
            WHERE tk.tenDangNhap = ?""";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ChiTietHoaDon chiTiet = new ChiTietHoaDon(
                        rs.getInt("maHoaDon"),
                        rs.getInt("maVe")
                );
                chiTiet.setSoGhe(rs.getString("soGhe"));
                chiTiet.setGiaVe(rs.getBigDecimal("giaVe"));
                chiTiet.setTenPhim(rs.getString("tenPhim"));
                chiTiet.setNgayGioChieu(rs.getTimestamp("ngayGioChieu").toLocalDateTime());
                chiTietList.add(chiTiet);
            }
        }
        return chiTietList;
    }
}