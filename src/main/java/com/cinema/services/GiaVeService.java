package com.cinema.services;

import com.cinema.models.GiaVe;
import com.cinema.models.repositories.GiaVeRepository;
import com.cinema.utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class GiaVeService {
    private final GiaVeRepository giaVeRepo;
    private final LichSuGiaVeService lichSuGiaVeService;
    protected Connection conn;
    protected DatabaseConnection dbConnection;

    public GiaVeService(DatabaseConnection dbConnection) throws SQLException {
        if (dbConnection == null) {
            throw new IllegalArgumentException("DatabaseConnection cannot be null");
        }
        this.dbConnection = dbConnection;
        try {
            this.conn = dbConnection.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Không thể lấy kết nối cơ sở dữ liệu", e);
        }
        this.giaVeRepo = new GiaVeRepository(dbConnection);
        this.lichSuGiaVeService = new LichSuGiaVeService(dbConnection);
    }

    // Lấy tất cả giá vé
    public List<GiaVe> getAllGiaVe() throws SQLException {
        return giaVeRepo.findAll();
    }

    // Lấy giá vé theo ID
    public GiaVe getGiaVeById(int maGiaVe) throws SQLException {
        return giaVeRepo.findById(maGiaVe);
    }

    // Lấy giá vé hiện tại theo loại ghế
    public GiaVe getCurrentGiaVeByLoaiGhe(String loaiGhe) throws SQLException {
        return giaVeRepo.findCurrentByLoaiGhe(loaiGhe);
    }

    // Thêm giá vé mới và lưu lịch sử
    public GiaVe addGiaVe(GiaVe giaVe, int maNhanVien) throws SQLException {
        // Lấy giá vé hiện tại để lưu lịch sử
        GiaVe currentGiaVe = giaVeRepo.findCurrentByLoaiGhe(giaVe.getLoaiGhe());
        
        // Thêm giá vé mới
        GiaVe newGiaVe = giaVeRepo.save(giaVe);
        
        // Lưu lịch sử thay đổi giá vé
        if (currentGiaVe != null) {
            lichSuGiaVeService.saveGiaVeChange(
                giaVe.getLoaiGhe(),
                currentGiaVe.getGiaVe().doubleValue(),
                giaVe.getGiaVe().doubleValue(),
                maNhanVien
            );
        }
        
        return newGiaVe;
    }

    // Cập nhật giá vé và lưu lịch sử
    public GiaVe updateGiaVe(GiaVe giaVe, int maNhanVien) throws SQLException {
        // Lấy giá vé cũ để lưu lịch sử
        GiaVe oldGiaVe = giaVeRepo.findById(giaVe.getMaGiaVe());
        if (oldGiaVe == null) {
            throw new SQLException("Không tìm thấy giá vé với mã: " + giaVe.getMaGiaVe());
        }
        
        // Cập nhật giá vé
        GiaVe updatedGiaVe = giaVeRepo.update(giaVe);
        
        // Lưu lịch sử thay đổi giá vé
        lichSuGiaVeService.saveGiaVeChange(
            giaVe.getLoaiGhe(),
            oldGiaVe.getGiaVe().doubleValue(),
            giaVe.getGiaVe().doubleValue(),
            maNhanVien
        );
        
        return updatedGiaVe;
    }

    // Xóa giá vé
    public boolean deleteGiaVe(int maGiaVe) throws SQLException {
        giaVeRepo.delete(maGiaVe);
        return true;
    }
    
    // Áp dụng giá vé mới
    public GiaVe applyNewPrice(String loaiGhe, BigDecimal giaVeMoi, LocalDate ngayApDung, String ghiChu, int maNhanVien) throws SQLException {
        // Lấy giá vé hiện tại
        GiaVe currentGiaVe = giaVeRepo.findCurrentByLoaiGhe(loaiGhe);
        
        // Tạo giá vé mới
        GiaVe newGiaVe = new GiaVe(0, loaiGhe, ngayApDung, giaVeMoi, ghiChu);
        
        // Lưu giá vé mới
        GiaVe savedGiaVe = giaVeRepo.save(newGiaVe);
        
        // Lưu lịch sử thay đổi giá vé
        if (currentGiaVe != null) {
            lichSuGiaVeService.saveGiaVeChange(
                loaiGhe,
                currentGiaVe.getGiaVe().doubleValue(),
                giaVeMoi.doubleValue(),
                maNhanVien
            );
        }
        
        return savedGiaVe;
    }
}