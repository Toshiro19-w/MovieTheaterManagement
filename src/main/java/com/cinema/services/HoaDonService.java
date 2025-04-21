package com.cinema.services;

import com.cinema.models.HoaDon;
import com.cinema.models.ChiTietHoaDon;
import com.cinema.repositories.HoaDonRepository;
import com.cinema.utils.DatabaseConnection;

import java.sql.SQLException;
import java.util.List;

public class HoaDonService {
    private final HoaDonRepository hoaDonRepository;

    public HoaDonService(DatabaseConnection databaseConnection) {
        this.hoaDonRepository = new HoaDonRepository(databaseConnection);
    }

    public List<HoaDon> getAllHoaDon() throws SQLException {
        return hoaDonRepository.findAll();
    }

    public List<HoaDon> searchHoaDon(String id, String idKhachHang, String tenKhachHang) throws SQLException {
        return hoaDonRepository.search(id, idKhachHang, tenKhachHang);
    }

    public List<ChiTietHoaDon> getChiTietHoaDon(int maHoaDon) throws SQLException {
        return hoaDonRepository.findChiTietByMaHoaDon(maHoaDon);
    }
}