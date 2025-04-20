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
    private String dinhDang;
    private String moTa;
    private String daoDien;
    private int soSuatChieu;

    public Phim(int maPhim, String tenPhim, int maTheLoai, int thoiLuong, LocalDate ngayKhoiChieu, String nuocSanXuat, String dinhDang, String moTa, String daoDien) {
        this.maPhim = maPhim;
        this.tenPhim = tenPhim;
        this.maTheLoai = maTheLoai;
        this.thoiLuong = thoiLuong;
        this.ngayKhoiChieu = ngayKhoiChieu;
        this.nuocSanXuat = nuocSanXuat;
        this.dinhDang = dinhDang;
        this.moTa = moTa;
        this.daoDien = daoDien;
    }

    public Phim(int maPhim, String tenPhim, String tenTheLoai, int thoiLuong, LocalDate ngayKhoiChieu, String nuocSanXuat, String dinhDang, String moTa, String daoDien, int soSuatChieu) {
        this.maPhim = maPhim;
        this.tenPhim = tenPhim;
        this.tenTheLoai = tenTheLoai;
        this.thoiLuong = thoiLuong;
        this.ngayKhoiChieu = ngayKhoiChieu;
        this.nuocSanXuat = nuocSanXuat;
        this.dinhDang = dinhDang;
        this.moTa = moTa;
        this.daoDien = daoDien;
        this.soSuatChieu = soSuatChieu;
    }

    public Phim() {}

    public Phim(int maPhim, String tenPhim, int maTheLoai,
                String tenTheLoai, int thoiLuong, LocalDate ngayKhoiChieu,
                String nuocSanXuat, String dinhDang, String moTa, String daoDien, int i) {
        this.maPhim = maPhim;
        this.tenPhim = tenPhim;
        this.maTheLoai = maTheLoai;
        this.tenTheLoai = tenTheLoai;
        this.thoiLuong = thoiLuong;
        this.ngayKhoiChieu = ngayKhoiChieu;
        this.nuocSanXuat = nuocSanXuat;
        this.dinhDang = dinhDang;
        this.moTa = moTa;
        this.daoDien = daoDien;
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

    public String getDinhDang() {
        return dinhDang;
    }

    public void setDinhDang(String dinhDang) {
        this.dinhDang = dinhDang;
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

    public int getSoSuatChieu() {
        return soSuatChieu;
    }

    public void setSoSuatChieu(int soSuatChieu) {
        this.soSuatChieu = soSuatChieu;
    }

    @Override
    public String toString() {
        return tenPhim;
    }
}