package com.cinema.models.repositories.Interface;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import com.cinema.models.LichSuGiaVe;

public interface ILichSuGiaVeRepository {
    
    // Tìm lịch sử giá vé theo loại ghế
    List<LichSuGiaVe> findByLoaiGhe(String loaiGhe) throws SQLException;
    
    // Tìm lịch sử giá vé theo khoảng thời gian
    List<LichSuGiaVe> findByTimeRange(LocalDateTime tuNgay, LocalDateTime denNgay) throws SQLException;
    
    // Tìm lịch sử giá vé theo người thay đổi
    List<LichSuGiaVe> findByNguoiThayDoi(int maNhanVien) throws SQLException;
    
    // Lưu lịch sử khi thay đổi giá vé
    LichSuGiaVe saveGiaVeChange(String loaiGhe, double giaVeCu, double giaVeMoi, int nguoiThayDoi) throws SQLException;
}