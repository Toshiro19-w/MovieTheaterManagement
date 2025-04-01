package com.cinema.controllers;

import com.cinema.models.Phim;
import com.cinema.services.PhimService;

import java.util.List;
import java.util.Optional;

public class PhimController {
    private final PhimService phimService;

    public PhimController() {
        this.phimService = new PhimService();
    }

    public List<Phim> hienThiTatCaPhim() {
        List<Phim> danhSachPhim = phimService.getAllPhim(1, 10);
        danhSachPhim.forEach(phim -> System.out.println(phim));
        return danhSachPhim;
    }

    public void timPhimTheoId(int maPhim) {
        Optional<Phim> phim = phimService.getPhimById(maPhim);
        phim.ifPresentOrElse(
                System.out::println,
                () -> System.out.println("Không tìm thấy phim có mã: " + maPhim)
        );
    }

    public void timPhimTheoTen(String keyword) {
        List<Phim> danhSachPhim = phimService.searchPhimByName(keyword);
        danhSachPhim.forEach(System.out::println);
    }

    public void xoaPhim(int maPhim) {
        boolean deleted = phimService.deletePhim(maPhim);
        if (deleted) {
            System.out.println("Xóa phim thành công!");
        } else {
            System.out.println("Không thể xóa phim.");
        }
    }
}
