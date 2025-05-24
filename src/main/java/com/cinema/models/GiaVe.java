package com.cinema.models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class GiaVe {
    private int maGiaVe;
    private String loaiGhe; // ENUM('Thuong', 'VIP')
    private LocalDate ngayApDung;
    private BigDecimal giaVe;
    private String ghiChu;

    private GiaVe() {}

    public GiaVe(int maGiaVe, String loaiGhe, LocalDate ngayApDung, BigDecimal giaVe, String ghiChu) {
        this.maGiaVe = maGiaVe;
        this.loaiGhe = loaiGhe;
        this.ngayApDung = ngayApDung;
        this.giaVe = giaVe;
        this.ghiChu = ghiChu;
    }

    public int getMaGiaVe() {
        return this.maGiaVe;
    }

    public void setMaGiaVe(int maGiaVe) {
        this.maGiaVe = maGiaVe;
    }

    public String getLoaiGhe() {
        return this.loaiGhe;
    }

    public void setLoaiGhe(String loaiGhe) {
        this.loaiGhe = loaiGhe;
    }

    public LocalDate getNgayApDung() {
        return this.ngayApDung;
    }

    public void setNgayApDung(LocalDate ngayApDung) {
        this.ngayApDung = ngayApDung;
    }

    public BigDecimal getGiaVe() {
        return this.giaVe;
    }

    public void setGiaVe(BigDecimal giaVe) {
        this.giaVe = giaVe;
    }

    public String getGhiChu() {
        return this.ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}