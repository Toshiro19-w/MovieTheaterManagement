package com.cinema.services;

import com.cinema.models.SuatChieu;
import com.cinema.repositories.SuatChieuRepository;
import com.cinema.utils.DatabaseConnection;

import java.sql.SQLException;
import java.util.List;

public class SuatChieuService {
    private final SuatChieuRepository suatChieuRepository;

    public SuatChieuService(DatabaseConnection databaseConnection){
        this.suatChieuRepository = new SuatChieuRepository(databaseConnection);
    }

    public List<SuatChieu> getAllSuatChieu() throws SQLException {
        return suatChieuRepository.findAll();
    }

    public List<SuatChieu> getAllSuatChieuDetail() throws SQLException {
        return suatChieuRepository.findAllDetail();
    }

    public SuatChieu getSuatChieuById(int maSuatChieu) throws SQLException {
        return suatChieuRepository.findById(maSuatChieu);
    }

    public SuatChieu addSuatChieu(SuatChieu suatChieu) throws SQLException {
        return suatChieuRepository.save(suatChieu);
    }

    public SuatChieu updateSuatChieu(SuatChieu suatChieu) throws SQLException {
        return suatChieuRepository.update(suatChieu);
    }

    public void deleteSuatChieu(int maSuatChieu) throws SQLException {
        suatChieuRepository.delete(maSuatChieu);
    }
}
