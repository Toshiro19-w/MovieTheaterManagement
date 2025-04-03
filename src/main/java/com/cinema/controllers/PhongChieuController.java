package com.cinema.controllers;

import com.cinema.models.PhongChieu;
import com.cinema.services.PhongChieuService;
import com.cinema.utils.ValidationUtils;

import java.sql.SQLException;
import java.util.List;

public class PhongChieuController {
    private final PhongChieuService phongChieuService;

    public PhongChieuController(PhongChieuService phongChieuService) {
        this.phongChieuService = phongChieuService;
    }

    public List<PhongChieu> findAll() {
        try {
            return phongChieuService.getAllPhongChieu();
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm phòng chiếu: " + e.getMessage());
            return null;
        }
    }

    public PhongChieu findById(int maPhong) {
        try {
            return phongChieuService.getPhongChieuById(maPhong);
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm phòng chiếu: " + e.getMessage());
            return null;
        }
    }

    public PhongChieu save(PhongChieu phongChieu) {
        try {
//            if (!ValidationUtils.validatePhongChieu(phongChieu)) {
//                System.out.println("Dữ liệu phòng chiếu không hợp lệ.");
//                return null;
//            }
            return phongChieuService.addPhongChieu(phongChieu);
        } catch (SQLException e) {
            System.err.println("Lỗi khi lưu phòng chiếu: " + e.getMessage());
            return null;
        }
    }

    public PhongChieu update(PhongChieu phongChieu) {
        try {
//            if (!ValidationUtils.validatePhongChieu(phongChieu)) {
//                System.out.println("Dữ liệu phòng chiếu không hợp lệ.");
//                return null;
//            }
            return phongChieuService.updatePhongChieu(phongChieu);
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật phòng chiếu: " + e.getMessage());
            return null;
        }
    }

    public boolean delete(int maPhong) {
        try {
            phongChieuService.deletePhongChieu(maPhong);
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa phòng chiếu: " + e.getMessage());
            return false;
        }
    }

    public boolean exists(int maPhong) {
        try {
            return phongChieuService.isPhongChieuExist(maPhong);
        } catch (SQLException e) {
            System.err.println("Lỗi khi kiểm tra phòng chiếu: " + e.getMessage());
            return false;
        }
    }
}