package com.cinema.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class HoaDon {
    private int maHoaDon;
    private int maNhanVien;
    private int maKhachHang;
    private LocalDateTime ngayLap;
    private String tenNhanVien;
    private String tenKhachHang;
    private BigDecimal tongTien;

    public HoaDon(int maHoaDon, int maNhanVien, int maKhachHang, LocalDateTime ngayLap) {
        this.maHoaDon = maHoaDon;
        this.maNhanVien = maNhanVien;
        this.maKhachHang = maKhachHang;
        this.ngayLap = ngayLap;
    }

    public HoaDon() {}

    public int getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(int maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public int getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(int maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    public int getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(int maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public LocalDateTime getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(LocalDateTime ngayLap) {
        this.ngayLap = ngayLap;
    }

    public String getTenNhanVien() { 
        return tenNhanVien; 
    }

    public void setTenNhanVien(String tenNhanVien) { 
        this.tenNhanVien = tenNhanVien; 
    }

    public String getTenKhachHang() { 
        return tenKhachHang; 
    }

    public void setTenKhachHang(String tenKhachHang) { 
        this.tenKhachHang = tenKhachHang; 
    }

    public BigDecimal getTongTien() {
        return tongTien;
    }

    public void setTongTien(BigDecimal tongTien) {
        this.tongTien = tongTien;
    }
}

