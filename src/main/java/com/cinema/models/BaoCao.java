package com.cinema.models;

public class BaoCao {
    private String tenPhim;
    private int soVeBanRa;
    private double tongDoanhThu;

    public BaoCao(String tenPhim, int soVeBanRa, double tongDoanhThu) {
        this.tenPhim = tenPhim;
        this.soVeBanRa = soVeBanRa;
        this.tongDoanhThu = tongDoanhThu;
    }

    // Getters và Setters
    public String getTenPhim() { return tenPhim; }
    public void setTenPhim(String tenPhim) { this.tenPhim = tenPhim; }
    public int getSoVeBanRa() { return soVeBanRa; }
    public void setSoVeBanRa(int soVeBanRa) { this.soVeBanRa = soVeBanRa; }
    public double getTongDoanhThu() { return tongDoanhThu; }
    public void setTongDoanhThu(double tongDoanhThu) { this.tongDoanhThu = tongDoanhThu; }

    @Override
    public String toString() {
        return "BaoCao{" +
                "Tên phim=: '" + tenPhim + '\'' +
                ", Số vé bán ra: " + soVeBanRa +
                ", tổng doanh thu: " + tongDoanhThu +
                '}';
    }
}