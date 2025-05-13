package com.cinema.models.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.cinema.models.ChiTietHoaDon;
import com.cinema.models.HoaDon;
import com.cinema.models.repositories.Interface.IHoaDonRepository;
import com.cinema.utils.DatabaseConnection;

public class HoaDonRepository implements IHoaDonRepository {
    private final DatabaseConnection databaseConnection;

    public HoaDonRepository(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    @Override
    public List<HoaDon> findAll() throws SQLException {
        List<HoaDon> hoaDons = new ArrayList<>();
        String sql = """
            SELECT h.*,
                   nv_nd.hoTen as tenNhanVien,
                   kh_nd.hoTen as tenKhachHang,
                   thd.tongTien
            FROM HoaDon h
            LEFT JOIN ThongKeHoaDon thd ON h.maHoaDon = thd.maHoaDon
            LEFT JOIN NhanVien nv ON h.maNhanVien = nv.maNguoiDung
            LEFT JOIN NguoiDung nv_nd ON nv.maNguoiDung = nv_nd.maNguoiDung
            LEFT JOIN KhachHang kh ON h.maKhachHang = kh.maNguoiDung
            LEFT JOIN NguoiDung kh_nd ON kh.maNguoiDung = kh_nd.maNguoiDung
            ORDER BY h.ngayLap DESC
        """;

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                HoaDon hoaDon = new HoaDon();
                hoaDon.setMaHoaDon(rs.getInt("maHoaDon"));
                hoaDon.setMaNhanVien(rs.getInt("maNhanVien"));
                hoaDon.setMaKhachHang(rs.getInt("maKhachHang"));
                hoaDon.setNgayLap(rs.getTimestamp("ngayLap").toLocalDateTime());
                hoaDon.setTenNhanVien(rs.getString("tenNhanVien"));
                hoaDon.setTenKhachHang(rs.getString("tenKhachHang"));
                hoaDon.setTongTien(rs.getBigDecimal("tongTien"));
                hoaDons.add(hoaDon);
            }
        }
        return hoaDons;
    }

    @Override
    public List<ChiTietHoaDon> findChiTietByMaHoaDon(int maHoaDon) throws SQLException {
        List<ChiTietHoaDon> chiTietList = new ArrayList<>();
        String sql = """
            SELECT cthd.maHoaDon,
                   v.maVe,
                   g.soGhe,
                   g.loaiGhe,
                   gv.giaVe,
                   p.tenPhim,
                   sc.ngayGioChieu,
                   v.trangThai
            FROM ChiTietHoaDon cthd
            JOIN Ve v ON cthd.maVe = v.maVe
            JOIN SuatChieu sc ON v.maSuatChieu = sc.maSuatChieu
            JOIN Phim p ON sc.maPhim = p.maPhim
            JOIN Ghe g ON v.maGhe = g.maGhe
            JOIN GiaVe gv ON v.maGiaVe = gv.maGiaVe
            WHERE cthd.maHoaDon = ?
            AND v.trangThai = 'paid'
            ORDER BY sc.ngayGioChieu
        """;

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, maHoaDon);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ChiTietHoaDon chiTiet = new ChiTietHoaDon(
                        rs.getInt("maHoaDon"),
                        rs.getInt("maVe")
                    );
                    chiTiet.setSoGhe(rs.getString("soGhe"));
                    chiTiet.setLoaiGhe(rs.getString("loaiGhe"));
                    chiTiet.setGiaVe(rs.getBigDecimal("giaVe"));
                    chiTiet.setTenPhim(rs.getString("tenPhim"));
                    chiTiet.setNgayGioChieu(rs.getTimestamp("ngayGioChieu").toLocalDateTime());
                    chiTietList.add(chiTiet);
                }
            }
        }
        return chiTietList;
    }

    public HoaDon findById(int maHoaDon) throws SQLException {
        String sql = """
            SELECT h.*,
                   nv_nd.hoTen as tenNhanVien,
                   kh_nd.hoTen as tenKhachHang,
                   thd.tongTien
            FROM HoaDon h
            JOIN ThongKeHoaDon thd ON h.maHoaDon = thd.maHoaDon
            LEFT JOIN NhanVien nv ON h.maNhanVien = nv.maNguoiDung
            LEFT JOIN NguoiDung nv_nd ON nv.maNguoiDung = nv_nd.maNguoiDung
            LEFT JOIN KhachHang kh ON h.maKhachHang = kh.maNguoiDung
            LEFT JOIN NguoiDung kh_nd ON kh.maNguoiDung = kh_nd.maNguoiDung
            WHERE h.maHoaDon = ?
        """;

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, maHoaDon);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    HoaDon hoaDon = new HoaDon();
                    hoaDon.setMaHoaDon(rs.getInt("maHoaDon"));
                    hoaDon.setMaNhanVien(rs.getInt("maNhanVien"));
                    hoaDon.setMaKhachHang(rs.getInt("maKhachHang"));
                    hoaDon.setNgayLap(rs.getTimestamp("ngayLap").toLocalDateTime());
                    hoaDon.setTenNhanVien(rs.getString("tenNhanVien"));
                    hoaDon.setTenKhachHang(rs.getString("tenKhachHang"));
                    return hoaDon;
                }
            }
        }
        return null;
    }

    public boolean insert(HoaDon hoaDon) throws SQLException {
        String sql = """
            INSERT INTO HoaDon (maNhanVien, maKhachHang, ngayLap)
            VALUES (?, NULL, ?)
        """;

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, hoaDon.getMaNhanVien());
            stmt.setTimestamp(2, Timestamp.valueOf(hoaDon.getNgayLap()));
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        hoaDon.setMaHoaDon(rs.getInt(1));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean update(HoaDon hoaDon) throws SQLException {
        String sql = """
            UPDATE HoaDon 
            SET maNhanVien = ?,
                maKhachHang = ?,
                ngayLap = ?
            WHERE maHoaDon = ?
        """;

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, hoaDon.getMaNhanVien());
            stmt.setInt(2, hoaDon.getMaKhachHang());
            stmt.setTimestamp(3, Timestamp.valueOf(hoaDon.getNgayLap()));
            stmt.setInt(4, hoaDon.getMaHoaDon());
            
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean delete(int maHoaDon) throws SQLException {
        String sql = "DELETE FROM HoaDon WHERE maHoaDon = ?";
        
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, maHoaDon);
            return stmt.executeUpdate() > 0;
        }
    }

    public List<ChiTietHoaDon> findChiTietByUsername(String username) throws SQLException {
        List<ChiTietHoaDon> chiTietList = new ArrayList<>();
        String sql = """
            SELECT cthd.maHoaDon,
                   v.maVe,
                   g.soGhe,
                   g.loaiGhe,
                   gv.giaVe,
                   p.tenPhim,
                   sc.ngayGioChieu,
                   v.trangThai,
                   COALESCE(km.giaTriGiam, 0) as giaTriGiam,
                   km.loaiGiamGia
            FROM ChiTietHoaDon cthd
            JOIN Ve v ON cthd.maVe = v.maVe
            JOIN SuatChieu sc ON v.maSuatChieu = sc.maSuatChieu
            JOIN Phim p ON sc.maPhim = p.maPhim
            JOIN Ghe g ON v.maGhe = g.maGhe
            JOIN GiaVe gv ON v.maGiaVe = gv.maGiaVe
            JOIN HoaDon h ON cthd.maHoaDon = h.maHoaDon
            JOIN KhachHang kh ON h.maKhachHang = kh.maNguoiDung
            JOIN TaiKhoan tk ON kh.maNguoiDung = tk.maNguoiDung
            LEFT JOIN KhuyenMai km ON v.maKhuyenMai = km.maKhuyenMai
            WHERE tk.tenDangNhap = ?
            AND v.trangThai = 'paid'
            ORDER BY sc.ngayGioChieu DESC
        """;

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ChiTietHoaDon chiTiet = new ChiTietHoaDon(
                        rs.getInt("maHoaDon"),
                        rs.getInt("maVe")
                    );
                    chiTiet.setSoGhe(rs.getString("soGhe"));
                    chiTiet.setLoaiGhe(rs.getString("loaiGhe"));
                    chiTiet.setGiaVe(rs.getBigDecimal("giaVe"));
                    chiTiet.setTenPhim(rs.getString("tenPhim"));
                    chiTiet.setNgayGioChieu(rs.getTimestamp("ngayGioChieu").toLocalDateTime());
                    chiTietList.add(chiTiet);
                }
            }
        }
        return chiTietList;
    }

    public List<Object[]> getKhuyenMaiByHoaDon(int maHoaDon) throws SQLException {
        String sql = """
            SELECT v.maVe,
                   km.tenKhuyenMai,
                   km.loaiGiamGia,
                   km.giaTriGiam
            FROM ChiTietHoaDon cthd
            JOIN Ve v ON cthd.maVe = v.maVe
            JOIN KhuyenMai km ON v.maKhuyenMai = km.maKhuyenMai
            WHERE cthd.maHoaDon = ?
            AND v.trangThai = 'paid'
        """;

        List<Object[]> results = new ArrayList<>();
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maHoaDon);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = {
                        rs.getInt("maVe"),
                        rs.getString("tenKhuyenMai"),
                        rs.getString("loaiGiamGia"),
                        rs.getBigDecimal("giaTriGiam")
                    };
                    results.add(row);
                }
            }
        }
        return results;
    }
}