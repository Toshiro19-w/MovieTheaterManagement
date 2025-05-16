package com.cinema.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.cinema.enums.TrangThaiVe;

public class Ve {
    private int maVe;
    private int maSuatChieu;
    private int maGhe;
    private int maGiaVe;
    private int maKhuyenMai;
    private int maHoaDon;
    private TrangThaiVe trangThai;
    private LocalDateTime ngayDat;

    // Navigation properties
    private SuatChieu suatChieu;
    private Ghe ghe;
    private GiaVe giaVe;
    private KhuyenMai khuyenMai;
    private HoaDon hoaDon;

    // Display properties
    private String tenPhong;
    private String soGhe;
    private String loaiGhe;
    private BigDecimal giaVeGoc;
    private BigDecimal giaVeSauGiam;
    private String tenPhim;
    private LocalDateTime ngayGioChieu;
    private String tenKhuyenMai;
    private BigDecimal tienGiam;
    private String tenKhachHang;

    public Ve() {}

    public Ve(int maVe, int maSuatChieu, int maGhe, int maGiaVe, int maKhuyenMai, int maHoaDon, TrangThaiVe trangThai, LocalDateTime ngayDat) {
        this.maVe = maVe;
        this.maSuatChieu = maSuatChieu;
        this.maGhe = maGhe;
        this.maGiaVe = maGiaVe;
        this.maKhuyenMai = maKhuyenMai;
        this.maHoaDon = maHoaDon;
        this.trangThai = trangThai;
        this.ngayDat = ngayDat;
    }

    public int getMaVe() {
        return this.maVe;
    }

    public void setMaVe(int maVe) {
        this.maVe = maVe;
    }

    public int getMaSuatChieu() {
        return this.maSuatChieu;
    }

    public void setMaSuatChieu(int maSuatChieu) {
        this.maSuatChieu = maSuatChieu;
    }

    public int getMaGhe() {
        return this.maGhe;
    }

    public void setMaGhe(int maGhe) {
        this.maGhe = maGhe;
    }

    public int getMaGiaVe() {
        return this.maGiaVe;
    }

    public void setMaGiaVe(int maGiaVe) {
        this.maGiaVe = maGiaVe;
    }

    public int getMaKhuyenMai() {
        return this.maKhuyenMai;
    }

    public void setMaKhuyenMai(int maKhuyenMai) {
        this.maKhuyenMai = maKhuyenMai;
    }

    public int getMaHoaDon() {
        return this.maHoaDon;
    }

    public void setMaHoaDon(int maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public TrangThaiVe getTrangThai() {
        return this.trangThai;
    }

    public void setTrangThai(TrangThaiVe trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDateTime getNgayDat() {
        return this.ngayDat;
    }

    public void setNgayDat(LocalDateTime ngayDat) {
        this.ngayDat = ngayDat;
    }

    public SuatChieu getSuatChieu() {
        return this.suatChieu;
    }

    public void setSuatChieu(SuatChieu suatChieu) {
        this.suatChieu = suatChieu;
    }

    public Ghe getGhe() {
        return this.ghe;
    }

    public void setGhe(Ghe ghe) {
        this.ghe = ghe;
    }

    public GiaVe getGiaVe() {
        return this.giaVe;
    }

    public void setGiaVe(GiaVe giaVe) {
        this.giaVe = giaVe;
    }

    public KhuyenMai getKhuyenMai() {
        return this.khuyenMai;
    }

    public void setKhuyenMai(KhuyenMai khuyenMai) {
        this.khuyenMai = khuyenMai;
    }

    public HoaDon getHoaDon() {
        return this.hoaDon;
    }

    public void setHoaDon(HoaDon hoaDon) {
        this.hoaDon = hoaDon;
    }

    public String getTenPhong() {
        return this.tenPhong;
    }

    public void setTenPhong(String tenPhong) {
        this.tenPhong = tenPhong;
    }

    public String getSoGhe() {
        return this.soGhe;
    }

    public void setSoGhe(String soGhe) {
        this.soGhe = soGhe;
    }

    public String getLoaiGhe() {
        return this.loaiGhe;
    }

    public void setLoaiGhe(String loaiGhe) {
        this.loaiGhe = loaiGhe;
    }

    public BigDecimal getGiaVeGoc() {
        return this.giaVeGoc;
    }

    public void setGiaVeGoc(BigDecimal giaVeGoc) {
        this.giaVeGoc = giaVeGoc;
    }

    public BigDecimal getGiaVeSauGiam() {
        return this.giaVeSauGiam;
    }

    public void setGiaVeSauGiam(BigDecimal giaVeSauGiam) {
        this.giaVeSauGiam = giaVeSauGiam;
    }

    public String getTenPhim() {
        return this.tenPhim;
    }

    public void setTenPhim(String tenPhim) {
        this.tenPhim = tenPhim;
    }

    public LocalDateTime getNgayGioChieu() {
        return this.ngayGioChieu;
    }

    public void setNgayGioChieu(LocalDateTime ngayGioChieu) {
        this.ngayGioChieu = ngayGioChieu;
    }

    public String getTenKhuyenMai() {
        return this.tenKhuyenMai;
    }

    public void setTenKhuyenMai(String tenKhuyenMai) {
        this.tenKhuyenMai = tenKhuyenMai;
    }

    public BigDecimal getTienGiam() {
        return this.tienGiam;
    }

    public void setTienGiam(BigDecimal tienGiam) {
        this.tienGiam = tienGiam;
    }

    public String getTenKhachHang() {
        return this.tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }
}