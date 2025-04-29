package com.cinema.models;

import java.math.BigDecimal;

public class NhanVien extends NguoiDung{
    private BigDecimal luong;
    private VaiTro vaiTro;

    public NhanVien(int maNguoiDung, String hoTen, String soDienThoai, String email, LoaiNguoiDung loaiNguoiDung, BigDecimal luong, VaiTro vaiTro) {
        super(maNguoiDung, hoTen, soDienThoai, email, loaiNguoiDung);
        this.luong = luong;
        this.vaiTro = vaiTro;
    }

    public NhanVien() {}

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
