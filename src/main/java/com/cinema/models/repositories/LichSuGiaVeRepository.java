package com.cinema.models.repositories;

import com.cinema.models.LichSuGiaVe;
import com.cinema.models.repositories.Interface.ILichSuGiaVeRepository;
import com.cinema.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LichSuGiaVeRepository extends BaseRepository<LichSuGiaVe> implements ILichSuGiaVeRepository {

    public LichSuGiaVeRepository(DatabaseConnection dbConnection) {
        super(dbConnection);
    }

    @Override
    public List<LichSuGiaVe> findAll() throws SQLException {
        List<LichSuGiaVe> lichSuList = new ArrayList<>();
        String query = "SELECT * FROM LichSuGiaVe ORDER BY ngayThayDoi DESC";
        
        try (Connection conn = dbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                lichSuList.add(extractFromResultSet(rs));
            }
        }
        
        return lichSuList;
    }

    @Override
    public LichSuGiaVe findById(int id) throws SQLException {
        String query = "SELECT * FROM LichSuGiaVe WHERE maLichSu = ?";
        
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
    public LichSuGiaVe save(LichSuGiaVe entity) throws SQLException {
        String query = "INSERT INTO LichSuGiaVe (loaiGhe, giaVeCu, giaVeMoi, ngayThayDoi, nguoiThayDoi) " +
                      "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, entity.getLoaiGhe());
            stmt.setDouble(2, entity.getGiaVeCu());
            stmt.setDouble(3, entity.getGiaVeMoi());
            stmt.setTimestamp(4, Timestamp.valueOf(entity.getNgayThayDoi()));
            
            if (entity.getNguoiThayDoi() != null) {
                stmt.setInt(5, entity.getNguoiThayDoi());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Tạo lịch sử giá vé thất bại, không có dòng nào được thêm vào.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setMaLichSu(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Tạo lịch sử giá vé thất bại, không lấy được ID.");
                }
            }
        }
        
        return entity;
    }

    @Override
    public LichSuGiaVe update(LichSuGiaVe entity) throws SQLException {
        String query = "UPDATE LichSuGiaVe SET loaiGhe = ?, giaVeCu = ?, giaVeMoi = ?, " +
                      "ngayThayDoi = ?, nguoiThayDoi = ? WHERE maLichSu = ?";
        
        try (Connection conn = dbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, entity.getLoaiGhe());
            stmt.setDouble(2, entity.getGiaVeCu());
            stmt.setDouble(3, entity.getGiaVeMoi());
            stmt.setTimestamp(4, Timestamp.valueOf(entity.getNgayThayDoi()));
            
            if (entity.getNguoiThayDoi() != null) {
                stmt.setInt(5, entity.getNguoiThayDoi());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            
            stmt.setInt(6, entity.getMaLichSu());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Cập nhật lịch sử giá vé thất bại, không tìm thấy bản ghi với ID: " + entity.getMaLichSu());
            }
        }
        
        return entity;
    }

    @Override
    public void delete(int id) throws SQLException {
        String query = "DELETE FROM LichSuGiaVe WHERE maLichSu = ?";
        
        try (Connection conn = dbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<LichSuGiaVe> findByLoaiGhe(String loaiGhe) throws SQLException {
        List<LichSuGiaVe> lichSuList = new ArrayList<>();
        String query = "SELECT * FROM LichSuGiaVe WHERE loaiGhe = ? ORDER BY ngayThayDoi DESC";
        
        try (Connection conn = dbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, loaiGhe);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lichSuList.add(extractFromResultSet(rs));
                }
            }
        }
        
        return lichSuList;
    }

    @Override
    public List<LichSuGiaVe> findByTimeRange(LocalDateTime tuNgay, LocalDateTime denNgay) throws SQLException {
        List<LichSuGiaVe> lichSuList = new ArrayList<>();
        String query = "SELECT * FROM LichSuGiaVe WHERE ngayThayDoi BETWEEN ? AND ? ORDER BY ngayThayDoi DESC";
        
        try (Connection conn = dbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setTimestamp(1, Timestamp.valueOf(tuNgay));
            stmt.setTimestamp(2, Timestamp.valueOf(denNgay));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lichSuList.add(extractFromResultSet(rs));
                }
            }
        }
        
        return lichSuList;
    }

    @Override
    public List<LichSuGiaVe> findByNguoiThayDoi(int maNhanVien) throws SQLException {
        List<LichSuGiaVe> lichSuList = new ArrayList<>();
        String query = "SELECT * FROM LichSuGiaVe WHERE nguoiThayDoi = ? ORDER BY ngayThayDoi DESC";
        
        try (Connection conn = dbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, maNhanVien);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lichSuList.add(extractFromResultSet(rs));
                }
            }
        }
        
        return lichSuList;
    }

    @Override
    public LichSuGiaVe saveGiaVeChange(String loaiGhe, double giaVeCu, double giaVeMoi, int nguoiThayDoi) throws SQLException {
        LichSuGiaVe lichSu = new LichSuGiaVe(loaiGhe, giaVeCu, giaVeMoi, LocalDateTime.now(), nguoiThayDoi);
        return save(lichSu);
    }
    
    private LichSuGiaVe extractFromResultSet(ResultSet rs) throws SQLException {
        LichSuGiaVe lichSu = new LichSuGiaVe();
        
        lichSu.setMaLichSu(rs.getInt("maLichSu"));
        lichSu.setLoaiGhe(rs.getString("loaiGhe"));
        lichSu.setGiaVeCu(rs.getDouble("giaVeCu"));
        lichSu.setGiaVeMoi(rs.getDouble("giaVeMoi"));
        lichSu.setNgayThayDoi(rs.getTimestamp("ngayThayDoi").toLocalDateTime());
        
        int nguoiThayDoi = rs.getInt("nguoiThayDoi");
        if (!rs.wasNull()) {
            lichSu.setNguoiThayDoi(nguoiThayDoi);
        }
        
        return lichSu;
    }
}