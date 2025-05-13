package com.cinema.models;
import java.time.LocalDate;

public class Phim {
    private int maPhim;
    private String tenPhim;
    private String tenTheLoai;
    private int maTheLoai;
    private int thoiLuong;
    private LocalDate ngayKhoiChieu;
    private String nuocSanXuat;
    private String kieuPhim;
    private String moTa;
    private String daoDien;
    private String duongDanPoster;
    private String trangThai;

    public Phim(int maPhim, String tenPhim, String tenTheLoai, int thoiLuong, LocalDate ngayKhoiChieu, String nuocSanXuat, String kieuPhim, String moTa, String daoDien, String duongDanPoster, String trangThai) {
        this.maPhim = maPhim;
        this.tenPhim = tenPhim;
        this.tenTheLoai = tenTheLoai;
        this.thoiLuong = thoiLuong;
        this.ngayKhoiChieu = ngayKhoiChieu;
        this.nuocSanXuat = nuocSanXuat;
        this.kieuPhim = kieuPhim;
        this.moTa = moTa;
        this.daoDien = daoDien;
        this.duongDanPoster = duongDanPoster;
        this.trangThai = trangThai;
    }

    public Phim() {}

    public Phim(int maPhim, String tenPhim, int maTheLoai,
                String tenTheLoai, int thoiLuong, LocalDate ngayKhoiChieu,
                String nuocSanXuat, String kieuPhim, String moTa, String daoDien, int i) {
        this.maPhim = maPhim;
        this.tenPhim = tenPhim;
        this.maTheLoai = maTheLoai;
        this.tenTheLoai = tenTheLoai;
        this.thoiLuong = thoiLuong;
        this.ngayKhoiChieu = ngayKhoiChieu;
        this.nuocSanXuat = nuocSanXuat;
        this.kieuPhim = kieuPhim;
        this.moTa = moTa;
        this.daoDien = daoDien;
    }

    public String getDuongDanPoster() {
        return duongDanPoster;
    }

    public void setDuongDanPoster(String duongDanPoster) {
        this.duongDanPoster = duongDanPoster;
    }

    public int getMaPhim() {
        return maPhim;
    }

    public void setMaPhim(int maPhim) {
        this.maPhim = maPhim;
    }

    public String getTenPhim() {
        return tenPhim;
    }

    public void setTenPhim(String tenPhim) {
        this.tenPhim = tenPhim;
    }

    public int getMaTheLoai() {
        return maTheLoai;
    }

    public void setMaTheLoai(int maTheLoai) {
        this.maTheLoai = maTheLoai;
    }

    public int getThoiLuong() {
        return thoiLuong;
    }

    public void setThoiLuong(int thoiLuong) {
        this.thoiLuong = thoiLuong;
    }

    public LocalDate getNgayKhoiChieu() {
        return ngayKhoiChieu;
    }

    public void setNgayKhoiChieu(LocalDate ngayKhoiChieu) {
        this.ngayKhoiChieu = ngayKhoiChieu;
    }

    public String getNuocSanXuat() {
        return nuocSanXuat;
    }

    public void setNuocSanXuat(String nuocSanXuat) {
        this.nuocSanXuat = nuocSanXuat;
    }

    public String getKieuPhim() {
        return kieuPhim;
    }

    public void setKieuPhim(String kieuPhim) {
        this.kieuPhim = kieuPhim;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public String getDaoDien() {
        return daoDien;
    }

    public void setDaoDien(String daoDien) {
        this.daoDien = daoDien;
    }

    public String getTenTheLoai() {
        return tenTheLoai;
    }

    public void setTenTheLoai(String tenTheLoai) {
        this.tenTheLoai = tenTheLoai;
    }

    @Override
    public String toString() {
        return tenPhim;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}