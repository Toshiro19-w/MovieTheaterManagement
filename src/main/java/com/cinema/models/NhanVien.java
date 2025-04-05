package com.cinema.models;

import java.math.BigDecimal;

public class NhanVien extends NguoiDung{
    private String chucVu;
    private BigDecimal luong;
    private VaiTro vaiTro;

    public NhanVien(int maNguoiDung, String hoTen, String soDienThoai, String email, LoaiNguoiDung loaiNguoiDung, String chucVu, BigDecimal luong, VaiTro vaiTro) {
        super(maNguoiDung, hoTen, soDienThoai, email, loaiNguoiDung);
        this.chucVu = chucVu;
        this.luong = luong;
        this.vaiTro = vaiTro;
    }

    public String getChucVu() {
        return chucVu;
    }

    public void setChucVu(String chucVu) {
        this.chucVu = chucVu;
    }

    public BigDecimal getLuong() {
        return luong;
    }

    public void setLuong(BigDecimal luong) {
        this.luong = luong;
    }

    public VaiTro getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(VaiTro vaiTro) {
        this.vaiTro = vaiTro;
    }
}
