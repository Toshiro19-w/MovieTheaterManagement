package com.cinema.services;

import com.cinema.models.PhienLamViec;
import com.cinema.models.repositories.PhienLamViecRepository;
import com.cinema.utils.DatabaseConnection;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PhienLamViecService {
    private final PhienLamViecRepository repository;
    
    public PhienLamViecService(DatabaseConnection dbConnection) {
        this.repository = new PhienLamViecRepository(dbConnection);
    }
    
    /**
     * Tạo phiên làm việc mới cho nhân viên
     * @param maNhanVien mã nhân viên
     * @return phiên làm việc đã tạo, null nếu thất bại
     */
    public PhienLamViec createPhienLamViec(int maNhanVien) {
        try {
            // Kiểm tra xem nhân viên đã có phiên làm việc đang hoạt động chưa
            PhienLamViec activePhien = repository.findActiveSessionByNhanVien(maNhanVien);
            if (activePhien != null) {
                return activePhien; // Trả về phiên đang hoạt động nếu có
            }
            
            // Tạo phiên làm việc mới
            PhienLamViec phienLamViec = new PhienLamViec();
            phienLamViec.setMaNhanVien(maNhanVien);
            phienLamViec.setThoiGianBatDau(LocalDateTime.now());
            phienLamViec.setTongDoanhThu(0);
            phienLamViec.setSoVeDaBan(0);
            
            return repository.save(phienLamViec);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Kết thúc phiên làm việc
     * @param maPhien mã phiên
     * @return true nếu kết thúc thành công
     */
    public boolean endPhienLamViec(int maPhien) {
        try {
            repository.endSession(maPhien);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Cập nhật thông tin phiên làm việc
     * @param phienLamViec thông tin phiên làm việc
     * @return true nếu cập nhật thành công
     */
    public boolean updatePhienLamViec(PhienLamViec phienLamViec) {
        try {
            repository.update(phienLamViec);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Lấy phiên làm việc theo mã phiên
     * @param maPhien mã phiên
     * @return phiên làm việc
     */
    public PhienLamViec getPhienLamViecById(int maPhien) {
        try {
            return repository.findById(maPhien);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Lấy danh sách phiên làm việc của nhân viên
     * @param maNhanVien mã nhân viên
     * @return danh sách phiên làm việc
     */
    public List<PhienLamViec> getPhienLamViecByNhanVien(int maNhanVien) {
        try {
            return repository.getRevenueByNhanVien(maNhanVien);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Lấy phiên làm việc đang hoạt động của nhân viên
     * @param maNhanVien mã nhân viên
     * @return phiên làm việc đang hoạt động hoặc null nếu không có
     */
    public PhienLamViec getActivePhienLamViec(int maNhanVien) {
        try {
            return repository.findActiveSessionByNhanVien(maNhanVien);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Lấy danh sách phiên làm việc trong khoảng thời gian
     * @param fromDate từ ngày
     * @param toDate đến ngày
     * @return danh sách phiên làm việc
     */
    public List<PhienLamViec> getPhienLamViecByDateRange(LocalDateTime fromDate, LocalDateTime toDate) {
        try {
            return repository.findByTimeRange(fromDate, toDate);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Lấy tất cả phiên làm việc
     * @return danh sách tất cả phiên làm việc
     */
    public List<PhienLamViec> getAllPhienLamViec() {
        try {
            return repository.findAll();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Cập nhật phiên làm việc sau khi bán vé
     * @param maPhien mã phiên
     * @param giaVe giá vé
     * @return true nếu cập nhật thành công
     */
    public boolean updateAfterSale(int maPhien, double giaVe) {
        try {
            repository.updateAfterSale(maPhien, giaVe);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}