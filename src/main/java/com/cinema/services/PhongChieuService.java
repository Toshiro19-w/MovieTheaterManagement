package com.cinema.services;

import com.cinema.models.PhongChieu;
import com.cinema.repositories.PhongChieuRepository;
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

    public List<PhongChieu> searchPhongChieuByTenPhong(String tenPhong) throws SQLException {
        if (tenPhong == null || tenPhong.trim().isEmpty()) {
            return getAllPhongChieu();
        }
        return phongChieuRepository.findByTenPhong(tenPhong);
    }

    public PhongChieu addPhongChieu(PhongChieu phongChieu) throws SQLException {
        if (phongChieu.getSoLuongGhe() <= 0 || phongChieu.getLoaiPhong() == null || phongChieu.getLoaiPhong().trim().isEmpty()) {
            throw new IllegalArgumentException("Dữ liệu phòng chiếu không hợp lệ: số lượng ghế phải lớn hơn 0 và loại phòng không được để trống.");
        }
        return phongChieuRepository.save(phongChieu);
    }
}