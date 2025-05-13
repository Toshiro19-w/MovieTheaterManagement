package com.cinema.models;

public class BaoCao {
    private String tenPhim;
    private int soVeBanRa;
    private double tongDoanhThu;
    private double diemDanhGiaTrungBinh;

    public BaoCao(String tenPhim, int soVeBanRa, double tongDoanhThu, double diemDanhGiaTrungBinh) {
        this.tenPhim = tenPhim;
        this.soVeBanRa = soVeBanRa;
        this.tongDoanhThu = tongDoanhThu;
        this.diemDanhGiaTrungBinh = diemDanhGiaTrungBinh;
    }

    public String getTenPhim() {
        return tenPhim;
    }

    public void setTenPhim(String tenPhim) {
        this.tenPhim = tenPhim;
    }

    public int getSoVeBanRa() {
        return soVeBanRa;
    }

    public void setSoVeBanRa(int soVeBanRa) {
        this.soVeBanRa = soVeBanRa;
    }

    public double getTongDoanhThu() {
        return tongDoanhThu;
    }

    public void setTongDoanhThu(double tongDoanhThu) {
        this.tongDoanhThu = tongDoanhThu;
    }

    public double getDiemDanhGiaTrungBinh() {
        return diemDanhGiaTrungBinh;
    }

    public void setDiemDanhGiaTrungBinh(double diemDanhGiaTrungBinh) {
        this.diemDanhGiaTrungBinh = diemDanhGiaTrungBinh;
    }

    @Override
    public String toString() {
        return "BaoCao{" +
                "tenPhim='" + tenPhim + '\'' +
                ", soVeBanRa=" + soVeBanRa +
                ", tongDoanhThu=" + tongDoanhThu +
                ", diemDanhGiaTrungBinh=" + diemDanhGiaTrungBinh +
                '}';
    }
}