package com.cinema.services;

import com.cinema.models.PhienLamViec;
import com.cinema.models.repositories.PhienLamViecRepository;
import com.cinema.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class PhienLamViecService {
    private final PhienLamViecRepository phienLamViecRepo;
    protected Connection conn;
    protected DatabaseConnection dbConnection;

    public PhienLamViecService(DatabaseConnection dbConnection) throws SQLException {
        if (dbConnection == null) {
            throw new IllegalArgumentException("DatabaseConnection cannot be null");
        }
        this.dbConnection = dbConnection;
        try {
            this.conn = dbConnection.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Không thể lấy kết nối cơ sở dữ liệu", e);
        }
        this.phienLamViecRepo = new PhienLamViecRepository(dbConnection);
    }

    // Lấy tất cả phiên làm việc
    public List<PhienLamViec> getAllPhienLamViec() throws SQLException {
        return phienLamViecRepo.findAll();
    }

    // Lấy phiên làm việc theo ID
    public PhienLamViec getPhienLamViecById(int maPhien) throws SQLException {
        return phienLamViecRepo.findById(maPhien);
    }

    // Tạo phiên làm việc mới
    public PhienLamViec createPhienLamViec(int maNhanVien) throws SQLException {
        // Kiểm tra xem nhân viên đã có phiên làm việc đang hoạt động chưa
        PhienLamViec activeSession = phienLamViecRepo.findActiveSessionByNhanVien(maNhanVien);
        if (activeSession != null) {
            throw new SQLException("Nhân viên đã có phiên làm việc đang hoạt động!");
        }
        
        // Tạo phiên làm việc mới
        PhienLamViec phienLamViec = new PhienLamViec(maNhanVien, LocalDateTime.now());
        return phienLamViecRepo.save(phienLamViec);
    }

    // Kết thúc phiên làm việc
    public void endPhienLamViec(int maPhien) throws SQLException {
        phienLamViecRepo.endSession(maPhien);
    }

    // Cập nhật thông tin sau khi bán vé
    public void updateAfterSale(int maPhien, double giaVe) throws SQLException {
        phienLamViecRepo.updateAfterSale(maPhien, giaVe);
    }

    // Lấy phiên làm việc đang hoạt động của nhân viên
    public PhienLamViec getActiveSessionByNhanVien(int maNhanVien) throws SQLException {
        return phienLamViecRepo.findActiveSessionByNhanVien(maNhanVien);
    }

    // Lấy danh sách phiên làm việc trong khoảng thời gian
    public List<PhienLamViec> getPhienLamViecByTimeRange(LocalDateTime tuNgay, LocalDateTime denNgay) throws SQLException {
        return phienLamViecRepo.findByTimeRange(tuNgay, denNgay);
    }

    // Thống kê doanh thu theo nhân viên
    public List<PhienLamViec> getRevenueByNhanVien(int maNhanVien) throws SQLException {
        return phienLamViecRepo.getRevenueByNhanVien(maNhanVien);
    }
}