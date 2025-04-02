package com.cinema.services;

import com.cinema.models.TrangThaiVe;
import com.cinema.models.Ve;
import com.cinema.repositories.VeRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class VeService{

    private final VeRepository veRepository;

    public VeService() {
        this.veRepository = new VeRepository();
    }

    public List<Ve> findAll() throws SQLException {
        return veRepository.findAll();
    }

    public Ve findByMaVe(int maVe) throws SQLException {
        return veRepository.findByMaVe(maVe);
    }

    public List<Ve> findByMaSuatChieu(int maSuatChieu) throws SQLException {
        return veRepository.findByMaSuatChieu(maSuatChieu);
    }

    public List<Ve> findByMaKhachHang(Integer maKhachHang, int page, int pageSize) throws SQLException {
        return veRepository.findByMaKhachHang(maKhachHang, page, pageSize);
    }

    public List<Ve> findByMaHoaDon(Integer maHoaDon) throws SQLException {
        return veRepository.findByMaHoaDon(maHoaDon);
    }

    public List<Ve> findByTrangThai(TrangThaiVe trangThai, int page, int pageSize) throws SQLException {
        return veRepository.findByTrangThai(trangThai, page, pageSize);
    }

    public List<Ve> findByNgayDat(LocalDate ngayDat) throws SQLException {
        return veRepository.findByNgayDat(ngayDat);
    }

    public Ve findVeChiTietByMaVe(int maVe) throws SQLException {
        return veRepository.findVeChiTietByMaVe(maVe);
    }

    public Ve save(Ve ve) throws SQLException {
        return veRepository.save(ve);
    }

    public Ve update(Ve ve) throws SQLException {
        return veRepository.update(ve);
    }

    public void delete(int maVe) throws SQLException {
        veRepository.delete(maVe);
    }
}