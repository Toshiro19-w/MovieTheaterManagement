package com.cinema.services;

import com.cinema.models.Ve;
import com.cinema.repositories.VeRepository;
import com.cinema.utils.DatabaseConnection;

import java.sql.SQLException;
import java.util.List;

public class VeService{

    private final VeRepository veRepository;

    public VeService(DatabaseConnection databaseConnection) {
        this.veRepository = new VeRepository(databaseConnection);
    }

    public List<Ve> getAllVe() throws SQLException {
        return veRepository.findAll();
    }

    public List<Ve> getAllVeDetail() throws SQLException {
        return veRepository.findAllDetail();
    }

    public Ve getVeById(int maVe) throws SQLException {
        return veRepository.findById(maVe);
    }

    public List<Ve> findByHoaDon(Integer maHoaDon) throws SQLException {
        return veRepository.findByHoaDon(maHoaDon);
    }
//
//    public List<Ve> findByMaSuatChieu(int maSuatChieu) throws SQLException {
//        return veRepository.findByMaSuatChieu(maSuatChieu);
//    }
//
//    public List<Ve> findByMaKhachHang(Integer maKhachHang, int page, int pageSize) throws SQLException {
//        return veRepository.findByMaKhachHang(maKhachHang, page, pageSize);
//    }
//
//    public List<Ve> findByMaHoaDon(Integer maHoaDon) throws SQLException {
//        return veRepository.findByMaHoaDon(maHoaDon);
//    }
//
//    public List<Ve> findByTrangThai(TrangThaiVe trangThai, int page, int pageSize) throws SQLException {
//        return veRepository.findByTrangThai(trangThai, page, pageSize);
//    }
//
//    public List<Ve> findByNgayDat(LocalDate ngayDat) throws SQLException {
//        return veRepository.findByNgayDat(ngayDat);
//    }
//
//    public Ve findVeChiTietByMaVe(int maVe) throws SQLException {
//        return veRepository.findVeChiTietByMaVe(maVe);
//    }

    public Ve saveVe(Ve ve) throws SQLException {
        return veRepository.save(ve);
    }

    public Ve updateVe(Ve ve) throws SQLException {
        return veRepository.update(ve);
    }

    public void deleteVe(int maVe) throws SQLException {
        veRepository.delete(maVe);
    }

    public void updateVeStatus(int maVe, String trangThai, Integer maHoaDon) throws SQLException {
        updateVeStatus(maVe, trangThai, maHoaDon);
    }
}