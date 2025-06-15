package com.cinema.models;

import java.util.Date;

public class ActivityLog {
    private int maLog;
    private String loaiHoatDong;
    private String moTa;
    private Date thoiGian;
    private int maNguoiDung;
    private String tenNguoiDung;
    
    public ActivityLog() {
    }
    
    public ActivityLog(int maLog, String loaiHoatDong, String moTa, Date thoiGian, int maNguoiDung) {
        this.maLog = maLog;
        this.loaiHoatDong = loaiHoatDong;
        this.moTa = moTa;
        this.thoiGian = thoiGian;
        this.maNguoiDung = maNguoiDung;
    }
    
    public int getMaLog() {
        return maLog;
    }
    
    public void setMaLog(int maLog) {
        this.maLog = maLog;
    }
    
    public String getLoaiHoatDong() {
        return loaiHoatDong;
    }
    
    public void setLoaiHoatDong(String loaiHoatDong) {
        this.loaiHoatDong = loaiHoatDong;
    }
    
    public String getMoTa() {
        return moTa;
    }
    
    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }
    
    public Date getThoiGian() {
        return thoiGian;
    }
    
    public void setThoiGian(Date thoiGian) {
        this.thoiGian = thoiGian;
    }
    
    public int getMaNguoiDung() {
        return maNguoiDung;
    }
    
    public void setMaNguoiDung(int maNguoiDung) {
        this.maNguoiDung = maNguoiDung;
    }
    
    public String getTenNguoiDung() {
        return tenNguoiDung;
    }
    
    public void setTenNguoiDung(String tenNguoiDung) {
        this.tenNguoiDung = tenNguoiDung;
    }
}