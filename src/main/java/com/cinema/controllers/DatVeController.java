package com.cinema.controllers;

import com.cinema.models.*;
import com.cinema.services.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class DatVeController {
    private final SuatChieuService suatChieuService;
    private final GheService gheService;
    private final VeService veService;

    public DatVeController(SuatChieuService suatChieuService, GheService gheService, VeService veService) {
        this.suatChieuService = suatChieuService;
        this.gheService = gheService;
        this.veService = veService;
    }

    public List<SuatChieu> getSuatChieuByPhim(int maPhim) throws SQLException {
        return suatChieuService.getSuatChieuByMaPhim(maPhim);
    }

    public List<Ghe> getGheTrongByPhongAndSuatChieu(int maPhong, int maSuatChieu) throws SQLException {
        return gheService.findGheTrongByPhongAndSuatChieu(maPhong, maSuatChieu);
    }

    public List<Ghe> getAllGheByPhong(int maPhong) throws SQLException {
        return gheService.findAllGheByPhong(maPhong);
    }

    public BigDecimal getTicketPriceBySuatChieu(int maSuatChieu) throws SQLException {
        return veService.getTicketPriceBySuatChieu(maSuatChieu);
    }

    public void datVe(int maSuatChieu, Integer maPhong, String soGhe, BigDecimal giaVe, int maKhachHang, int maNhanVien) throws SQLException {
        veService.datVe(maSuatChieu, maPhong, soGhe, giaVe, maKhachHang, maNhanVien);
    }
    
    public void createPendingVe(int maSuatChieu, Integer maPhong, String soGhe, BigDecimal giaVe, int maKhachHang) throws SQLException {
        veService.createPendingVe(maSuatChieu, maPhong, soGhe, giaVe, maKhachHang);
    }

    public int confirmPayment(int maVe, int maKhachHang, int maNhanVien) throws SQLException {
        return veService.confirmPayment(maVe, maKhachHang, maNhanVien);
    }

    public int getMaVeFromBooking(int maSuatChieu, String soGhe, int maKhachHang) throws SQLException {
        return veService.getMaVeFromBooking(maSuatChieu, soGhe, maKhachHang);
    }
    
    public int getPendingVeFromBooking(int maSuatChieu, String soGhe, int maKhachHang) throws SQLException {
        return veService.getPendingVeFromBooking(maSuatChieu, soGhe, maKhachHang);
    }

    public void cancelVe(int maVe) throws SQLException {
        veService.cancelVe(maVe);
    }
}