package com.cinema.models;

public class ChiTietHoaDon {
    private int maHoaDon;
    private int maVe;
    private int soLuong;
    private double thanhTien;

    public ChiTietHoaDon(int maHoaDon, int maVe, int soLuong, double thanhTien) {
        this.maHoaDon = maHoaDon;
        this.maVe = maVe;
        this.soLuong = soLuong;
        this.thanhTien = thanhTien;
    }

    public int getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(int maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public int getMaVe() {
        return maVe;
    }

    public void setMaVe(int maVe) {
        this.maVe = maVe;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public double getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(double thanhTien) {
        this.thanhTien = thanhTien;
    }
}
