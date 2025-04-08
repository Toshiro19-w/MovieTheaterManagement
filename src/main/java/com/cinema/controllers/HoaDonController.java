package com.cinema.controllers;

import com.cinema.models.HoaDon;
import com.cinema.models.Ve;
import com.cinema.services.HoaDonService;
import com.cinema.services.VeService;

import java.sql.SQLException;
import java.util.List;

public class HoaDonController {
    private final HoaDonService hoaDonService;
    private final VeService veService;

    public HoaDonController(HoaDonService hoaDonService, VeService veService) {
        this.hoaDonService = hoaDonService;
        this.veService = veService;
    }

    public List<HoaDon> getLichSuHoaDon(int maKhachHang) throws SQLException {
        return hoaDonService.findByKhachHang(maKhachHang);
    }

    public List<HoaDon> getLichSuHoaDonByTenKhachHang(String tenKhachHang) throws SQLException {
        return hoaDonService.getLichSuHoaDonByTenKhachHang(tenKhachHang);
    }

    public List<Ve> getVeByHoaDon(int maHoaDon) throws SQLException {
        return veService.findByHoaDon(maHoaDon);
    }

    public List<String> getAllTenKhachHang() throws SQLException {
        return hoaDonService.getAllTenKhachHang();
    }
}