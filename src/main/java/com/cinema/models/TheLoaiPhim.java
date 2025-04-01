package com.cinema.models;

public class TheLoaiPhim {
    private int maTheLoai;
    private int tenTheLoai;

    public TheLoaiPhim(int maTheLoai, int tenTheLoai) {
        this.maTheLoai = maTheLoai;
        this.tenTheLoai = tenTheLoai;
    }

    public int getMaTheLoai() {
        return maTheLoai;
    }

    public void setMaTheLoai(int maTheLoai) {
        this.maTheLoai = maTheLoai;
    }

    public int getTenTheLoai() {
        return tenTheLoai;
    }

    public void setTenTheLoai(int tenTheLoai) {
        this.tenTheLoai = tenTheLoai;
    }
}


