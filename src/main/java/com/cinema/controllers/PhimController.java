package com.cinema.controllers;

import com.cinema.models.Phim;
import com.cinema.services.PhimService;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class PhimController {
    private final PhimService phimService;

    public PhimController(PhimService phimService) {
        this.phimService = phimService;
    }

    public List<Phim> findAll() {
        try {
            return phimService.getAllPhim();
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm phim: " + e.getMessage());
            return null;
        }
    }

    public List<Phim> findAllDetail() {
        try {
            return phimService.getAllPhimDetail();
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm phim: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    // Chưa làm sidebar cho tìm kiếm
    public Phim timPhimTheoId(int maPhim) {
        try {
            return phimService.getPhimById(maPhim);
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm phim: " + e.getMessage());
            return null;
        }
    }

    public Phim savePhim(Phim phim) {
        try {
//            if (!ValidationUtils.validatePhim(phim)) {
//                System.out.println("Dữ liệu phim không hợp lệ.");
//                return null;
//            }
            return phimService.addPhim(phim);
        } catch (SQLException e) {
            System.err.println("Lỗi khi lưu phim: " + e.getMessage());
            return null;
        }
    }

    public Phim updatePhim(Phim phim) {
        try {
//            if (!ValidationUtils.validatePhongChieu(phongChieu)) {
//                System.out.println("Dữ liệu phòng chiếu không hợp lệ.");
//                return null;
//            }
            return phimService.updatePhim(phim);
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật phim: " + e.getMessage());
            return null;
        }
    }

    public boolean deletePhim(int maPhim) {
        try {
            phimService.deletePhim(maPhim);
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa phim: " + e.getMessage());
            return false;
        }
    }
}
