package com.cinema.services;

import com.cinema.models.LichSuGiaVe;
import com.cinema.models.repositories.LichSuGiaVeRepository;
import com.cinema.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class LichSuGiaVeService {
    private final LichSuGiaVeRepository lichSuRepo;
    protected Connection conn;
    protected DatabaseConnection dbConnection;

    public LichSuGiaVeService(DatabaseConnection dbConnection) throws SQLException {
        if (dbConnection == null) {
            throw new IllegalArgumentException("DatabaseConnection cannot be null");
        }
        this.dbConnection = dbConnection;
        try {
            this.conn = dbConnection.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Không thể lấy kết nối cơ sở dữ liệu", e);
        }
        this.lichSuRepo = new LichSuGiaVeRepository(dbConnection);
    }

    // Lấy tất cả lịch sử giá vé
    public List<LichSuGiaVe> getAllLichSuGiaVe() throws SQLException {
        return lichSuRepo.findAll();
    }

    // Lấy lịch sử giá vé theo ID
    public LichSuGiaVe getLichSuGiaVeById(int maLichSu) throws SQLException {
        return lichSuRepo.findById(maLichSu);
    }

    // Lấy lịch sử giá vé theo loại ghế
    public List<LichSuGiaVe> getLichSuGiaVeByLoaiGhe(String loaiGhe) throws SQLException {
        return lichSuRepo.findByLoaiGhe(loaiGhe);
    }

    // Lấy lịch sử giá vé theo khoảng thời gian
    public List<LichSuGiaVe> getLichSuGiaVeByTimeRange(LocalDateTime tuNgay, LocalDateTime denNgay) throws SQLException {
        return lichSuRepo.findByTimeRange(tuNgay, denNgay);
    }

    // Lấy lịch sử giá vé theo người thay đổi
    public List<LichSuGiaVe> getLichSuGiaVeByNguoiThayDoi(int maNhanVien) throws SQLException {
        return lichSuRepo.findByNguoiThayDoi(maNhanVien);
    }

    // Lưu lịch sử khi thay đổi giá vé
    public LichSuGiaVe saveGiaVeChange(String loaiGhe, double giaVeCu, double giaVeMoi, int nguoiThayDoi) throws SQLException {
        return lichSuRepo.saveGiaVeChange(loaiGhe, giaVeCu, giaVeMoi, nguoiThayDoi);
    }
    
    // Tạo lịch sử giá vé mới
    public LichSuGiaVe createLichSuGiaVe(LichSuGiaVe lichSu) throws SQLException {
        return lichSuRepo.save(lichSu);
    }
}