package com.cinema.models;

public class BaoCaoNhanVien {
    private String tenNhanVien;
    private String vaiTro;
    private int soVeDaBan;
    private double tongDoanhThu;

    public BaoCaoNhanVien(String tenNhanVien, String vaiTro, int soVeDaBan, double tongDoanhThu) {
        this.tenNhanVien = tenNhanVien;
        this.vaiTro = vaiTro;
        this.soVeDaBan = soVeDaBan;
        this.tongDoanhThu = tongDoanhThu;
    }

    public String getTenNhanVien() {
        return tenNhanVien;
    }

    public void setTenNhanVien(String tenNhanVien) {
        this.tenNhanVien = tenNhanVien;
    }

    public String getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(String vaiTro) {
        this.vaiTro = vaiTro;
    }

    public int getSoVeDaBan() {
        return soVeDaBan;
    }

    public void setSoVeDaBan(int soVeDaBan) {
        this.soVeDaBan = soVeDaBan;
    }

    public double getTongDoanhThu() {
        return tongDoanhThu;
    }

    public void setTongDoanhThu(double tongDoanhThu) {
        this.tongDoanhThu = tongDoanhThu;
    }
} 