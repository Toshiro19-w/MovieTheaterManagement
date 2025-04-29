package com.cinema.controllers;

import com.cinema.models.LoaiNguoiDung;
import com.cinema.models.NhanVien;
import com.cinema.models.TaiKhoan;
import com.cinema.models.VaiTro;
import com.cinema.services.NhanVienService;
import com.cinema.services.TaiKhoanService;
import com.cinema.views.NhanVienView;

import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import javax.swing.JOptionPane;

public class NhanVienController {
    private final NhanVienView view;
    private final NhanVienService service;
    private final TaiKhoanService taiKhoanService;

    public NhanVienController(NhanVienView view) {
        this.view = view;
        this.service = new NhanVienService(view.getDatabaseConnection());
        this.taiKhoanService = new TaiKhoanService(view.getDatabaseConnection());
        initView();
        addListeners();
    }


    private void initListeners() {
        view.getBtnTaoTaiKhoan().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                taoTaiKhoan();
            }
        });
    }

    private void taoTaiKhoan() {
        try {
            String tenDangNhap = view.getTxtTenDangNhap().getText();
            String matKhau = new String(view.getTxtMatKhau().getPassword());
            String loaiTaiKhoan = (String) view.getCmbLoaiTaiKhoan().getSelectedItem();
            Integer maNhanVien = view.getSelectedMaNV();

            if (maNhanVien == null) {
                JOptionPane.showMessageDialog(view, "Vui lòng chọn nhân viên!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            TaiKhoan taiKhoan = new TaiKhoan(tenDangNhap, matKhau, loaiTaiKhoan, maNhanVien);
            taiKhoanService.createTaiKhoan(taiKhoan);

            JOptionPane.showMessageDialog(view, "Tạo tài khoản thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(view, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(view, "Lỗi cơ sở dữ liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initView() {
        try {
            loadNhanVienList(service.findAllNhanVien());
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi tải danh sách nhân viên!");
        }
    }

    private void addListeners() {
        view.getSearchField().addActionListener(_ -> searchNhanVien());
        view.getTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = view.getTable().getSelectedRow();
                if (selectedRow >= 0) {
                    displayNhanVienInfo(selectedRow);
                }
            }
        });
        view.getBtnThem().addActionListener(_ -> themNhanVien());
        view.getBtnSua().addActionListener(_ -> suaNhanVien());
        view.getBtnXoa().addActionListener(_ -> xoaNhanVien());
        view.getBtnClear().addActionListener(_ -> clearForm());
    }

    private void searchNhanVien() {
        String hoTen = view.getSearchText().trim();
        try {
            loadNhanVienList(service.searchNhanVien(hoTen));
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi tìm kiếm nhân viên!");
        }
    }

    private void loadNhanVienList(List<NhanVien> nhanViens) {
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);
        for (NhanVien nv : nhanViens) {
            model.addRow(new Object[]{
                    nv.getMaNguoiDung(),
                    nv.getHoTen(),
                    nv.getSoDienThoai(),
                    nv.getEmail(),
                    formatCurrency(nv.getLuong()),
                    nv.getVaiTro().getValue()
            });
        }
    }

    private String formatCurrency(BigDecimal amount) {
        return String.format("%,.0f VND", amount);
    }

    private void displayNhanVienInfo(int row) {
        DefaultTableModel model = view.getTableModel();
        view.getTxtMaND().setText(model.getValueAt(row, 0).toString());
        view.getTxtHoTen().setText(model.getValueAt(row, 1).toString());
        view.getTxtSDT().setText(model.getValueAt(row, 2).toString());
        view.getTxtEmail().setText(model.getValueAt(row, 3).toString());
        view.getTxtLuong().setText(model.getValueAt(row, 4).toString().replace(" VND", "").replace(",", ""));
        view.getVaiTroCombo().setSelectedItem(model.getValueAt(row, 5).toString());
    }

    private void themNhanVien() {
        try {
            NhanVien nhanVien = createNhanVienFromForm();
            service.saveNhanVien(nhanVien);
            JOptionPane.showMessageDialog(view, "Thêm nhân viên thành công!");
            loadNhanVienList(service.findAllNhanVien());
            clearForm();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi thêm nhân viên: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(view, "Lương phải là số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(view, e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void suaNhanVien() {
        try {
            if (view.getTxtMaND().getText().isEmpty()) {
                JOptionPane.showMessageDialog(view, "Vui lòng chọn nhân viên cần sửa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            NhanVien nhanVien = createNhanVienFromForm();
            nhanVien.setMaNguoiDung(Integer.parseInt(view.getTxtMaND().getText()));
            service.updateNhanVien(nhanVien);
            JOptionPane.showMessageDialog(view, "Cập nhật nhân viên thành công!");
            loadNhanVienList(service.findAllNhanVien());
            clearForm();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi cập nhật nhân viên: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(view, "Lương phải là số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(view, e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xoaNhanVien() {
        if (view.getTxtMaND().getText().isEmpty()) {
            JOptionPane.showMessageDialog(view, "Vui lòng chọn nhân viên cần xóa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int maNguoiDung = Integer.parseInt(view.getTxtMaND().getText());
        int confirm = JOptionPane.showConfirmDialog(view, "Bạn có chắc chắn muốn xóa nhân viên này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                service.deleteNhanVien(maNguoiDung);
                JOptionPane.showMessageDialog(view, "Xóa nhân viên thành công!");
                loadNhanVienList(service.findAllNhanVien());
                clearForm();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(view, "Lỗi khi xóa nhân viên: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        view.getTxtMaND().setText("");
        view.getTxtHoTen().setText("");
        view.getTxtSDT().setText("");
        view.getTxtEmail().setText("");
        view.getTxtLuong().setText("");
        view.getVaiTroCombo().setSelectedIndex(0);
        view.getTable().clearSelection();
    }

    private NhanVien createNhanVienFromForm() {
        String hoTen = view.getTxtHoTen().getText().trim();
        String sdt = view.getTxtSDT().getText().trim();
        String email = view.getTxtEmail().getText().trim();
        String luongStr = view.getTxtLuong().getText().trim();
        VaiTro vaiTro = VaiTro.fromString(Objects.requireNonNull(view.getVaiTroCombo().getSelectedItem()).toString());

        if (hoTen.isEmpty()) {
            throw new IllegalArgumentException("Họ tên không được để trống!");
        }
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Email không hợp lệ!");
        }
        if (!isValidPhoneNumber(sdt)) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ (phải có 10 chữ số, bắt đầu từ 0)!");
        }
        BigDecimal luong = new BigDecimal(luongStr);
        if (luong.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Lương phải là số dương!");
        }

        return new NhanVien(0, hoTen, sdt, email, LoaiNguoiDung.NHANVIEN, luong, vaiTro);
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("^0\\d{9}$");
    }
}