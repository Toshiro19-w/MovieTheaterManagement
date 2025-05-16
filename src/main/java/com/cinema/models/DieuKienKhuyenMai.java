package com.cinema.models;

import java.math.BigDecimal;

public class DieuKienKhuyenMai {
    private int maDieuKien;
    private int maKhuyenMai;
    private String loaiDieuKien; // ENUM('Phim')
    private BigDecimal giaTri; 
    private String moTa;

    // Navigation property
    private KhuyenMai khuyenMai;

    // Constructors
    public DieuKienKhuyenMai() {}
    
    public DieuKienKhuyenMai(int maDieuKien, int maKhuyenMai, String loaiDieuKien, 
            BigDecimal giaTri, String moTa) {
        this.maDieuKien = maDieuKien;
        this.maKhuyenMai = maKhuyenMai;
        this.loaiDieuKien = loaiDieuKien;
        this.giaTri = giaTri;
        this.moTa = moTa;
    }

    // Getters and Setters
    public int getMaDieuKien() {
        return maDieuKien;
    }

    public void setMaDieuKien(int maDieuKien) {
        this.maDieuKien = maDieuKien;
    }

    public int getMaKhuyenMai() {
        return maKhuyenMai;
    }

    public void setMaKhuyenMai(int maKhuyenMai) {
        this.maKhuyenMai = maKhuyenMai;
    }

    public String getLoaiDieuKien() {
        return loaiDieuKien;
    }

    public void setLoaiDieuKien(String loaiDieuKien) {
        this.loaiDieuKien = loaiDieuKien;
    }

    public BigDecimal getGiaTri() {
        return giaTri;
    }

    public void setGiaTri(BigDecimal giaTri) {
        this.giaTri = giaTri;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public KhuyenMai getKhuyenMai() {
        return khuyenMai;
    }

    public void setKhuyenMai(KhuyenMai khuyenMai) {
        this.khuyenMai = khuyenMai;
    }

    @Override
    public String toString() {
        return String.format("DieuKienKM[id=%d, loai=%s, giaTri=%s]", 
            maDieuKien, loaiDieuKien, giaTri);
    }
}