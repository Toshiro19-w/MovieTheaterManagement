package com.cinema.services;

import com.cinema.models.SuatChieu;
import com.cinema.models.repositories.SuatChieuRepository;
import com.cinema.utils.DatabaseConnection;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class SuatChieuService {
    private final SuatChieuRepository suatChieuRepository;

    public SuatChieuService(DatabaseConnection databaseConnection) {
        this.suatChieuRepository = new SuatChieuRepository(databaseConnection);
    }

    public List<SuatChieu> getAllSuatChieu() throws SQLException {
        return suatChieuRepository.findAll();
    }

    public List<SuatChieu> getSuatChieuByMaPhim(int maPhim) throws SQLException {
        return suatChieuRepository.findByMaPhim(maPhim);
    }

    public void addSuatChieu(SuatChieu suatChieu) throws SQLException {
        if (suatChieu.getMaPhim() <= 0 || suatChieu.getMaPhong() <= 0 || suatChieu.getNgayGioChieu() == null) {
            throw new IllegalArgumentException("Dữ liệu suất chiếu không hợp lệ.");
        }
        suatChieuRepository.save(suatChieu);
    }

    public void updateSuatChieu(SuatChieu suatChieu) throws SQLException {
        if (suatChieu.getMaSuatChieu() <= 0 || suatChieu.getMaPhim() <= 0 || suatChieu.getMaPhong() <= 0 || suatChieu.getNgayGioChieu() == null) {
            throw new IllegalArgumentException("Dữ liệu suất chiếu không hợp lệ.");
        }
        suatChieuRepository.update(suatChieu);
    }

    public void deleteSuatChieu(int maSuatChieu) throws SQLException {
        if (maSuatChieu <= 0) {
            throw new IllegalArgumentException("Mã suất chiếu không hợp lệ.");
        }
        suatChieuRepository.delete(maSuatChieu);
    }
}