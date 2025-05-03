package com.cinema.controllers;

import com.cinema.models.*;
import com.cinema.models.repositories.VeRepository;
import com.cinema.services.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

    public void datVe(int maSuatChieu, Integer maPhong, String soGhe, BigDecimal giaVe, int maKhachHang) throws SQLException {
        veService.datVe(maSuatChieu, maPhong, soGhe, giaVe, maKhachHang);
    }

    public void confirmPayment(int maVe, int maHoaDon) throws SQLException {
        veService.confirmPayment(maVe, maHoaDon);
    }

    public int getMaVeFromBooking(int maSuatChieu, String soGhe, int maKhachHang) throws SQLException {
        return veService.getMaVeFromBooking(maSuatChieu, soGhe, maKhachHang);
    }

    public int getMaHoaDonFromVe(int maVe) throws SQLException {
        return veService.getMaHoaDonFromVe(maVe);

    }
}