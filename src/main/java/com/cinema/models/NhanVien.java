package com.cinema.models;

public class NhanVien extends NguoiDung{
    private int maNhanVien;
    private int maNguoiDung;
    private String chucVu;
    private double luong;

    public NhanVien(int maNguoiDung, String hoTen, String soDienThoai, String email, LoaiNguoiDung loaiNguoiDung, int maNhanVien, int maNguoiDung1, String chucVu, double luong) {
        super(maNguoiDung, hoTen, soDienThoai, email, loaiNguoiDung);
        this.maNhanVien = maNhanVien;
        this.maNguoiDung = maNguoiDung1;
        this.chucVu = chucVu;
        this.luong = luong;
    }

    public NhanVien() {
        super();
    }

    public int getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(int maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    @Override
    public int getMaNguoiDung() {
        return maNguoiDung;
    }

    @Override
    public void setMaNguoiDung(int maNguoiDung) {
        this.maNguoiDung = maNguoiDung;
    }

    public String getChucVu() {
        return chucVu;
    }

    public void setChucVu(String chucVu) {
        this.chucVu = chucVu;
    }

    public double getLuong() {
        return luong;
    }

    public void setLuong(double luong) {
        this.luong = luong;
    }
}
