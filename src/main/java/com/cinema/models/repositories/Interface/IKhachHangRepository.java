package com.cinema.models.repositories.Interface;

import java.sql.SQLException;
import java.util.List;

import com.cinema.models.KhachHang;

public interface IKhachHangRepository {
    //Lấy thông tin khách hàng qua maHoaDon
    KhachHang getKhachHangByMaVe(int maVe) throws SQLException;

    KhachHang getKhachHangByUsername(String username) throws SQLException;

    int getMaKhachHangFromSession(String username) throws SQLException;

    List<KhachHang> findAll() throws SQLException;

    List<KhachHang> searchKhachHang(String keyword) throws SQLException;

    List<KhachHang> findRecentKhachHang(int limit) throws SQLException;
}
