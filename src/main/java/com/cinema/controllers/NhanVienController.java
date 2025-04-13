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

    public List<NhanVien> findAll() {
        try {
            return nhanVienService.findAllNhanVien();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải danh sách nhân viên!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public List<NhanVien> searchNhanVienByTen(String hoTen) {
        try {
            return nhanVienService.findByTen(hoTen);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tìm kiếm nhân viên: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public void save(NhanVien nhanVien)  {
        try {
            nhanVienService.saveNhanVien(nhanVien);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Thêm nhân viên thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void update(NhanVien nhanVien) {
        try {
            nhanVienService.updateNhanVien(nhanVien);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Cập nhật nhân viên thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void delete(int maNguoiDung) {
        try {
            nhanVienService.deleteNhanVien(maNguoiDung);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Xóa nhân viên thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}