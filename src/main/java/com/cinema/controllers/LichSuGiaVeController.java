package com.cinema.controllers;

import com.cinema.models.LichSuGiaVe;
import com.cinema.services.LichSuGiaVeService;
import com.cinema.utils.DatabaseConnection;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class LichSuGiaVeController {
    private final LichSuGiaVeService lichSuGiaVeService;
    
    public LichSuGiaVeController(DatabaseConnection dbConnection) throws SQLException {
        this.lichSuGiaVeService = new LichSuGiaVeService(dbConnection);
    }
    
    public List<LichSuGiaVe> getAllLichSuGiaVe() throws SQLException {
        return lichSuGiaVeService.getAllLichSuGiaVe();
    }
    
    public LichSuGiaVe getLichSuGiaVeById(int maLichSu) throws SQLException {
        return lichSuGiaVeService.getLichSuGiaVeById(maLichSu);
    }
    
    public List<LichSuGiaVe> getLichSuGiaVeByLoaiGhe(String loaiGhe) throws SQLException {
        return lichSuGiaVeService.getLichSuGiaVeByLoaiGhe(loaiGhe);
    }
    
    public List<LichSuGiaVe> getLichSuGiaVeByTimeRange(LocalDateTime tuNgay, LocalDateTime denNgay) throws SQLException {
        return lichSuGiaVeService.getLichSuGiaVeByTimeRange(tuNgay, denNgay);
    }
    
    public List<LichSuGiaVe> getLichSuGiaVeByNguoiThayDoi(int maNhanVien) throws SQLException {
        return lichSuGiaVeService.getLichSuGiaVeByNguoiThayDoi(maNhanVien);
    }
    
    public LichSuGiaVe saveGiaVeChange(String loaiGhe, double giaVeCu, double giaVeMoi, int nguoiThayDoi) throws SQLException {
        return lichSuGiaVeService.saveGiaVeChange(loaiGhe, giaVeCu, giaVeMoi, nguoiThayDoi);
    }
}