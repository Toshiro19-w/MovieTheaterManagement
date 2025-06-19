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
        String query = "SELECT * FROM GiaVe ORDER BY ngayApDung DESC, loaiGhe";
        
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
        String query = "INSERT INTO GiaVe (loaiGhe, ngayApDung, ngayKetThuc, giaVe, ghiChu) VALUES (?, ?, ?, ?, ?)";
        
        // Cập nhật ngày kết thúc cho giá vé cũ
        updatePreviousPriceEndDate(entity.getLoaiGhe(), entity.getNgayApDung());
        
        try (Connection conn = dbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, entity.getLoaiGhe());
            stmt.setDate(2, Date.valueOf(entity.getNgayApDung()));
            
            // Xử lý ngayKetThuc có thể null
            if (entity.getNgayKetThuc() != null) {
                stmt.setDate(3, Date.valueOf(entity.getNgayKetThuc()));
            } else {
                stmt.setNull(3, java.sql.Types.DATE);
            }
            
            stmt.setBigDecimal(4, entity.getGiaVe());
            stmt.setString(5, entity.getGhiChu());
            
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
    
    private void updatePreviousPriceEndDate(String loaiGhe, LocalDate ngayApDung) throws SQLException {
        String query = "UPDATE GiaVe SET ngayKetThuc = ? WHERE loaiGhe = ? AND (ngayKetThuc IS NULL OR ngayKetThuc > ?) AND ngayApDung < ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            // Ngày kết thúc của giá vé cũ là ngày trước ngày áp dụng của giá vé mới
            LocalDate ngayKetThucCu = ngayApDung.minusDays(1);
            stmt.setDate(1, Date.valueOf(ngayKetThucCu));
            stmt.setString(2, loaiGhe);
            stmt.setDate(3, Date.valueOf(ngayApDung));
            stmt.setDate(4, Date.valueOf(ngayApDung));
            
            stmt.executeUpdate();
        }
    }

    @Override
    public GiaVe update(GiaVe entity) throws SQLException {
        String query = "UPDATE GiaVe SET loaiGhe = ?, ngayApDung = ?, ngayKetThuc = ?, giaVe = ?, ghiChu = ? WHERE maGiaVe = ?";
        
        try (Connection conn = dbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, entity.getLoaiGhe());
            stmt.setDate(2, Date.valueOf(entity.getNgayApDung()));
            
            // Xử lý ngayKetThuc có thể null
            if (entity.getNgayKetThuc() != null) {
                stmt.setDate(3, Date.valueOf(entity.getNgayKetThuc()));
            } else {
                stmt.setNull(3, java.sql.Types.DATE);
            }
            
            stmt.setBigDecimal(4, entity.getGiaVe());
            stmt.setString(5, entity.getGhiChu());
            stmt.setInt(6, entity.getMaGiaVe());
            
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
        // Tìm giá vé hiện tại dựa trên ngày hiện tại
        LocalDate today = LocalDate.now();
        return findActiveByLoaiGheAndDate(loaiGhe, today);
    }
    
    @Override
    public GiaVe findActiveByLoaiGheAndDate(String loaiGhe, LocalDate date) throws SQLException {
        String query = "SELECT * FROM GiaVe WHERE loaiGhe = ? AND ngayApDung <= ? AND (ngayKetThuc IS NULL OR ngayKetThuc >= ?) ORDER BY ngayApDung DESC LIMIT 1";
        
        try (Connection conn = dbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, loaiGhe);
            stmt.setDate(2, Date.valueOf(date));
            stmt.setDate(3, Date.valueOf(date));
            
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
        String query = "SELECT * FROM GiaVe WHERE ngayApDung <= ? AND (ngayKetThuc IS NULL OR ngayKetThuc >= ?) ORDER BY loaiGhe, ngayApDung DESC";
        
        try (Connection conn = dbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(date));
            stmt.setDate(2, Date.valueOf(date));
            
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
        
        // Xử lý ngayKetThuc có thể null
        LocalDate ngayKetThuc = null;
        Date sqlNgayKetThuc = rs.getDate("ngayKetThuc");
        if (sqlNgayKetThuc != null) {
            ngayKetThuc = sqlNgayKetThuc.toLocalDate();
        }
        
        BigDecimal giaVe = rs.getBigDecimal("giaVe");
        String ghiChu = rs.getString("ghiChu");
        
        return new GiaVe(maGiaVe, loaiGhe, ngayApDung, ngayKetThuc, giaVe, ghiChu);
    }
}