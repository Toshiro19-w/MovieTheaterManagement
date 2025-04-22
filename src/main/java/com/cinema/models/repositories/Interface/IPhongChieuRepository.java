package com.cinema.models.repositories.Interface;

import com.cinema.models.PhongChieu;

import java.sql.SQLException;
import java.util.List;

public interface IPhongChieuRepository {
    List<PhongChieu> findAll() throws SQLException;
    List<PhongChieu> findByTenPhong(String tenPhong) throws SQLException;
    PhongChieu save(PhongChieu entity) throws SQLException;
}
