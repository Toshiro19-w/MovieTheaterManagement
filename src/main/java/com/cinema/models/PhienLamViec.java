package com.cinema.models;

import java.time.LocalDateTime;

public class PhienLamViec {
    private int maPhien;
    private int maNhanVien;
    private LocalDateTime thoiGianBatDau;
    private LocalDateTime thoiGianKetThuc;
    private double tongDoanhThu;
    private int soVeDaBan;
    
    // Constructor đầy đủ
    public PhienLamViec(int maPhien, int maNhanVien, LocalDateTime thoiGianBatDau, 
                        LocalDateTime thoiGianKetThuc, double tongDoanhThu, int soVeDaBan) {
        this.maPhien = maPhien;
        this.maNhanVien = maNhanVien;
        this.thoiGianBatDau = thoiGianBatDau;
        this.thoiGianKetThuc = thoiGianKetThuc;
        this.tongDoanhThu = tongDoanhThu;
        this.soVeDaBan = soVeDaBan;
    }
    
    // Constructor không có maPhien (cho việc tạo mới)
    public PhienLamViec(int maNhanVien, LocalDateTime thoiGianBatDau) {
        this.maNhanVien = maNhanVien;
        this.thoiGianBatDau = thoiGianBatDau;
        this.tongDoanhThu = 0;
        this.soVeDaBan = 0;
    }
    
    // Constructor mặc định
    public PhienLamViec() {
    }

    // Getters và Setters
    public int getMaPhien() {
        return maPhien;
    }

    public void setMaPhien(int maPhien) {
        this.maPhien = maPhien;
    }

    public int getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(int maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    public LocalDateTime getThoiGianBatDau() {
        return thoiGianBatDau;
    }

    public void setThoiGianBatDau(LocalDateTime thoiGianBatDau) {
        this.thoiGianBatDau = thoiGianBatDau;
    }

    public LocalDateTime getThoiGianKetThuc() {
        return thoiGianKetThuc;
    }

    public void setThoiGianKetThuc(LocalDateTime thoiGianKetThuc) {
        this.thoiGianKetThuc = thoiGianKetThuc;
    }

    public double getTongDoanhThu() {
        return tongDoanhThu;
    }

    public void setTongDoanhThu(double tongDoanhThu) {
        this.tongDoanhThu = tongDoanhThu;
    }

    public int getSoVeDaBan() {
        return soVeDaBan;
    }

    public void setSoVeDaBan(int soVeDaBan) {
        this.soVeDaBan = soVeDaBan;
    }
    
    // Phương thức cập nhật khi bán vé
    public void capNhatBanVe(double giaVe) {
        this.tongDoanhThu += giaVe;
        this.soVeDaBan++;
    }
    
    // Phương thức kết thúc phiên làm việc
    public void ketThucPhien() {
        this.thoiGianKetThuc = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "PhienLamViec{" +
                "maPhien=" + maPhien +
                ", maNhanVien=" + maNhanVien +
                ", thoiGianBatDau=" + thoiGianBatDau +
                ", thoiGianKetThuc=" + thoiGianKetThuc +
                ", tongDoanhThu=" + tongDoanhThu +
                ", soVeDaBan=" + soVeDaBan +
                '}';
    }
}