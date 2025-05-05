package com.cinema.models.repositories.Interface;

import com.cinema.models.KhachHang;

import java.sql.SQLException;

public interface IKhachHangRepository {
    //Lấy thông tin khách hàng qua maHoaDon
    KhachHang getKhachHangByMaVe(int maVe) throws SQLException;

    KhachHang getKhachHangByUsername(String username) throws SQLException;

    int getMaKhachHangFromSession(String username) throws SQLException;
}
