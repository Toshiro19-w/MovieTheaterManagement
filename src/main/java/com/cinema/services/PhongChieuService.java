package com.cinema.services;

import com.cinema.models.PhongChieu;
import com.cinema.repositories.PhongChieuRepository;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class PhongChieuService {
    private final PhongChieuRepository phongChieuRepository;

    public PhongChieuService(Connection conn) {
        this.phongChieuRepository = new PhongChieuRepository(conn);
    }

    public List<PhongChieu> getAllPhongChieu() throws SQLException {
        return phongChieuRepository.findAll();
    }

    public PhongChieu getPhongChieuById(int maPhong) throws SQLException {
        return phongChieuRepository.findById(maPhong);
    }

    public PhongChieu addPhongChieu(PhongChieu phongChieu) throws SQLException {
        return phongChieuRepository.save(phongChieu);
    }

    public PhongChieu updatePhongChieu(PhongChieu phongChieu) throws SQLException {
        return phongChieuRepository.update(phongChieu);
    }

    public void deletePhongChieu(int maPhong) throws SQLException {
        phongChieuRepository.delete(maPhong);
    }

    public boolean isPhongChieuExist(int maPhong) throws SQLException {
        return phongChieuRepository.findById(maPhong) != null;
    }
}