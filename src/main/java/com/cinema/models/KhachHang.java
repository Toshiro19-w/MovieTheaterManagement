package com.cinema.models;

public class KhachHang extends NguoiDung {
    private int maKhachHang;
    private int diemTichLuy;

    public KhachHang(int maNguoiDung, String hoTen, String soDienThoai, String email, LoaiNguoiDung loaiNguoiDung, int maKhachHang, int maNguoiDung1, int diemTichLuy) {
        super(maNguoiDung, hoTen, soDienThoai, email, loaiNguoiDung);
        this.maKhachHang = maKhachHang;
        this.diemTichLuy = diemTichLuy;
    }

    public KhachHang() {
        super();
    }

    public int getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(int maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public int getDiemTichLuy() {
        return diemTichLuy;
    }

    public void setDiemTichLuy(int diemTichLuy) {
        this.diemTichLuy = diemTichLuy;
    }
}

