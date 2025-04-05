package com.cinema.services;

import com.cinema.models.HoaDon;
import com.cinema.repositories.ChiTietHoaDonRepository;
import com.cinema.repositories.HoaDonRepository;
import com.cinema.utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class HoaDonService {
    private final HoaDonRepository hoaDonRepository;
    private final ChiTietHoaDonRepository chiTietHoaDonRepository;

    public HoaDonService(DatabaseConnection databaseConnection) {
        this.hoaDonRepository = new HoaDonRepository(databaseConnection);
        this.chiTietHoaDonRepository = new ChiTietHoaDonRepository(databaseConnection);
    }

    public int createHoaDon(Integer maNhanVien, Integer maKhachHang, BigDecimal tongTien) throws SQLException {
        return hoaDonRepository.createHoaDon(maNhanVien, maKhachHang, tongTien);
    }

    public List<HoaDon> findByKhachHang(int maKhachHang) throws SQLException {
        return hoaDonRepository.findByKhachHang(maKhachHang);
    }

    public void createChiTietHoaDon(int maHoaDon, int maVe) throws SQLException {
        chiTietHoaDonRepository.createChiTietHoaDon(maHoaDon, maVe);
    }
}
