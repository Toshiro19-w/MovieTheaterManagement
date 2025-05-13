package com.cinema.services;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.cinema.models.ChiTietHoaDon;
import com.cinema.models.HoaDon;
import com.cinema.models.NguoiDung;
import com.cinema.models.repositories.HoaDonRepository;
import com.cinema.utils.DatabaseConnection;

public class HoaDonService {
    private final HoaDonRepository repository;
    private NguoiDung currentUser;
    private final DatabaseConnection databaseConnection;

    public HoaDonService(DatabaseConnection connection) {
        this.repository = new HoaDonRepository(connection);
        this.databaseConnection = connection;
    }

    public void setCurrentUser(NguoiDung user) {
        this.currentUser = user;
    }

    public NguoiDung getCurrentUser() {
        return currentUser;
    }

    public List<HoaDon> getAllHoaDon() throws SQLException {
        return repository.findAll();
    }

    public HoaDon getHoaDonById(int maHoaDon) throws SQLException {
        return repository.findById(maHoaDon);
    }

    public List<ChiTietHoaDon> getChiTietHoaDon(int maHoaDon) throws SQLException {
        return repository.findChiTietByMaHoaDon(maHoaDon);
    }

    public boolean insertHoaDon(HoaDon hoaDon) throws SQLException {
        return repository.insert(hoaDon);
    }

    public boolean updateHoaDon(HoaDon hoaDon) throws SQLException {
        return repository.update(hoaDon);
    }

    public boolean deleteHoaDon(int maHoaDon) throws SQLException {
        return repository.delete(maHoaDon);
    }

    public NguoiDung getNguoiDungByUsername(String username) throws SQLException {
        String sql = """
            SELECT nd.* 
            FROM NguoiDung nd
            JOIN TaiKhoan tk ON nd.maNguoiDung = tk.maNguoiDung
            WHERE tk.tenDangNhap = ?
        """;

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    NguoiDung nguoiDung = new NguoiDung();
                    nguoiDung.setMaNguoiDung(rs.getInt("maNguoiDung"));
                    nguoiDung.setHoTen(rs.getString("hoTen"));
                    nguoiDung.setSoDienThoai(rs.getString("soDienThoai"));
                    nguoiDung.setEmail(rs.getString("email"));
                    return nguoiDung;
                }
            }
        }
        return null;
    }

    public BigDecimal calculateTotalAmount(int maHoaDon) throws SQLException {
        String sql = """
            SELECT COALESCE(SUM(
                CASE 
                    WHEN km.loaiGiamGia = 'PhanTram' THEN gv.giaVe * (1 - km.giaTriGiam/100)
                    WHEN km.loaiGiamGia = 'CoDinh' THEN gv.giaVe - km.giaTriGiam
                    ELSE gv.giaVe
                END
            ), 0) as tongTien
            FROM ChiTietHoaDon cthd
            JOIN Ve v ON cthd.maVe = v.maVe
            JOIN GiaVe gv ON v.maGiaVe = gv.maGiaVe
            LEFT JOIN KhuyenMai km ON v.maKhuyenMai = km.maKhuyenMai
            WHERE cthd.maHoaDon = ?
            AND v.trangThai = 'paid'
        """;

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, maHoaDon);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("tongTien");
                }
            }
        }
        return BigDecimal.ZERO;
    }

    public List<Object[]> getChiTietVeWithGia(int maHoaDon) throws SQLException {
        String sql = """
            SELECT 
                v.maVe,
                g.soGhe,
                g.loaiGhe,
                gv.giaVe as giaGoc,
                km.tenKhuyenMai,
                km.loaiGiamGia,
                km.giaTriGiam,
                CASE 
                    WHEN km.loaiGiamGia = 'PhanTram' THEN gv.giaVe * (1 - km.giaTriGiam/100)
                    WHEN km.loaiGiamGia = 'CoDinh' THEN gv.giaVe - km.giaTriGiam
                    ELSE gv.giaVe
                END as giaSauGiam
            FROM ChiTietHoaDon cthd
            JOIN Ve v ON cthd.maVe = v.maVe
            JOIN Ghe g ON v.maGhe = g.maGhe
            JOIN GiaVe gv ON v.maGiaVe = gv.maGiaVe
            LEFT JOIN KhuyenMai km ON v.maKhuyenMai = km.maKhuyenMai
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
                        rs.getString("soGhe"),
                        rs.getString("loaiGhe"),
                        rs.getBigDecimal("giaGoc"),
                        rs.getString("tenKhuyenMai"),
                        rs.getString("loaiGiamGia"),
                        rs.getBigDecimal("giaTriGiam"),
                        rs.getBigDecimal("giaSauGiam")
                    };
                    results.add(row);
                }
            }
        }
        return results;
    }

    public List<Object[]> getDanhSachKhachHang() throws SQLException {
        String sql = """
            SELECT 
                kh.maNguoiDung,
                nd.hoTen,
                nd.soDienThoai,
                nd.email
            FROM KhachHang kh
            JOIN NguoiDung nd ON kh.maNguoiDung = nd.maNguoiDung
            ORDER BY nd.hoTen
        """;

        List<Object[]> results = new ArrayList<>();
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("maNguoiDung"),
                    rs.getString("hoTen"),
                    rs.getString("soDienThoai"),
                    rs.getString("email")
                };
                results.add(row);
            }
        }
        return results;
    }

    public List<Object[]> getVeCoTheThem() throws SQLException {
        String sql = """
            SELECT 
                v.maVe,
                p.tenPhim,
                g.soGhe,
                g.loaiGhe,
                sc.ngayGioChieu,
                gv.giaVe,
                v.trangThai
            FROM Ve v
            JOIN SuatChieu sc ON v.maSuatChieu = sc.maSuatChieu
            JOIN Phim p ON sc.maPhim = p.maPhim
            JOIN Ghe g ON v.maGhe = g.maGhe
            JOIN GiaVe gv ON v.maGiaVe = gv.maGiaVe
            WHERE v.trangThai = 'pending'
            ORDER BY sc.ngayGioChieu
        """;

        List<Object[]> results = new ArrayList<>();
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("maVe"),
                    rs.getString("tenPhim"),
                    rs.getString("soGhe"),
                    rs.getString("loaiGhe"),
                    rs.getTimestamp("ngayGioChieu"),
                    rs.getBigDecimal("giaVe"),
                    rs.getString("trangThai")
                };
                results.add(row);
            }
        }
        return results;
    }

    public boolean themVeVaoHoaDon(int maHoaDon, int maVe) throws SQLException {
        String sql = """
            INSERT INTO ChiTietHoaDon (maHoaDon, maVe)
            VALUES (?, ?)
        """;

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, maHoaDon);
            stmt.setInt(2, maVe);
            
            if (stmt.executeUpdate() > 0) {
                // Cập nhật trạng thái vé thành 'paid'
                sql = "UPDATE Ve SET trangThai = 'paid', maHoaDon = ? WHERE maVe = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(sql)) {
                    updateStmt.setInt(1, maHoaDon);
                    updateStmt.setInt(2, maVe);
                    return updateStmt.executeUpdate() > 0;
                }
            }
        }
        return false;
    }

    public boolean capNhatKhachHang(int maHoaDon, int maKhachHang) throws SQLException {
        String sql = "UPDATE HoaDon SET maKhachHang = ? WHERE maHoaDon = ?";
        
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, maKhachHang);
            stmt.setInt(2, maHoaDon);
            return stmt.executeUpdate() > 0;
        }
    }
}