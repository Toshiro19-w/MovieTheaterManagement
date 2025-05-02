package com.cinema.models;

import com.cinema.enums.LoaiNguoiDung;

public class NguoiDung {
    private int maNguoiDung;
    private String hoTen;
    private String soDienThoai;
    private String email;
    private LoaiNguoiDung loaiNguoiDung;

    public NguoiDung(int maNguoiDung, String hoTen, String soDienThoai, String email, LoaiNguoiDung loaiNguoiDung) {
        this.maNguoiDung = maNguoiDung;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.loaiNguoiDung = loaiNguoiDung;
    }

    public NguoiDung() {}

    public int getMaNguoiDung() {
        return maNguoiDung;
    }

    public void setMaNguoiDung(int maNguoiDung) {
        this.maNguoiDung = maNguoiDung;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LoaiNguoiDung getLoaiNguoiDung() {
        return loaiNguoiDung;
    }

    public void setLoaiNguoiDung(LoaiNguoiDung loaiNguoiDung) {
        this.loaiNguoiDung = loaiNguoiDung;
    }
}
