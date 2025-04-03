package com.cinema.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Ve {
    private int maVe;
    private int maSuatChieu;
    private Integer maPhong;
    private String soGhe;
    private Integer maHoaDon;
    private BigDecimal giaVe;
    private TrangThaiVe trangThai;
    private LocalDateTime ngayDat;

    //JOIN chỉ cho user
    private String tenPhim;
    private LocalDate ngayGioChieu;
    private String hoTenKhachHang;
    private String loaiPhong;

    public Ve(int maVe, int maSuatChieu, Integer maPhong,
              String soGhe, Integer maHoaDon, BigDecimal giaVe,
              TrangThaiVe trangThai, LocalDateTime ngayDat) {
        this.maVe = maVe;
        this.maSuatChieu = maSuatChieu;
        this.maPhong = maPhong;
        this.soGhe = soGhe;
        this.maHoaDon = maHoaDon;
        this.giaVe = giaVe;
        this.trangThai = trangThai;
        this.ngayDat = ngayDat;
    }

    public Ve(int maVe, int maSuatChieu, Integer maPhong,
              String soGhe, Integer maHoaDon, BigDecimal giaVe,
              TrangThaiVe trangThai, LocalDateTime ngayDat, String tenPhim, LocalDate ngayGioChieu,
              String hoTenKhachHang, String loaiPhong) {
        this.maVe = maVe;
        this.maSuatChieu = maSuatChieu;
        this.maPhong = maPhong;
        this.soGhe = soGhe;
        this.maHoaDon = maHoaDon;
        this.giaVe = giaVe;
        this.trangThai = trangThai;
        this.ngayDat = ngayDat;
        this.tenPhim = tenPhim;
        this.ngayGioChieu = ngayGioChieu;
        this.hoTenKhachHang = hoTenKhachHang;
        this.loaiPhong = loaiPhong;
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

    public Integer getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(Integer maPhong) {
        this.maPhong = maPhong;
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

    public BigDecimal getGiaVe() {
        return giaVe;
    }

    public void setGiaVe(BigDecimal giaVe) {
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

    public LocalDateTime getNgayDat() {
        return ngayDat;
    }

    public void setNgayDat(LocalDateTime ngayDat) {
        this.ngayDat = ngayDat;
    }
}