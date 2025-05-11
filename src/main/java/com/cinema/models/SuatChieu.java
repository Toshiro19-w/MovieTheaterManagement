package com.cinema.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SuatChieu {
    private int maSuatChieu;
    private int maPhim;
    private int maPhong;
    private LocalDateTime ngayGioChieu;

    // Các trường từ JOIN
    private String tenPhim;
    private String tenPhong;
    private int thoiLuongPhim;
    private String kieuPhim;

    // Constructor cho dữ liệu cơ bản
    public SuatChieu(int maSuatChieu, int maPhim, int maPhong, LocalDateTime ngayGioChieu) {
        this.maSuatChieu = maSuatChieu;
        this.maPhim = maPhim;
        this.maPhong = maPhong;
        this.ngayGioChieu = ngayGioChieu;
    }

    // Constructor cho dữ liệu chi tiết (từ JOIN)
    public SuatChieu(int maSuatChieu, int maPhim, String tenPhim, int maPhong, String tenPhong,
                     LocalDateTime ngayGioChieu, int thoiLuongPhim, String kieuPhim) {
        this.maSuatChieu = maSuatChieu;
        this.maPhim = maPhim;
        this.tenPhim = tenPhim;
        this.maPhong = maPhong;
        this.tenPhong = tenPhong;
        this.ngayGioChieu = ngayGioChieu;
        this.thoiLuongPhim = thoiLuongPhim;
        this.kieuPhim = kieuPhim;
    }

    // Constructor mặc định
    public SuatChieu() {}

    // Getters và Setters
    public int getMaSuatChieu() {
        return maSuatChieu;
    }

    public void setMaSuatChieu(int maSuatChieu) {
        this.maSuatChieu = maSuatChieu;
    }

    public int getMaPhim() {
        return maPhim;
    }

    public void setMaPhim(int maPhim) {
        this.maPhim = maPhim;
    }

    public int getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(int maPhong) {
        this.maPhong = maPhong;
    }

    public LocalDateTime getNgayGioChieu() {
        return ngayGioChieu;
    }

    public void setNgayGioChieu(LocalDateTime ngayGioChieu) {
        this.ngayGioChieu = ngayGioChieu;
    }

    public String getTenPhim() {
        return tenPhim;
    }

    public void setTenPhim(String tenPhim) {
        this.tenPhim = tenPhim;
    }

    public String getTenPhong() {
        return tenPhong;
    }

    public void setTenPhong(String tenPhong) {
        this.tenPhong = tenPhong;
    }

    public int getThoiLuongPhim() {
        return thoiLuongPhim;
    }

    public void setThoiLuongPhim(int thoiLuongPhim) {
        this.thoiLuongPhim = thoiLuongPhim;
    }

    public String getKieugPhim() {
        return kieuPhim;
    }

    public void setKieuPhim(String kieuPhim) {
        this.kieuPhim = kieuPhim;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return String.format("%s - %s - %s",
                tenPhim != null ? tenPhim : "N/A",
                tenPhong != null ? tenPhong : "N/A",
                ngayGioChieu != null ? ngayGioChieu.format(formatter) : "N/A");
    }
}