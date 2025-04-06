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
    private String dinhDangPhim;

    public SuatChieu(int maSuatChieu, String tenPhim, String tenPhong, LocalDateTime ngayGioChieu, int thoiLuongPhim, String dinhDangPhim) {
        this.maSuatChieu = maSuatChieu;
        this.tenPhim = tenPhim;
        this.tenPhong = tenPhong;
        this.ngayGioChieu = ngayGioChieu;
        this.thoiLuongPhim = thoiLuongPhim;
        this.dinhDangPhim = dinhDangPhim;
    }

    public SuatChieu(int maSuatChieu, int maPhim, int maPhong, LocalDateTime ngayGioChieu) {
        this.maSuatChieu = maSuatChieu;
        this.maPhim = maPhim;
        this.maPhong = maPhong;
        this.ngayGioChieu = ngayGioChieu;
    }

    public SuatChieu(int maSuatChieu, String tenPhim, int thoiLuongPhim, String dinhDangPhim,
                     String tenPhong, int maPhong, LocalDateTime ngayGioChieu) {
        this.maSuatChieu = maSuatChieu;
        this.tenPhim = tenPhim != null ? tenPhim : "Unknown Film";
        this.thoiLuongPhim = thoiLuongPhim;
        this.dinhDangPhim = dinhDangPhim != null ? dinhDangPhim : "Unknown Format";
        this.tenPhong = tenPhong != null ? tenPhong : "Unknown Room";
        this.maPhong = maPhong;
        this.ngayGioChieu = ngayGioChieu;
    }

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

    public String getDinhDangPhim() {
        return dinhDangPhim;
    }

    public void setDinhDangPhim(String dinhDangPhim) {
        this.dinhDangPhim = dinhDangPhim;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return tenPhim + " - " + tenPhong + " - " +
                (ngayGioChieu != null ? ngayGioChieu.format(formatter) : "N/A");
    }
}