package com.cinema.services;

import com.cinema.models.HoaDon;
import com.cinema.repositories.ChiTietHoaDonRepository;
import com.cinema.repositories.HoaDonRepository;
import com.cinema.utils.DatabaseConnection;

import java.sql.SQLException;
import java.util.List;

public class HoaDonService {
    private final HoaDonRepository hoaDonRepository;
    private final ChiTietHoaDonRepository chiTietHoaDonRepository;

    public HoaDonService(DatabaseConnection databaseConnection) {
        this.hoaDonRepository = new HoaDonRepository(databaseConnection);
        this.chiTietHoaDonRepository = new ChiTietHoaDonRepository(databaseConnection);
    }

    public List<HoaDon> getLichSuHoaDonByTenKhachHang(String tenKhachHang) throws SQLException {
        return hoaDonRepository.getHoaDonByTenKhachHang(tenKhachHang);
    }

    public List<HoaDon> findByKhachHang(int maKhachHang) throws SQLException {
        return hoaDonRepository.findByKhachHang(maKhachHang);
    }

    public void createChiTietHoaDon(int maHoaDon, int maVe) throws SQLException {
        chiTietHoaDonRepository.createChiTietHoaDon(maHoaDon, maVe);
    }

    public List<String> getAllTenKhachHang() throws SQLException {
        return hoaDonRepository.getAllTenKhachHang();
    }
}
