package com.cinema.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class KhuyenMai {
    private int maKhuyenMai;
    private String tenKhuyenMai;
    private String moTa;
    private String loaiGiamGia; // ENUM('PhanTram', 'CoDinh')
    private BigDecimal giaTriGiam;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private String trangThai;

    // Navigation properties
    private List<DieuKienKhuyenMai> dieuKienKhuyenMai;

    // Khởi tạo list trong constructor mặc định
    public KhuyenMai() {
        this.dieuKienKhuyenMai = new ArrayList<>();
    }

    // Khởi tạo list trong constructor có tham số
    public KhuyenMai(int maKhuyenMai, String tenKhuyenMai, String moTa, String loaiGiamGia, 
            BigDecimal giaTriGiam, LocalDate ngayBatDau, LocalDate ngayKetThuc, String trangThai) {
        this.maKhuyenMai = maKhuyenMai;
        this.tenKhuyenMai = tenKhuyenMai;
        this.moTa = moTa;
        this.loaiGiamGia = loaiGiamGia;
        this.giaTriGiam = giaTriGiam;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.trangThai = trangThai;
        this.dieuKienKhuyenMai = new ArrayList<>();
    }

    public int getMaKhuyenMai() {
        return this.maKhuyenMai;
    }

    public void setMaKhuyenMai(int maKhuyenMai) {
        this.maKhuyenMai = maKhuyenMai;
    }

    public String getTenKhuyenMai() {
        return this.tenKhuyenMai;
    }

    public void setTenKhuyenMai(String tenKhuyenMai) {
        this.tenKhuyenMai = tenKhuyenMai;
    }

    public String getMoTa() {
        return this.moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public String getLoaiGiamGia() {
        return this.loaiGiamGia;
    }

    public void setLoaiGiamGia(String loaiGiamGia) {
        this.loaiGiamGia = loaiGiamGia;
    }

    public BigDecimal getGiaTriGiam() {
        return this.giaTriGiam;
    }

    public void setGiaTriGiam(BigDecimal giaTriGiam) {
        this.giaTriGiam = giaTriGiam;
    }

    public LocalDate getNgayBatDau() {
        return this.ngayBatDau;
    }

    public void setNgayBatDau(LocalDate ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public LocalDate getNgayKetThuc() {
        return this.ngayKetThuc;
    }

    public void setNgayKetThuc(LocalDate ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

    public String getTrangThai() {
        return this.trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    } // ENUM('HoatDong', 'HetHan', 'DaHuy')

    public List<DieuKienKhuyenMai> getDieuKienKhuyenMai() {
        return this.dieuKienKhuyenMai;
    }

    public void setDieuKienKhuyenMai(List<DieuKienKhuyenMai> dieuKienKhuyenMai) {
        this.dieuKienKhuyenMai = dieuKienKhuyenMai;
    }

    // Thêm các phương thức tiện ích để quản lý danh sách
    public void themDieuKien(DieuKienKhuyenMai dieuKien) {
        if (this.dieuKienKhuyenMai == null) {
            this.dieuKienKhuyenMai = new ArrayList<>();
        }
        this.dieuKienKhuyenMai.add(dieuKien);
    }

    public void xoaDieuKien(DieuKienKhuyenMai dieuKien) {
        if (this.dieuKienKhuyenMai != null) {
            this.dieuKienKhuyenMai.remove(dieuKien);
        }
    }

    public void xoaTatCaDieuKien() {
        if (this.dieuKienKhuyenMai != null) {
            this.dieuKienKhuyenMai.clear();
        }
    }
}