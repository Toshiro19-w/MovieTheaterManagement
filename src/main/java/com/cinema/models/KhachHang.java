package com.cinema.models;

public class KhachHang extends NguoiDung {
    private int diemTichLuy;

    public KhachHang(int maNguoiDung, String hoTen, String soDienThoai, String email, LoaiNguoiDung loaiNguoiDung, int diemTichLuy) {
        super(maNguoiDung, hoTen, soDienThoai, email, loaiNguoiDung);
        this.diemTichLuy = diemTichLuy;
    }
    
    public KhachHang() {
        super();
    }

    public int getDiemTichLuy() {
        return diemTichLuy;
    }

    public void setDiemTichLuy(int diemTichLuy) {
        this.diemTichLuy = diemTichLuy;
    }
}

