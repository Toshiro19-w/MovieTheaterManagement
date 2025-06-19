package com.cinema.models;

import java.time.LocalDateTime;

public class UserSession {
    private String maPhien;
    private int maNguoiDung;
    private LocalDateTime thoiGianBatDau;
    private LocalDateTime thoiGianHoatDongCuoi;
    private String trangThai;
    private String thongTinThietBi;

    public UserSession() {
    }

    public UserSession(String maPhien, int maNguoiDung, LocalDateTime thoiGianBatDau, 
                       LocalDateTime thoiGianHoatDongCuoi, String trangThai, String thongTinThietBi) {
        this.maPhien = maPhien;
        this.maNguoiDung = maNguoiDung;
        this.thoiGianBatDau = thoiGianBatDau;
        this.thoiGianHoatDongCuoi = thoiGianHoatDongCuoi;
        this.trangThai = trangThai;
        this.thongTinThietBi = thongTinThietBi;
    }

    public String getMaPhien() {
        return maPhien;
    }

    public void setMaPhien(String maPhien) {
        this.maPhien = maPhien;
    }

    public int getMaNguoiDung() {
        return maNguoiDung;
    }

    public void setMaNguoiDung(int maNguoiDung) {
        this.maNguoiDung = maNguoiDung;
    }

    public LocalDateTime getThoiGianBatDau() {
        return thoiGianBatDau;
    }

    public void setThoiGianBatDau(LocalDateTime thoiGianBatDau) {
        this.thoiGianBatDau = thoiGianBatDau;
    }

    public LocalDateTime getThoiGianHoatDongCuoi() {
        return thoiGianHoatDongCuoi;
    }

    public void setThoiGianHoatDongCuoi(LocalDateTime thoiGianHoatDongCuoi) {
        this.thoiGianHoatDongCuoi = thoiGianHoatDongCuoi;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getThongTinThietBi() {
        return thongTinThietBi;
    }

    public void setThongTinThietBi(String thongTinThietBi) {
        this.thongTinThietBi = thongTinThietBi;
    }

    @Override
    public String toString() {
        return "UserSession{" +
                "maPhien='" + maPhien + '\'' +
                ", maNguoiDung=" + maNguoiDung +
                ", thoiGianBatDau=" + thoiGianBatDau +
                ", thoiGianHoatDongCuoi=" + thoiGianHoatDongCuoi +
                ", trangThai='" + trangThai + '\'' +
                ", thongTinThietBi='" + thongTinThietBi + '\'' +
                '}';
    }
}