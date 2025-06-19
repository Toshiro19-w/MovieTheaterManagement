package com.cinema.controllers;

import com.cinema.models.PhienLamViec;
import com.cinema.services.PhienLamViecService;
import com.cinema.utils.DatabaseConnection;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PhienLamViecController {
    private final PhienLamViecService phienLamViecService;
    
    public PhienLamViecController(DatabaseConnection dbConnection) {
        this.phienLamViecService = new PhienLamViecService(dbConnection);
    }
    
    public List<PhienLamViec> getAllPhienLamViec() {
        return phienLamViecService.getAllPhienLamViec();
    }
    
    public PhienLamViec getPhienLamViecById(int maPhien) {
        return phienLamViecService.getPhienLamViecById(maPhien);
    }
    
    public PhienLamViec createPhienLamViec(int maNhanVien) {
        return phienLamViecService.createPhienLamViec(maNhanVien);
    }
    
    public boolean endPhienLamViec(int maPhien) {
        return phienLamViecService.endPhienLamViec(maPhien);
    }
    
    public boolean updateAfterSale(int maPhien, double giaVe) {
        return phienLamViecService.updateAfterSale(maPhien, giaVe);
    }
    
    public PhienLamViec getActivePhienLamViec(int maNhanVien) {
        return phienLamViecService.getActivePhienLamViec(maNhanVien);
    }
    
    public List<PhienLamViec> getPhienLamViecByDateRange(LocalDateTime tuNgay, LocalDateTime denNgay) {
        return phienLamViecService.getPhienLamViecByDateRange(tuNgay, denNgay);
    }
    
    public List<PhienLamViec> getPhienLamViecByNhanVien(int maNhanVien) {
        return phienLamViecService.getPhienLamViecByNhanVien(maNhanVien);
    }
    
    public boolean updatePhienLamViec(PhienLamViec phienLamViec) {
        return phienLamViecService.updatePhienLamViec(phienLamViec);
    }
}