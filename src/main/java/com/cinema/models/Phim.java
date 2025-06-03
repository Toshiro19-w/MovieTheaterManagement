package com.cinema.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp đại diện cho thông tin phim
 */
public class Phim {
    private int maPhim;
    private String tenPhim;
    private int thoiLuong;
    private LocalDate ngayKhoiChieu;
    private String nuocSanXuat;
    private String kieuPhim;
    private String moTa;
    private String daoDien;
    private String duongDanPoster;
    private String trangThai;
    private String tenTheLoai;
    
    // Thêm danh sách mã thể loại để hỗ trợ nhiều thể loại
    private List<Integer> maTheLoaiList = new ArrayList<>();
    
    public Phim() {}
    
    public Phim(int maPhim, String tenPhim, int thoiLuong, LocalDate ngayKhoiChieu, 
                String nuocSanXuat, String kieuPhim, String moTa, String daoDien, 
                String duongDanPoster, String trangThai) {
        this.maPhim = maPhim;
        this.tenPhim = tenPhim;
        this.thoiLuong = thoiLuong;
        this.ngayKhoiChieu = ngayKhoiChieu;
        this.nuocSanXuat = nuocSanXuat;
        this.kieuPhim = kieuPhim;
        this.moTa = moTa;
        this.daoDien = daoDien;
        this.duongDanPoster = duongDanPoster;
        this.trangThai = trangThai;
    }
    
    // Getters và Setters
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
    
    public String getDuongDanPoster() {
        return duongDanPoster;
    }
    
    public void setDuongDanPoster(String duongDanPoster) {
        this.duongDanPoster = duongDanPoster;
    }
    
    public String getTrangThai() {
        return trangThai;
    }
    
    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
    
    public String getTenTheLoai() {
        return tenTheLoai;
    }
    
    public void setTenTheLoai(String tenTheLoai) {
        this.tenTheLoai = tenTheLoai;
    }
    
    public List<Integer> getMaTheLoaiList() {
        return maTheLoaiList;
    }
    
    public void setMaTheLoaiList(List<Integer> maTheLoaiList) {
        this.maTheLoaiList = maTheLoaiList;
    }
    
    public void addMaTheLoai(int maTheLoai) {
        if (!maTheLoaiList.contains(maTheLoai)) {
            maTheLoaiList.add(maTheLoai);
        }
    }
    
    public void removeMaTheLoai(int maTheLoai) {
        maTheLoaiList.remove(Integer.valueOf(maTheLoai));
    }
    
    @Override
    public String toString() {
        return "Phim [maPhim=" + maPhim + ", tenPhim=" + tenPhim + ", thoiLuong=" + thoiLuong
                + ", ngayKhoiChieu=" + ngayKhoiChieu + ", nuocSanXuat=" + nuocSanXuat + ", kieuPhim=" + kieuPhim
                + ", tenTheLoai=" + tenTheLoai + "]";
    }
}