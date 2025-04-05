package com.cinema.controllers;

import com.cinema.models.NhanVien;
import com.cinema.services.NhanVienService;

import java.sql.SQLException;
import java.util.List;

public class NhanVienController {
    private final NhanVienService nhanVienService;

    public NhanVienController(NhanVienService nhanVienService) {
        this.nhanVienService = nhanVienService;
    }

    public List<NhanVien> findAll() throws SQLException {
        return nhanVienService.findAllNhanVien();
    }

    public void save(NhanVien nhanVien) throws SQLException {
        nhanVienService.saveNhanVien(nhanVien);
    }

    public void update(NhanVien nhanVien) throws SQLException {
        nhanVienService.updateNhanVien(nhanVien);
    }

    public void delete(int maNguoiDung) throws SQLException {
        nhanVienService.deleteNhanVien(maNguoiDung);
    }
}