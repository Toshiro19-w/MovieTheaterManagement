package com.cinema.models.repositories;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.cinema.models.PhienLamViec;
import com.cinema.models.repositories.Interface.IPhienLamViecRepository;
import com.cinema.utils.DatabaseConnection;

public class PhienLamViecRepository extends BaseRepository<PhienLamViec> implements IPhienLamViecRepository {

    public PhienLamViecRepository(DatabaseConnection dbConnection) {
        super(dbConnection);
    }

    @Override
    public List<PhienLamViec> findAll() throws SQLException {
        List<PhienLamViec> phienLamViecs = new ArrayList<>();
        String query = "SELECT * FROM PhienLamViec";
        
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                phienLamViecs.add(extractFromResultSet(rs));
            }
        }
        
        return phienLamViecs;
    }

    public PhienLamViec findById(int id) throws SQLException {
        String query = "SELECT * FROM PhienLamViec WHERE maPhien = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
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
    public PhienLamViec save(PhienLamViec entity) throws SQLException {
        String query = "INSERT INTO PhienLamViec (maNhanVien, thoiGianBatDau, thoiGianKetThuc, tongDoanhThu, soVeDaBan) " +
                      "VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, entity.getMaNhanVien());
            stmt.setTimestamp(2, Timestamp.valueOf(entity.getThoiGianBatDau()));
            
            if (entity.getThoiGianKetThuc() != null) {
                stmt.setTimestamp(3, Timestamp.valueOf(entity.getThoiGianKetThuc()));
            } else {
                stmt.setNull(3, Types.TIMESTAMP);
            }
            
            stmt.setDouble(4, entity.getTongDoanhThu());
            stmt.setInt(5, entity.getSoVeDaBan());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Tạo phiên làm việc thất bại, không có dòng nào được thêm vào.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setMaPhien(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Tạo phiên làm việc thất bại, không lấy được ID.");
                }
            }
        }
        
        return entity;
    }

    @Override
    public PhienLamViec update(PhienLamViec entity) throws SQLException {
        String query = "UPDATE PhienLamViec SET maNhanVien = ?, thoiGianBatDau = ?, thoiGianKetThuc = ?, " +
                      "tongDoanhThu = ?, soVeDaBan = ? WHERE maPhien = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, entity.getMaNhanVien());
            stmt.setTimestamp(2, Timestamp.valueOf(entity.getThoiGianBatDau()));
            
            if (entity.getThoiGianKetThuc() != null) {
                stmt.setTimestamp(3, Timestamp.valueOf(entity.getThoiGianKetThuc()));
            } else {
                stmt.setNull(3, Types.TIMESTAMP);
            }
            
            stmt.setDouble(4, entity.getTongDoanhThu());
            stmt.setInt(5, entity.getSoVeDaBan());
            stmt.setInt(6, entity.getMaPhien());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Cập nhật phiên làm việc thất bại, không tìm thấy phiên với ID: " + entity.getMaPhien());
            }
        }
        
        return entity;
    }

    @Override
    public void delete(int id) throws SQLException {
        String query = "DELETE FROM PhienLamViec WHERE maPhien = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public PhienLamViec findActiveSessionByNhanVien(int maNhanVien) throws SQLException {
        String query = "SELECT * FROM PhienLamViec WHERE maNhanVien = ? AND thoiGianKetThuc IS NULL";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, maNhanVien);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractFromResultSet(rs);
                }
            }
        }
        
        return null;
    }

    @Override
    public List<PhienLamViec> findByTimeRange(LocalDateTime tuNgay, LocalDateTime denNgay) throws SQLException {
        List<PhienLamViec> phienLamViecs = new ArrayList<>();
        String query = "SELECT * FROM PhienLamViec WHERE thoiGianBatDau BETWEEN ? AND ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setTimestamp(1, Timestamp.valueOf(tuNgay));
            stmt.setTimestamp(2, Timestamp.valueOf(denNgay));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    phienLamViecs.add(extractFromResultSet(rs));
                }
            }
        }
        
        return phienLamViecs;
    }

    @Override
    public void updateAfterSale(int maPhien, double giaVe) throws SQLException {
        String query = "UPDATE PhienLamViec SET tongDoanhThu = tongDoanhThu + ?, soVeDaBan = soVeDaBan + 1 WHERE maPhien = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDouble(1, giaVe);
            stmt.setInt(2, maPhien);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Cập nhật phiên làm việc sau khi bán vé thất bại, không tìm thấy phiên với ID: " + maPhien);
            }
        }
    }

    @Override
    public void endSession(int maPhien) throws SQLException {
        String query = "UPDATE PhienLamViec SET thoiGianKetThuc = ? WHERE maPhien = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, maPhien);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Kết thúc phiên làm việc thất bại, không tìm thấy phiên với ID: " + maPhien);
            }
        }
    }

    @Override
    public List<PhienLamViec> getRevenueByNhanVien(int maNhanVien) throws SQLException {
        List<PhienLamViec> phienLamViecs = new ArrayList<>();
        String query = "SELECT * FROM PhienLamViec WHERE maNhanVien = ? ORDER BY thoiGianBatDau DESC";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, maNhanVien);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    phienLamViecs.add(extractFromResultSet(rs));
                }
            }
        }
        
        return phienLamViecs;
    }
    
    private PhienLamViec extractFromResultSet(ResultSet rs) throws SQLException {
        PhienLamViec phienLamViec = new PhienLamViec();
        
        phienLamViec.setMaPhien(rs.getInt("maPhien"));
        phienLamViec.setMaNhanVien(rs.getInt("maNhanVien"));
        phienLamViec.setThoiGianBatDau(rs.getTimestamp("thoiGianBatDau").toLocalDateTime());
        
        Timestamp thoiGianKetThuc = rs.getTimestamp("thoiGianKetThuc");
        if (thoiGianKetThuc != null) {
            phienLamViec.setThoiGianKetThuc(thoiGianKetThuc.toLocalDateTime());
        }
        
        phienLamViec.setTongDoanhThu(rs.getDouble("tongDoanhThu"));
        phienLamViec.setSoVeDaBan(rs.getInt("soVeDaBan"));
        
        return phienLamViec;
    }
}