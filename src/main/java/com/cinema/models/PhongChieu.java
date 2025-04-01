package com.cinema.models;

public class PhongChieu {
    private int maPhong;
    private int soLuongGhe;
    private String loaiPhong;

    public PhongChieu(int maPhong, int soLuongGhe, String loaiPhong) {
        this.maPhong = maPhong;
        this.soLuongGhe = soLuongGhe;
        this.loaiPhong = loaiPhong;
    }

    public int getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(int maPhong) {
        this.maPhong = maPhong;
    }

    public int getSoLuongGhe() {
        return soLuongGhe;
    }

    public void setSoLuongGhe(int soLuongGhe) {
        this.soLuongGhe = soLuongGhe;
    }

    public String getLoaiPhong() {
        return loaiPhong;
    }

    public void setLoaiPhong(String loaiPhong) {
        this.loaiPhong = loaiPhong;
    }
}
