package com.cinema.models;

import java.time.LocalDateTime;

public class Ve {
    private int maVe;
    private int maSuatChieu;
    private Integer maKhachHang; // Có thể null
    private Integer maHoaDon;    // Có thể null
    private String soGhe;
    private double giaVe;
    private String trangThai;
    
    // Thông tin từ các bảng JOIN
    private String tenPhim;
    private LocalDateTime ngayGioChieu;
    private String loaiPhong;
    private String hoTenKhachHang;
    
    // Constructors
    public Ve() {}

    public Ve(int maVe, int maSuatChieu, Integer maKhachHang, Integer maHoaDon, 
             String soGhe, double giaVe, String trangThai) {
        this.maVe = maVe;
        this.maSuatChieu = maSuatChieu;
        this.maKhachHang = maKhachHang;
        this.maHoaDon = maHoaDon;
        this.soGhe = soGhe;
        this.giaVe = giaVe;
        this.trangThai = trangThai;
    }

    // Getters và Setters
    public int getMaVe() {
        return maVe;
    }

    public void setMaVe(int maVe) {
        this.maVe = maVe;
    }

    public int getMaSuatChieu() {
        return maSuatChieu;
    }

    public void setMaSuatChieu(int maSuatChieu) {
        this.maSuatChieu = maSuatChieu;
    }

    public Integer getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(Integer maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public Integer getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(Integer maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public String getSoGhe() {
        return soGhe;
    }

    public void setSoGhe(String soGhe) {
        this.soGhe = soGhe;
    }

    public double getGiaVe() {
        return giaVe;
    }

    public void setGiaVe(double giaVe) {
        this.giaVe = giaVe;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getTenPhim() {
        return tenPhim;
    }

    public void setTenPhim(String tenPhim) {
        this.tenPhim = tenPhim;
    }

    public LocalDateTime getNgayGioChieu() {
        return ngayGioChieu;
    }

    public void setNgayGioChieu(LocalDateTime ngayGioChieu) {
        this.ngayGioChieu = ngayGioChieu;
    }

    public String getLoaiPhong() {
        return loaiPhong;
    }

    public void setLoaiPhong(String loaiPhong) {
        this.loaiPhong = loaiPhong;
    }

    public String getHoTenKhachHang() {
        return hoTenKhachHang;
    }

    public void setHoTenKhachHang(String hoTenKhachHang) {
        this.hoTenKhachHang = hoTenKhachHang;
    }

    @Override
    public String toString() {
        return "Ve{" +
                "maVe=" + maVe +
                ", maSuatChieu=" + maSuatChieu +
                ", maKhachHang=" + maKhachHang +
                ", maHoaDon=" + maHoaDon +
                ", soGhe='" + soGhe + '\'' +
                ", giaVe=" + giaVe +
                ", trangThai='" + trangThai + '\'' +
                ", tenPhim='" + tenPhim + '\'' +
                ", ngayGioChieu=" + ngayGioChieu +
                ", loaiPhong='" + loaiPhong + '\'' +
                ", hoTenKhachHang='" + hoTenKhachHang + '\'' +
                '}';
    }
}