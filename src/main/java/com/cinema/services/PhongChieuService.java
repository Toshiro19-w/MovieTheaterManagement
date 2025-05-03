package com.cinema.services;

import com.cinema.models.PhongChieu;
import com.cinema.models.repositories.PhongChieuRepository;
import com.cinema.utils.DatabaseConnection;

import java.sql.SQLException;
import java.util.List;

public class PhongChieuService {
    private final PhongChieuRepository phongChieuRepository;

    public PhongChieuService(DatabaseConnection databaseConnection) {
        this.phongChieuRepository = new PhongChieuRepository(databaseConnection);
    }

    public List<PhongChieu> getAllPhongChieu() throws SQLException {
        return phongChieuRepository.findAll();
    }
}