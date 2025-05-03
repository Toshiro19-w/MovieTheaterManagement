package com.cinema.controllers;

import com.cinema.models.KhachHang;
import com.cinema.services.KhachHangService;

import java.sql.SQLException;

public class KhachHangController {
    private final KhachHangService khachHangService;

    public KhachHangController(KhachHangService khachHangService) {
        this.khachHangService = khachHangService;
    }

    public KhachHang getKhachHangByUsername(String username) throws SQLException {
        return khachHangService.getKhachHangByUsername(username);
    }
}