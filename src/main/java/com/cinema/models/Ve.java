package com.cinema.models;

import java.time.LocalDate;

public class Ve {
    private int maVe;
    private int maSuatChieu;
    private Integer maKhachHang;
    private Integer maHoaDon;
    private String soGhe;
    private double giaVe;
    private TrangThaiVe trangThai;
    private LocalDate ngayDat;

    //JOIN
    private String tenPhim;
    private LocalDate ngayGioChieu;
    private String hoTenKhachHang;
    private String loaiPhong;

    public Ve(int maVe, int maSuatChieu, Integer maKhachHang, Integer maHoaDon, String soGhe, double giaVe, TrangThaiVe trangThai, LocalDate ngayDat) {
        this.maVe = maVe;
        this.maSuatChieu = maSuatChieu;
        this.maKhachHang = maKhachHang;
        this.maHoaDon = maHoaDon;
        this.soGhe = soGhe;
        this.giaVe = giaVe;
        this.trangThai = trangThai;
        this.ngayDat = ngayDat;
    }

    public Ve() {}

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

    public TrangThaiVe getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiVe trangThai) {
        this.trangThai = trangThai;
    }

    public String getTenPhim() {
        return tenPhim;
    }

    public void setTenPhim(String tenPhim) {
        this.tenPhim = tenPhim;
    }

    public LocalDate getNgayGioChieu() {
        return ngayGioChieu;
    }

    public void setNgayGioChieu(LocalDate ngayGioChieu) {
        this.ngayGioChieu = ngayGioChieu;
    }

    public String getHoTenKhachHang() {
        return hoTenKhachHang;
    }

    public void setHoTenKhachHang(String hoTenKhachHang) {
        this.hoTenKhachHang = hoTenKhachHang;
    }

    public String getLoaiPhong() {
        return loaiPhong;
    }

    public void setLoaiPhong(String loaiPhong) {
        this.loaiPhong = loaiPhong;
    }

    public LocalDate getNgayDat() {
        return ngayDat;
    }

    public void setNgayDat(LocalDate ngayDat) {
        this.ngayDat = ngayDat;
    }
}