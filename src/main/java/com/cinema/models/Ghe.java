package com.cinema.models;

public class Ghe {
    private int maGhe;
    private int maPhong;
    private String soGhe;
    private String loaiGhe;

    public Ghe(int maGhe, int maPhong, String soGhe, String loaiGhe) {
        this.maGhe = maGhe;
        this.maPhong = maPhong;
        this.soGhe = soGhe;
        this.loaiGhe = loaiGhe;
    }

    public Ghe() {}

    public int getMaGhe() {
        return this.maGhe;
    }

    public void setMaGhe(int maGhe) {
        this.maGhe = maGhe;
    }

    public int getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(int maPhong) {
        this.maPhong = maPhong;
    }

    public String getSoGhe() {
        return soGhe;
    }

    public void setSoGhe(String soGhe) {
        this.soGhe = soGhe;
    }

    public String getLoaiGhe() {
        return this.loaiGhe;
    }

    public void setLoaiGhe(String loaiGhe) {
        this.loaiGhe = loaiGhe;
    }

    @Override
    public String toString() {
        return "Phòng " + maPhong + " - " + soGhe + " - " + loaiGhe;
    }
}
