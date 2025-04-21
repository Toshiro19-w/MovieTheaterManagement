package com.cinema.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ChiTietHoaDon {
    private int maHoaDon;
    private int maVe;
    private String soGhe;
    private BigDecimal giaVe;
    private String tenPhim;
    private LocalDateTime ngayGioChieu;

    public ChiTietHoaDon(int maHoaDon, int maVe) {
        this.maHoaDon = maHoaDon;
        this.maVe = maVe;
    }

    public int getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(int maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public int getMaVe() {
        return maVe;
    }

    public void setMaVe(int maVe) {
        this.maVe = maVe;
    }

    public String getSoGhe() { return soGhe; }

    public void setSoGhe(String soGhe) { this.soGhe = soGhe; }

    public BigDecimal getGiaVe() { return giaVe; }

    public void setGiaVe(BigDecimal giaVe) { this.giaVe = giaVe; }

    public String getTenPhim() { return tenPhim; }

    public void setTenPhim(String tenPhim) { this.tenPhim = tenPhim; }

    public LocalDateTime getNgayGioChieu() {
        return ngayGioChieu;
    }

    public void setNgayGioChieu(LocalDateTime ngayGioChieu) {
        this.ngayGioChieu = ngayGioChieu;
    }
}
