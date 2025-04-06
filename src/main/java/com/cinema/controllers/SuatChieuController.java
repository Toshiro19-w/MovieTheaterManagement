package com.cinema.controllers;

import com.cinema.models.SuatChieu;
import com.cinema.services.SuatChieuService;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class SuatChieuController {
    private final SuatChieuService suatChieuService;

    public SuatChieuController(SuatChieuService suatChieuService) {
        this.suatChieuService = suatChieuService;
    }

    public List<SuatChieu> findAll() {
        try {
            return suatChieuService.getAllSuatChieu();
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm phim: " + e.getMessage());
            return null;
        }
    }

    public List<SuatChieu> findAllDetail() {
        try {
            return suatChieuService.getAllSuatChieuDetail();
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm phim: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public SuatChieu findSuatChieuById(int maSuatChieu) {
        try {
            return suatChieuService.getSuatChieuById(maSuatChieu);
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm phim: " + e.getMessage());
            return null;
        }
    }

    public SuatChieu saveSuatChieu(SuatChieu suatChieu) {
        try {
//            if (!ValidationUtils.validatePhim(phim)) {
//                System.out.println("Dữ liệu phim không hợp lệ.");
//                return null;
//            }
            return suatChieuService.addSuatChieu(suatChieu);
        } catch (SQLException e) {
            System.err.println("Lỗi khi lưu phim: " + e.getMessage());
            return null;
        }
    }

    public SuatChieu updateSuatChieu(SuatChieu suatChieu) {
        try {
//            if (!ValidationUtils.validatePhongChieu(phongChieu)) {
//                System.out.println("Dữ liệu phòng chiếu không hợp lệ.");
//                return null;
//            }
            return suatChieuService.updateSuatChieu(suatChieu);
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật phim: " + e.getMessage());
            return null;
        }
    }

    public boolean deleteSuatChieu(int maSuatChieu) {
        try {
            suatChieuService.deleteSuatChieu(maSuatChieu);
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa phim: " + e.getMessage());
            return false;
        }
    }
}
