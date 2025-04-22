package com.cinema.models.repositories.Interface;

import com.cinema.models.ChiTietHoaDon;
import com.cinema.models.HoaDon;

import java.sql.*;
import java.util.List;

public interface IHoaDonRepository {
    List<HoaDon> findAll() throws SQLException;
    List<HoaDon> search(String id, String idKhachHang, String tenKhachHang) throws SQLException;
    List<ChiTietHoaDon> findChiTietByMaHoaDon(int maHoaDon) throws SQLException;
}
