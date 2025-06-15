package com.cinema.controllers;

import java.sql.SQLException;
import java.util.List;

import com.cinema.models.DanhGia;
import com.cinema.services.DanhGiaService;

public class DanhGiaController {
    private final DanhGiaService danhGiaService;

    public DanhGiaController(DanhGiaService danhGiaService) {
        this.danhGiaService = danhGiaService;
    }

    public List<DanhGia> getDanhGiaByPhimId(int maPhim, int limit) throws SQLException {
        return danhGiaService.getDanhGiaByPhimId(maPhim, limit);
    }

    public boolean daXemPhim(int maKhachHang, int maPhim) throws SQLException {
        return danhGiaService.daXemPhim(maKhachHang, maPhim);
    }

    public boolean daDanhGia(int maKhachHang, int maPhim) throws SQLException {
        return danhGiaService.daDanhGia(maKhachHang, maPhim);
    }

    public int themDanhGia(DanhGia danhGia) throws SQLException {
        return danhGiaService.themDanhGia(danhGia);
    }
    
    public boolean capNhatDanhGia(DanhGia danhGia) throws SQLException {
        return danhGiaService.capNhatDanhGia(danhGia);
    }

    public double getDiemDanhGiaTrungBinh(int maPhim) throws SQLException {
        return danhGiaService.getDiemDanhGiaTrungBinh(maPhim);
    }
    
    public int getMaVeDaMua(int maKhachHang, int maPhim) throws SQLException {
        return danhGiaService.getMaVeDaMua(maKhachHang, maPhim);
    }
    
    public DanhGia getDanhGiaById(int maDanhGia) throws SQLException {
        return danhGiaService.getDanhGiaById(maDanhGia);
    }
    
    public DanhGia getDanhGiaByUserAndPhim(int maNguoiDung, int maPhim) throws SQLException {
        return danhGiaService.getDanhGiaByUserAndPhim(maNguoiDung, maPhim);
    }
}