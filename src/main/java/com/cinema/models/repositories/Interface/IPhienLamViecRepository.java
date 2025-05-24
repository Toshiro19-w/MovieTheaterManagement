package com.cinema.models.repositories.Interface;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import com.cinema.models.PhienLamViec;

public interface IPhienLamViecRepository {
    
    // Tìm phiên làm việc đang hoạt động của nhân viên
    PhienLamViec findActiveSessionByNhanVien(int maNhanVien) throws SQLException;
    
    // Lấy danh sách phiên làm việc theo khoảng thời gian
    List<PhienLamViec> findByTimeRange(LocalDateTime tuNgay, LocalDateTime denNgay) throws SQLException;
    
    // Cập nhật thông tin khi bán vé
    void updateAfterSale(int maPhien, double giaVe) throws SQLException;
    
    // Kết thúc phiên làm việc
    void endSession(int maPhien) throws SQLException;
    
    // Thống kê doanh thu theo nhân viên
    List<PhienLamViec> getRevenueByNhanVien(int maNhanVien) throws SQLException;
}