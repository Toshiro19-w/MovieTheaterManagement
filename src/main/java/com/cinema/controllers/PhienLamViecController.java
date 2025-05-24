package com.cinema.controllers;

import com.cinema.models.PhienLamViec;
import com.cinema.models.NhanVien;
import com.cinema.services.PhienLamViecService;
import com.cinema.utils.DatabaseConnection;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class PhienLamViecController {
    private final PhienLamViecService phienLamViecService;
    
    public PhienLamViecController(DatabaseConnection dbConnection) throws SQLException {
        this.phienLamViecService = new PhienLamViecService(dbConnection);
    }
    
    public List<PhienLamViec> getAllPhienLamViec() throws SQLException {
        return phienLamViecService.getAllPhienLamViec();
    }
    
    public PhienLamViec getPhienLamViecById(int maPhien) throws SQLException {
        return phienLamViecService.getPhienLamViecById(maPhien);
    }
    
    public PhienLamViec createPhienLamViec(int maNhanVien) throws SQLException {
        return phienLamViecService.createPhienLamViec(maNhanVien);
    }
    
    public void endPhienLamViec(int maPhien) throws SQLException {
        phienLamViecService.endPhienLamViec(maPhien);
    }
    
    public void updateAfterSale(int maPhien, double giaVe) throws SQLException {
        phienLamViecService.updateAfterSale(maPhien, giaVe);
    }
    
    public PhienLamViec getActiveSessionByNhanVien(int maNhanVien) throws SQLException {
        return phienLamViecService.getActiveSessionByNhanVien(maNhanVien);
    }
    
    public List<PhienLamViec> getPhienLamViecByTimeRange(LocalDateTime tuNgay, LocalDateTime denNgay) throws SQLException {
        return phienLamViecService.getPhienLamViecByTimeRange(tuNgay, denNgay);
    }
    
    public List<PhienLamViec> getRevenueByNhanVien(int maNhanVien) throws SQLException {
        return phienLamViecService.getRevenueByNhanVien(maNhanVien);
    }
}