package com.cinema.controllers;

import com.cinema.models.KhachHang;
import com.cinema.services.KhachHangService;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class KhachHangController extends Component {
    private final KhachHangService khachHangService;

    public KhachHangController(KhachHangService khachHangService) {
        this.khachHangService = khachHangService;
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
}