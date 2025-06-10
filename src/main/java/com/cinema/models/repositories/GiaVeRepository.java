package com.cinema.models.repositories;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.cinema.models.GiaVe;
import com.cinema.models.repositories.Interface.IGiaVeRepository;
import com.cinema.utils.DatabaseConnection;

public class GiaVeRepository extends BaseRepository<GiaVe> implements IGiaVeRepository {

    public GiaVeRepository(DatabaseConnection dbConnection) {
        super(dbConnection);
    }

    @Override
    public List<GiaVe> findAll() throws SQLException {
        List<GiaVe> giaVeList = new ArrayList<>();
        String query = "SELECT * FROM GiaVe ORDER BY ngayApDung DESC";
        
        try (Connection conn = dbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                giaVeList.add(extractFromResultSet(rs));
            }
        }
        
        return giaVeList;
    }

    @Override
    public GiaVe findById(int id) throws SQLException {
        String query = "SELECT * FROM GiaVe WHERE maGiaVe = ?";
        
        try (Connection conn = dbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractFromResultSet(rs);
                }
            }
        }
        
        return null;
    }

    @Override
    public GiaVe save(GiaVe entity) throws SQLException {
        String query = "INSERT INTO GiaVe (loaiGhe, ngayApDung, giaVe, ghiChu) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, entity.getLoaiGhe());
            stmt.setDate(2, Date.valueOf(entity.getNgayApDung()));
            stmt.setBigDecimal(3, entity.getGiaVe());
            stmt.setString(4, entity.getGhiChu());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Tạo giá vé thất bại, không có dòng nào được thêm vào.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setMaGiaVe(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Tạo giá vé thất bại, không lấy được ID.");
                }
            }
        }
        
        return entity;
    }

    @Override
    public GiaVe update(GiaVe entity) throws SQLException {
        String query = "UPDATE GiaVe SET loaiGhe = ?, ngayApDung = ?, giaVe = ?, ghiChu = ? WHERE maGiaVe = ?";
        
        try (Connection conn = dbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, entity.getLoaiGhe());
            stmt.setDate(2, Date.valueOf(entity.getNgayApDung()));
            stmt.setBigDecimal(3, entity.getGiaVe());
            stmt.setString(4, entity.getGhiChu());
            stmt.setInt(5, entity.getMaGiaVe());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Cập nhật giá vé thất bại, không tìm thấy giá vé với ID: " + entity.getMaGiaVe());
            }
        }
        
        return entity;
    }

    @Override
    public void delete(int id) throws SQLException {
        String query = "DELETE FROM GiaVe WHERE maGiaVe = ?";
        
        try (Connection conn = dbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public GiaVe findCurrentByLoaiGhe(String loaiGhe) throws SQLException {
        String query = "SELECT * FROM GiaVe WHERE loaiGhe = ? ORDER BY ngayApDung DESC LIMIT 1";
        
        try (Connection conn = dbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, loaiGhe);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractFromResultSet(rs);
                }
            }
        }
        
        return null;
    }

    @Override
    public List<GiaVe> findByDate(LocalDate date) throws SQLException {
        List<GiaVe> giaVeList = new ArrayList<>();
        String query = "SELECT * FROM GiaVe WHERE ngayApDung <= ? ORDER BY ngayApDung DESC";
        
        try (Connection conn = dbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(date));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    giaVeList.add(extractFromResultSet(rs));
                }
            }
        }
        
        return giaVeList;
    }
    
    private GiaVe extractFromResultSet(ResultSet rs) throws SQLException {
        int maGiaVe = rs.getInt("maGiaVe");
        String loaiGhe = rs.getString("loaiGhe");
        LocalDate ngayApDung = rs.getDate("ngayApDung").toLocalDate();
        BigDecimal giaVe = rs.getBigDecimal("giaVe");
        String ghiChu = rs.getString("ghiChu");
        
        return new GiaVe(maGiaVe, loaiGhe, ngayApDung, giaVe, ghiChu);
    }
}