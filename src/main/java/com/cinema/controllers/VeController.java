package com.cinema.controllers;

import com.cinema.models.KhachHang;
import com.cinema.models.Ve;
import com.cinema.services.VeService;
import com.cinema.utils.ValidationUtils;

import java.sql.SQLException;
import java.util.List;

public class VeController {
    private final VeService veService;

    public VeController(VeService veService) {
        this.veService = veService;
    }

    public List<Ve> findAll() {
        try {
            return veService.getAllVe();
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm vé: " + e.getMessage());
            return null;
        }
    }

    public List<Ve> findAllDetail() {
        try {
            return veService.getAllVeDetail();
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm vé: " + e.getMessage());
            return null;
        }
    }

    public List<KhachHang> findAllKhachHang() {
        try {
            return veService.getAllKhachHang();
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm khách hàng: " + e.getMessage());
            return null;
        }
    }

    public KhachHang getKhachHangByMaVe(int maVe) {
        try {
            return veService.getKhachHangByMaVe(maVe);
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm khách hàng: " + e.getMessage());
            return null;
        }
    }

    public List<Ve> searchVeBySoGhe(String soGhe){
        try {
            return veService.findBySoGhe(soGhe);
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm vé: " + e.getMessage());
            return null;
        }
    }

    public Ve saveVe(Ve ve) {
        try {
            if (!ValidationUtils.validateVe(ve)) {
                System.out.println("Dữ liệu vé không hợp lệ.");
                return null;
            }
            return veService.saveVe(ve);
        } catch (SQLException e) {
            System.err.println("Lỗi khi lưu vé: " + e.getMessage());
            return null;
        }
    }

    public Ve updateVe(Ve ve) {
        try {
            return veService.updateVe(ve);
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật vé: " + e.getMessage());
            return null;
        }
    }

    public boolean deleteVe(int maVe) {
        try {
            veService.deleteVe(maVe);
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa vé: " + e.getMessage());
        }
        return false;
    }
}