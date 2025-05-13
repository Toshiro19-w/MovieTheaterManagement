package com.cinema.services;

import java.sql.SQLException;
import java.util.List;

import com.cinema.models.KhachHang;
import com.cinema.models.repositories.KhachHangRepository;
import com.cinema.utils.DatabaseConnection;

public class KhachHangService {
    private final KhachHangRepository khachHangRepository;

    public KhachHangService(DatabaseConnection databaseConnection) {
        this.khachHangRepository = new KhachHangRepository(databaseConnection);
    }

    public List<KhachHang> findAllKhachHang() throws SQLException {
        return khachHangRepository.findAll();
    }

    public KhachHang getKhachHangByUsername(String username) throws SQLException {
        return khachHangRepository.getKhachHangByUsername(username);
    }

    public int getMaKhachHangFromSession(String username) throws SQLException {
        return khachHangRepository.getMaKhachHangFromSession(username);
    }

    public List<KhachHang> searchKhachHang(String keyword) throws SQLException {
        return khachHangRepository.searchKhachHang(keyword);
    }

    public List<KhachHang> findRecentKhachHang(int limit) throws SQLException {
        return khachHangRepository.findRecentKhachHang(limit);
    }
}
