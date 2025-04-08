package com.cinema.repositories.Interface;

import com.cinema.models.HoaDon;

import java.sql.SQLException;
import java.util.List;

public interface IHoaDonRepository {
    List<HoaDon> getHoaDonByTenKhachHang(String tenKhachHang) throws SQLException;
    List<HoaDon> findByKhachHang(int maKhachHang) throws SQLException;
    List<String> getAllTenKhachHang() throws SQLException;
}
