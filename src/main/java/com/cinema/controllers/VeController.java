package com.cinema.controllers;

import com.cinema.models.Ve;
import com.cinema.services.VeService;

import java.util.List;
import java.util.Optional;

public class VeController {
    private final VeService veService;

    public VeController() {
        this.veService = new VeService();
    }

    public List<Ve> hienThiTatCaVe() {
        List<Ve> danhSachPhim = veService.getAllVe(1, 10);
        danhSachPhim.forEach(System.out::println);
        return danhSachPhim;
    }

    public Ve save(Ve ve) {
        try {
            // Validate dữ liệu trước khi lưu
            if (!validateVe(ve)) {
                return null;
            }

            // Nếu vé đã có mã (đang update)
            if (ve.getMaVe() > 0) {
                // Kiểm tra vé có tồn tại không
                Optional<Ve> existingVe = veService.getVeById(ve.getMaVe());
                if (existingVe.isEmpty()) {
                    System.out.println("Không tìm thấy vé có mã: " + ve.getMaVe());
                    return null;
                }
            }

            // Thực hiện lưu vé
            Ve savedVe = veService.addOrUpdateVe(ve);

            if (savedVe != null) {
                System.out.println("Lưu vé thành công! Mã vé: " + savedVe.getMaVe());
            } else {
                System.out.println("Lưu vé thất bại!");
            }

            return savedVe;
        } catch (Exception e) {
            System.err.println("Lỗi khi lưu vé: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private boolean validateVe(Ve ve) {
        if (ve.getMaSuatChieu() <= 0) {
            System.out.println("Mã suất chiếu không hợp lệ");
            return false;
        }

        if (ve.getSoGhe() == null || ve.getSoGhe().trim().isEmpty()) {
            System.out.println("Số ghế không được để trống");
            return false;
        }

        if (ve.getGiaVe() < 0) {
            System.out.println("Giá vé không hợp lệ");
            return false;
        }

        if (ve.getTrangThai() == null || !List.of("available", "booked", "paid", "cancelled").contains(ve.getTrangThai())) { //lỗi ở dòng này
            System.out.println("Trạng thái vé không hợp lệ");
            return false;
        }

        return true;
    }

    public void timPhimTheoId(int maPhim) {
        Optional<Ve> phim = veService.getVeById(maPhim);
        phim.ifPresentOrElse(
                System.out::println,
                () -> System.out.println("Không tìm thấy phim có mã: " + maPhim)
        );
    }

    public void xoaPhim(int maPhim) {
        boolean deleted = veService.deletePhim(maPhim);
        if (deleted) {
            System.out.println("Xóa phim thành công!");
        } else {
            System.out.println("Không thể xóa phim.");
        }
    }
}
