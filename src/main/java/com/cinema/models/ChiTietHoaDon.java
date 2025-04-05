package com.cinema.models;

public class ChiTietHoaDon {
    private int maHoaDon;
    private int maVe;

    public ChiTietHoaDon(int maHoaDon, int maVe) {
        this.maHoaDon = maHoaDon;
        this.maVe = maVe;
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
}
