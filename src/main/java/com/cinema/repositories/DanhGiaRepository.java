package com.cinema.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.cinema.models.DanhGia;
import com.cinema.utils.DatabaseConnection;

public class DanhGiaRepository {
    private final DatabaseConnection dbConnection;

    public DanhGiaRepository(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    // Lấy danh sách đánh giá của một phim, giới hạn số lượng
    public List<DanhGia> getDanhGiaByPhimId(int maPhim, int limit) throws SQLException {
        List<DanhGia> danhGiaList = new ArrayList<>();
        String query = "SELECT dg.*, nd.hoTen FROM DanhGia dg " +
                       "JOIN NguoiDung nd ON dg.maNguoiDung = nd.maNguoiDung " +
                       "WHERE dg.maPhim = ? " +
                       "ORDER BY dg.ngayDanhGia DESC " +
                       "LIMIT ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, maPhim);
            stmt.setInt(2, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DanhGia danhGia = new DanhGia();
                    danhGia.setMaDanhGia(rs.getInt("maDanhGia"));
                    danhGia.setMaPhim(rs.getInt("maPhim"));
                    danhGia.setMaNguoiDung(rs.getInt("maNguoiDung"));
                    danhGia.setMaVe(rs.getInt("maVe"));
                    danhGia.setDiemDanhGia(rs.getInt("diemDanhGia"));
                    danhGia.setNhanXet(rs.getString("nhanXet"));
                    
                    Timestamp timestamp = rs.getTimestamp("ngayDanhGia");
                    if (timestamp != null) {
                        danhGia.setNgayDanhGia(timestamp.toLocalDateTime());
                    }
                    
                    // Thêm tên người dùng vào đối tượng DanhGia
                    danhGia.setTenNguoiDung(rs.getString("hoTen"));
                    
                    danhGiaList.add(danhGia);
                }
            }
        }
        
        return danhGiaList;
    }
    
    // Kiểm tra xem khách hàng đã mua vé xem phim này chưa
    public boolean daXemPhim(int maKhachHang, int maPhim) throws SQLException {
        String query = "SELECT COUNT(*) FROM Ve v " +
                       "JOIN SuatChieu sc ON v.maSuatChieu = sc.maSuatChieu " +
                       "WHERE sc.maPhim = ? AND v.maHoaDon IN " +
                       "(SELECT h.maHoaDon FROM HoaDon h WHERE h.maKhachHang = ?) " +
                       "AND v.trangThai = 'paid'";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, maPhim);
            stmt.setInt(2, maKhachHang);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }
    
    // Lấy mã vé đã mua của khách hàng cho phim này
    public int getMaVeDaMua(int maKhachHang, int maPhim) throws SQLException {
        String query = "SELECT v.maVe FROM Ve v " +
                       "JOIN SuatChieu sc ON v.maSuatChieu = sc.maSuatChieu " +
                       "WHERE sc.maPhim = ? AND v.maHoaDon IN " +
                       "(SELECT h.maHoaDon FROM HoaDon h WHERE h.maKhachHang = ?) " +
                       "AND v.trangThai = 'paid' " +
                       "LIMIT 1";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, maPhim);
            stmt.setInt(2, maKhachHang);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("maVe");
                }
            }
        }
        
        return -1;
    }
    
    // Kiểm tra xem khách hàng đã đánh giá phim này chưa
    public boolean daDanhGia(int maKhachHang, int maPhim) throws SQLException {
        String query = "SELECT COUNT(*) FROM DanhGia " +
                       "WHERE maPhim = ? AND maNguoiDung = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, maPhim);
            stmt.setInt(2, maKhachHang);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }
    
    // Thêm đánh giá mới
    public int themDanhGia(DanhGia danhGia) throws SQLException {
        String query = "INSERT INTO DanhGia (maPhim, maNguoiDung, maVe, diemDanhGia, nhanXet, ngayDanhGia) " +
                       "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, danhGia.getMaPhim());
            stmt.setInt(2, danhGia.getMaNguoiDung());
            stmt.setInt(3, danhGia.getMaVe());
            stmt.setInt(4, danhGia.getDiemDanhGia());
            stmt.setString(5, danhGia.getNhanXet());
            stmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Thêm đánh giá thất bại, không có dòng nào được thêm vào.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Thêm đánh giá thất bại, không lấy được ID.");
                }
            }
        }
    }
    
    // Cập nhật đánh giá
    public boolean capNhatDanhGia(DanhGia danhGia) throws SQLException {
        String query = "UPDATE DanhGia SET diemDanhGia = ?, nhanXet = ?, ngayDanhGia = ? WHERE maDanhGia = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, danhGia.getDiemDanhGia());
            stmt.setString(2, danhGia.getNhanXet());
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(4, danhGia.getMaDanhGia());
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    // Lấy đánh giá theo ID
    public DanhGia getDanhGiaById(int maDanhGia) throws SQLException {
        String query = "SELECT dg.*, nd.hoTen FROM DanhGia dg " +
                       "JOIN NguoiDung nd ON dg.maNguoiDung = nd.maNguoiDung " +
                       "WHERE dg.maDanhGia = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, maDanhGia);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    DanhGia danhGia = new DanhGia();
                    danhGia.setMaDanhGia(rs.getInt("maDanhGia"));
                    danhGia.setMaPhim(rs.getInt("maPhim"));
                    danhGia.setMaNguoiDung(rs.getInt("maNguoiDung"));
                    danhGia.setMaVe(rs.getInt("maVe"));
                    danhGia.setDiemDanhGia(rs.getInt("diemDanhGia"));
                    danhGia.setNhanXet(rs.getString("nhanXet"));
                    
                    Timestamp timestamp = rs.getTimestamp("ngayDanhGia");
                    if (timestamp != null) {
                        danhGia.setNgayDanhGia(timestamp.toLocalDateTime());
                    }
                    
                    danhGia.setTenNguoiDung(rs.getString("hoTen"));
                    
                    return danhGia;
                }
            }
        }
        
        return null;
    }
    
    // Lấy đánh giá của người dùng cho một phim
    public DanhGia getDanhGiaByUserAndPhim(int maNguoiDung, int maPhim) throws SQLException {
        String query = "SELECT dg.*, nd.hoTen FROM DanhGia dg " +
                       "JOIN NguoiDung nd ON dg.maNguoiDung = nd.maNguoiDung " +
                       "WHERE dg.maNguoiDung = ? AND dg.maPhim = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, maNguoiDung);
            stmt.setInt(2, maPhim);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    DanhGia danhGia = new DanhGia();
                    danhGia.setMaDanhGia(rs.getInt("maDanhGia"));
                    danhGia.setMaPhim(rs.getInt("maPhim"));
                    danhGia.setMaNguoiDung(rs.getInt("maNguoiDung"));
                    danhGia.setMaVe(rs.getInt("maVe"));
                    danhGia.setDiemDanhGia(rs.getInt("diemDanhGia"));
                    danhGia.setNhanXet(rs.getString("nhanXet"));
                    
                    Timestamp timestamp = rs.getTimestamp("ngayDanhGia");
                    if (timestamp != null) {
                        danhGia.setNgayDanhGia(timestamp.toLocalDateTime());
                    }
                    
                    danhGia.setTenNguoiDung(rs.getString("hoTen"));
                    
                    return danhGia;
                }
            }
        }
        
        return null;
    }
    
    // Lấy điểm đánh giá trung bình của phim
    public double getDiemDanhGiaTrungBinh(int maPhim) throws SQLException {
        String query = "SELECT AVG(diemDanhGia) FROM DanhGia WHERE maPhim = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, maPhim);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        
        return 0.0;
    }
}