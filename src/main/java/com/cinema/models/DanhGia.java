package com.cinema.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DanhGia {
    private int maDanhGia;
    private int maPhim;
    private int maNguoiDung;
    private int maVe;
    private int diemDanhGia; // 1-5 sao
    private String nhanXet;
    private LocalDateTime ngayDanhGia;
    
    // Thêm trường để hiển thị tên người dùng
    private String tenNguoiDung;

    // Navigation properties
    private Phim phim;
    private NguoiDung nguoiDung;
    private Ve ve;

    public int getMaDanhGia() {
        return this.maDanhGia;
    }

    public void setMaDanhGia(int maDanhGia) {
        this.maDanhGia = maDanhGia;
    }

    public int getMaPhim() {
        return this.maPhim;
    }

    public void setMaPhim(int maPhim) {
        this.maPhim = maPhim;
    }

    public int getMaNguoiDung() {
        return this.maNguoiDung;
    }

    public void setMaNguoiDung(int maNguoiDung) {
        this.maNguoiDung = maNguoiDung;
    }

    public int getMaVe() {
        return this.maVe;
    }

    public void setMaVe(int maVe) {
        this.maVe = maVe;
    }

    public int getDiemDanhGia() {
        return this.diemDanhGia;
    }

    public void setDiemDanhGia(int diemDanhGia) {
        this.diemDanhGia = diemDanhGia;
    }

    public String getNhanXet() {
        return this.nhanXet;
    }

    public void setNhanXet(String nhanXet) {
        this.nhanXet = nhanXet;
    }

    public LocalDateTime getNgayDanhGia() {
        return this.ngayDanhGia;
    }

    public void setNgayDanhGia(LocalDateTime ngayDanhGia) {
        this.ngayDanhGia = ngayDanhGia;
    }

    public Phim getPhim() {
        return this.phim;
    }

    public void setPhim(Phim phim) {
        this.phim = phim;
    }

    public NguoiDung getNguoiDung() {
        return this.nguoiDung;
    }

    public void setNguoiDung(NguoiDung nguoiDung) {
        this.nguoiDung = nguoiDung;
    }

    public Ve getVe() {
        return this.ve;
    }

    public void setVe(Ve ve) {
        this.ve = ve;
    }
    
    public String getTenNguoiDung() {
        return tenNguoiDung;
    }

    public void setTenNguoiDung(String tenNguoiDung) {
        this.tenNguoiDung = tenNguoiDung;
    }
    
    public String getNgayDanhGiaFormatted() {
        if (ngayDanhGia == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return ngayDanhGia.format(formatter);
    }
    
    // Constructors
    public DanhGia() {}
    
    public DanhGia(int maDanhGia, int maPhim, int maNguoiDung, int maVe, 
            int diemDanhGia, String nhanXet, LocalDateTime ngayDanhGia) {
        this.maDanhGia = maDanhGia;
        this.maPhim = maPhim;
        this.maNguoiDung = maNguoiDung;
        this.maVe = maVe;
        this.diemDanhGia = diemDanhGia;
        this.nhanXet = nhanXet;
        this.ngayDanhGia = ngayDanhGia;
    }
    
    @Override
    public String toString() {
        return String.format("DanhGia[id=%d, diem=%d, ngay=%s]", 
            maDanhGia, diemDanhGia, ngayDanhGia);
    }
}