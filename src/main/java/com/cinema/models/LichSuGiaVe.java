package com.cinema.models;

import java.time.LocalDateTime;

public class LichSuGiaVe {
    private int maLichSu;
    private String loaiGhe;
    private double giaVeCu;
    private double giaVeMoi;
    private LocalDateTime ngayThayDoi;
    private Integer nguoiThayDoi;
    
    // Constructor đầy đủ
    public LichSuGiaVe(int maLichSu, String loaiGhe, double giaVeCu, double giaVeMoi, 
                      LocalDateTime ngayThayDoi, Integer nguoiThayDoi) {
        this.maLichSu = maLichSu;
        this.loaiGhe = loaiGhe;
        this.giaVeCu = giaVeCu;
        this.giaVeMoi = giaVeMoi;
        this.ngayThayDoi = ngayThayDoi;
        this.nguoiThayDoi = nguoiThayDoi;
    }
    
    // Constructor không có maLichSu (cho việc tạo mới)
    public LichSuGiaVe(String loaiGhe, double giaVeCu, double giaVeMoi, 
                      LocalDateTime ngayThayDoi, Integer nguoiThayDoi) {
        this.loaiGhe = loaiGhe;
        this.giaVeCu = giaVeCu;
        this.giaVeMoi = giaVeMoi;
        this.ngayThayDoi = ngayThayDoi;
        this.nguoiThayDoi = nguoiThayDoi;
    }
    
    // Constructor mặc định
    public LichSuGiaVe() {
    }

    // Getters và Setters
    public int getMaLichSu() {
        return maLichSu;
    }

    public void setMaLichSu(int maLichSu) {
        this.maLichSu = maLichSu;
    }

    public String getLoaiGhe() {
        return loaiGhe;
    }

    public void setLoaiGhe(String loaiGhe) {
        this.loaiGhe = loaiGhe;
    }

    public double getGiaVeCu() {
        return giaVeCu;
    }

    public void setGiaVeCu(double giaVeCu) {
        this.giaVeCu = giaVeCu;
    }

    public double getGiaVeMoi() {
        return giaVeMoi;
    }

    public void setGiaVeMoi(double giaVeMoi) {
        this.giaVeMoi = giaVeMoi;
    }

    public LocalDateTime getNgayThayDoi() {
        return ngayThayDoi;
    }

    public void setNgayThayDoi(LocalDateTime ngayThayDoi) {
        this.ngayThayDoi = ngayThayDoi;
    }

    public Integer getNguoiThayDoi() {
        return nguoiThayDoi;
    }

    public void setNguoiThayDoi(Integer nguoiThayDoi) {
        this.nguoiThayDoi = nguoiThayDoi;
    }
    
    @Override
    public String toString() {
        return "LichSuGiaVe{" +
                "maLichSu=" + maLichSu +
                ", loaiGhe='" + loaiGhe + '\'' +
                ", giaVeCu=" + giaVeCu +
                ", giaVeMoi=" + giaVeMoi +
                ", ngayThayDoi=" + ngayThayDoi +
                ", nguoiThayDoi=" + nguoiThayDoi +
                '}';
    }
}