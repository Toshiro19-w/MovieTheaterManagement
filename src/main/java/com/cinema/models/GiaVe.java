package com.cinema.models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class GiaVe {
    private int maGiaVe;
    private String loaiGhe;
    private LocalDate ngayApDung;
    private LocalDate ngayKetThuc;
    private BigDecimal giaVe;
    private String ghiChu;
    
    public GiaVe() {
    }
    
    public GiaVe(int maGiaVe, String loaiGhe, LocalDate ngayApDung, LocalDate ngayKetThuc, BigDecimal giaVe, String ghiChu) {
        this.maGiaVe = maGiaVe;
        this.loaiGhe = loaiGhe;
        this.ngayApDung = ngayApDung;
        this.ngayKetThuc = ngayKetThuc;
        this.giaVe = giaVe;
        this.ghiChu = ghiChu;
    }
    
    public GiaVe(int maGiaVe, String loaiGhe, LocalDate ngayApDung, BigDecimal giaVe, String ghiChu) {
        this.maGiaVe = maGiaVe;
        this.loaiGhe = loaiGhe;
        this.ngayApDung = ngayApDung;
        this.giaVe = giaVe;
        this.ghiChu = ghiChu;
    }

    public int getMaGiaVe() {
        return maGiaVe;
    }

    public void setMaGiaVe(int maGiaVe) {
        this.maGiaVe = maGiaVe;
    }

    public String getLoaiGhe() {
        return loaiGhe;
    }

    public void setLoaiGhe(String loaiGhe) {
        this.loaiGhe = loaiGhe;
    }

    public LocalDate getNgayApDung() {
        return ngayApDung;
    }

    public void setNgayApDung(LocalDate ngayApDung) {
        this.ngayApDung = ngayApDung;
    }

    public BigDecimal getGiaVe() {
        return giaVe;
    }

    public void setGiaVe(BigDecimal giaVe) {
        this.giaVe = giaVe;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
    
    public LocalDate getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(LocalDate ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }
}