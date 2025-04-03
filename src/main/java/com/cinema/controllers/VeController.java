package com.cinema.controllers;

import com.cinema.models.TrangThaiVe;
import com.cinema.models.Ve;
import com.cinema.services.VeService;
import com.cinema.utils.ValidationUtils;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class VeController {
    private final VeService veService;

    public VeController(VeService veService) {
        this.veService = veService;
    }

    public List<Ve> findAll() {
        try {
            return veService.findAll();
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm vé: " + e.getMessage());
            return null;
        }
    }

    public Ve findVeById(int maVe) {
        try {
            return veService.findById(maVe);
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm vé: " + e.getMessage());
            return null;
        }
    }
//
//    public List<Ve> findVeByMaSuatChieu(int maSuatChieu) {
//        try {
//            return veService.findByMaSuatChieu(maSuatChieu);
//        } catch (SQLException e) {
//            System.err.println("Lỗi khi tìm vé theo mã suất chiếu: " + e.getMessage());
//            return null;
//        }
//    }
//
//    public List<Ve> findVeByMaKhachHang(int maKhachHang, int page, int pageSize) {
//        try {
//            return veService.findByMaKhachHang(maKhachHang, page, pageSize);
//        } catch (SQLException e) {
//            System.err.println("Lỗi khi tìm vé theo mã khách hàng: " + e.getMessage());
//            return null;
//        }
//    }

    public Ve saveVe(Ve ve) {
        try {
            if (!ValidationUtils.validateVe(ve)) {
                System.out.println("Dữ liệu vé không hợp lệ.");
                return null;
            }
            return veService.save(ve);
        } catch (SQLException e) {
            System.err.println("Lỗi khi lưu vé: " + e.getMessage());
            return null;
        }
    }

    public Ve updateVe(Ve ve) {
        try {
            return veService.update(ve);
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật vé: " + e.getMessage());
            return null;
        }
    }

    public boolean deleteVe(int maVe) {
        try {
            veService.delete(maVe);
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa vé: " + e.getMessage());
        }
        return false;
    }
}