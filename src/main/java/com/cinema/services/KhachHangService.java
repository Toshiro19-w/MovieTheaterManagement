package com.cinema.services;

import com.cinema.models.KhachHang;
import com.cinema.models.repositories.KhachHangRepository;
import com.cinema.utils.DatabaseConnection;

import java.sql.SQLException;

public class KhachHangService {
    private final KhachHangRepository khachHangRepository;

    public KhachHangService(DatabaseConnection databaseConnection) {
        this.khachHangRepository = new KhachHangRepository(databaseConnection);
    }

    public KhachHang getKhachHangByUsername(String username) throws SQLException {
        return khachHangRepository.getKhachHangByUsername(username);
    }

    public int getMaKhachHangFromSession(String username) throws SQLException {
        return khachHangRepository.getMaKhachHangFromSession(username);
    }
}
