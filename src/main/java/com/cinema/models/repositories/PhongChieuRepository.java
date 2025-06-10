package com.cinema.models.repositories;

import com.cinema.models.PhongChieu;
import com.cinema.models.repositories.Interface.IPhongChieuRepository;
import com.cinema.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PhongChieuRepository implements IPhongChieuRepository {
    protected final DatabaseConnection dbConnection;

    public PhongChieuRepository(DatabaseConnection dbConnection) {
        if (dbConnection == null) {
            throw new IllegalArgumentException("DatabaseConnection cannot be null");
        }
        this.dbConnection = dbConnection;
    }

    @Override
    public List<PhongChieu> findAll() throws SQLException {
        List<PhongChieu> list = new ArrayList<>();
        String sql = "SELECT * FROM PhongChieu";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new PhongChieu(
                        rs.getInt("maPhong"),
                        rs.getString("tenPhong"),
                        rs.getInt("soLuongGhe"),
                        rs.getString("loaiPhong")
                ));
            }
        }
        return list;
    }
    
    public PhongChieu findById(int maPhong) throws SQLException {
        String sql = "SELECT * FROM PhongChieu WHERE maPhong = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new PhongChieu(
                            rs.getInt("maPhong"),
                            rs.getString("tenPhong"),
                            rs.getInt("soLuongGhe"),
                            rs.getString("loaiPhong")
                    );
                }
            }
        }
        return null;
    }
    
    public PhongChieu findByTenPhong(String tenPhong) throws SQLException {
        String sql = "SELECT * FROM PhongChieu WHERE tenPhong = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new PhongChieu(
                            rs.getInt("maPhong"),
                            rs.getString("tenPhong"),
                            rs.getInt("soLuongGhe"),
                            rs.getString("loaiPhong")
                    );
                }
            }
        }
        return null;
    }
    
    public PhongChieu save(PhongChieu phongChieu) throws SQLException {
        String sql = "INSERT INTO PhongChieu (tenPhong, soLuongGhe, loaiPhong) VALUES (?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, phongChieu.getTenPhong());
            stmt.setInt(2, phongChieu.getSoLuongGhe());
            stmt.setString(3, phongChieu.getLoaiPhong());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Tạo phòng chiếu thất bại, không có dòng nào được thêm vào.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    phongChieu.setMaPhong(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Tạo phòng chiếu thất bại, không lấy được ID.");
                }
            }
        }
        return phongChieu;
    }
    
    public PhongChieu update(PhongChieu phongChieu) throws SQLException {
        String sql = "UPDATE PhongChieu SET tenPhong = ?, soLuongGhe = ?, loaiPhong = ? WHERE maPhong = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, phongChieu.getTenPhong());
            stmt.setInt(2, phongChieu.getSoLuongGhe());
            stmt.setString(3, phongChieu.getLoaiPhong());
            stmt.setInt(4, phongChieu.getMaPhong());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Cập nhật phòng chiếu thất bại, không tìm thấy phòng với mã: " + phongChieu.getMaPhong());
            }
        }
        return phongChieu;
    }
    
    public void delete(int maPhong) throws SQLException {
        // Kiểm tra xem phòng có đang được sử dụng không
        String checkSql = "SELECT COUNT(*) FROM SuatChieu WHERE maPhong = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, maPhong);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Không thể xóa phòng chiếu này vì đang được sử dụng bởi một hoặc nhiều suất chiếu.");
                }
            }
        }
        
        // Xóa các ghế trong phòng trước
        String deleteGheSql = "DELETE FROM Ghe WHERE maPhong = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement deleteGheStmt = conn.prepareStatement(deleteGheSql)) {
            deleteGheStmt.setInt(1, maPhong);
            deleteGheStmt.executeUpdate();
        }
        
        // Sau đó xóa phòng
        String sql = "DELETE FROM PhongChieu WHERE maPhong = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maPhong);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Xóa phòng chiếu thất bại, không tìm thấy phòng với mã: " + maPhong);
            }
        }
    }
    
    public boolean isPhongChieuExists(String tenPhong, int excludeMaPhong) throws SQLException {
        String sql = "SELECT COUNT(*) FROM PhongChieu WHERE tenPhong = ? AND maPhong != ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenPhong);
            stmt.setInt(2, excludeMaPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}