package com.cinema.services;

import com.cinema.models.KhachHang;
import com.cinema.models.Ve;
import com.cinema.models.repositories.KhachHangRepository;
import com.cinema.models.repositories.VeRepository;
import com.cinema.utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class VeService{

    private final VeRepository veRepository;
    private final KhachHangRepository khachHangRepository;

    public VeService(DatabaseConnection databaseConnection) {
        this.veRepository = new VeRepository(databaseConnection);
        this.khachHangRepository = new KhachHangRepository(databaseConnection);
    }

    public List<Ve> getAllVeDetail() throws SQLException {
        return veRepository.findAllDetail();
    }

    public List<Ve> findBySoGhe(String soGhe) throws SQLException {
        return veRepository.findBySoGhe(soGhe);
    }

    public Ve saveVe(Ve ve) throws SQLException {
        return veRepository.save(ve);
    }

    public Ve updateVe(Ve ve) throws SQLException {
        return veRepository.update(ve);
    }

    public void deleteVe(int maVe) throws SQLException {
        veRepository.delete(maVe);
    }

    public void datVe(int maSuatChieu, Integer maPhong, String soGhe, BigDecimal giaVe, int maKhachHang) throws SQLException {
        veRepository.datVe(maSuatChieu, maPhong, soGhe, giaVe, maKhachHang);
    }

    public KhachHang getKhachHangByMaVe(int maVe) throws SQLException {
        return khachHangRepository.getKhachHangByMaVe(maVe);
    }

    public BigDecimal getTicketPriceBySuatChieu(int maSuatChieu) throws SQLException {
        return veRepository.findTicketPriceBySuatChieu(maSuatChieu);
    }

    public void confirmPayment(int maVe, int maHoaDon) throws SQLException {
        veRepository.confirmPayment(maVe, maHoaDon);
    }

    public int getMaVeFromBooking(int maSuatChieu, String soGhe, int maKhachHang) throws SQLException {
        return veRepository.getMaVeFromBooking(maSuatChieu, soGhe, maKhachHang);
    }

    public int getMaHoaDonFromVe(int maVe) throws SQLException {
        return veRepository.getMaHoaDonFromVe(maVe);
    }
}