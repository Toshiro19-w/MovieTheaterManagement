package com.cinema.controllers;

import com.cinema.models.NhanVien;
import com.cinema.services.NhanVienService;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class NhanVienController extends Component {
    private final NhanVienService nhanVienService;

    public NhanVienController(NhanVienService nhanVienService) {
        this.nhanVienService = nhanVienService;
    }

    public List<NhanVien> findAll() throws SQLException {
        return nhanVienService.findAllNhanVien();
    }

    public List<NhanVien> searchNhanVienByTen(String hoTen) {
        try {
            return nhanVienService.findByTen(hoTen);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tìm kiếm nhân viên: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return null;
        }
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