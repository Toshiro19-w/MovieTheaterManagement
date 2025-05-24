package com.cinema.models.repositories.Interface;

import com.cinema.models.GiaVe;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface IGiaVeRepository extends IRepository<GiaVe> {
    
    // Tìm giá vé hiện tại theo loại ghế
    GiaVe findCurrentByLoaiGhe(String loaiGhe) throws SQLException;
    
    // Tìm giá vé theo ngày
    List<GiaVe> findByDate(LocalDate date) throws SQLException;
}