package com.cinema.models;

public class Ghe {
    private int maPhong;
    private String soGhe;

    public Ghe(int maPhong, String soGhe) {
        this.maPhong = maPhong;
        this.soGhe = soGhe;
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
}
