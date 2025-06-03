package com.cinema.models;

public class TheLoaiPhim {
    private int maTheLoai;
    private String tenTheLoai;
    
    public TheLoaiPhim() {
    }
    
    public TheLoaiPhim(int maTheLoai, String tenTheLoai) {
        this.maTheLoai = maTheLoai;
        this.tenTheLoai = tenTheLoai;
    }
    
    public int getMaTheLoai() {
        return maTheLoai;
    }
    
    public void setMaTheLoai(int maTheLoai) {
        this.maTheLoai = maTheLoai;
    }
    
    public String getTenTheLoai() {
        return tenTheLoai;
    }
    
    public void setTenTheLoai(String tenTheLoai) {
        this.tenTheLoai = tenTheLoai;
    }
    
    @Override
    public String toString() {
        return tenTheLoai;
    }
}