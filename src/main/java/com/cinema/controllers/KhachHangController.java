package com.cinema.controllers;

import java.awt.Component;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JOptionPane;

import com.cinema.models.KhachHang;
import com.cinema.services.KhachHangService;

public class KhachHangController extends Component {
    private final KhachHangService khachHangService;

    public KhachHangController(KhachHangService khachHangService) {
        this.khachHangService = khachHangService;
    }

    public List<KhachHang> findAllKhachHang() throws SQLException {
        return khachHangService.findAllKhachHang();
    }

    public KhachHang getKhachHangByUsername(String username) throws SQLException {
        return khachHangService.getKhachHangByUsername(username);
    }

    public int getMaKhachHangFromSession(String username) throws SQLException {
        try {
            return khachHangService.getMaKhachHangFromSession(username);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lấy thông tin khách hàng: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException("Lỗi khi lấy maKhachHang: " + e.getMessage(), e);
        }
    }

    public List<KhachHang> searchKhachHang(String keyword) throws SQLException {
        return khachHangService.searchKhachHang(keyword);
    }

    public List<KhachHang> findRecentKhachHang(int limit) throws SQLException {
        return khachHangService.findRecentKhachHang(limit);
    }
}