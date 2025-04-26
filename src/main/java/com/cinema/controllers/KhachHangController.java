package com.cinema.controllers;

import com.cinema.models.KhachHang;
import com.cinema.models.LoaiNguoiDung;
import com.cinema.services.KhachHangService;
//import com.cinema.views.KhachHangView;

import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;
import java.sql.SQLException;
import java.util.List;

public class KhachHangController {

    private final KhachHangService service = null;


    private void initView() {
        try {
            loadKhachHangList(service.findAllKhachHang());
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi tải danh sách khách hàng!");
        }
    }

    private void addListeners() {
        view.getTxtSearchHoTen().addActionListener(_ -> searchKhachHang());
        view.getTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = view.getTable().getSelectedRow();
                if (selectedRow >= 0) {
                    displayKhachHangInfo(selectedRow);
                }
            }
        });
        view.getBtnThem().addActionListener(_ -> themKhachHang());
        view.getBtnSua().addActionListener(_ -> suaKhachHang());
        view.getBtnXoa().addActionListener(_ -> xoaKhachHang());
        view.getBtnClear().addActionListener(_ -> clearForm());
    }

    private void searchKhachHang() {
        String hoTen = view.getTxtSearchHoTen().getText().trim();
        try {
            loadKhachHangList(service.searchKhachHang(hoTen));
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi tìm kiếm khách hàng!");
        }
    }

    private void loadKhachHangList(List<KhachHang> khachHangs) {
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);
        for (KhachHang kh : khachHangs) {
            model.addRow(new Object[]{
                    kh.getMaNguoiDung(),
                    kh.getHoTen(),
                    kh.getSoDienThoai(),
                    kh.getEmail(),
                    kh.getDiemTichLuy()
            });
        }
    }

    private void displayKhachHangInfo(int row) {
        DefaultTableModel model = view.getTableModel();
        view.getTxtMaND().setText(model.getValueAt(row, 0).toString());
        view.getTxtHoTen().setText(model.getValueAt(row, 1).toString());
        view.getTxtSDT().setText(model.getValueAt(row, 2).toString());
        view.getTxtEmail().setText(model.getValueAt(row, 3).toString());
        view.getTxtDiem().setText(model.getValueAt(row, 4).toString());
    }

    private void themKhachHang() {
        try {
            KhachHang khachHang = createKhachHangFromForm();
            service.saveKhachHang(khachHang);
            JOptionPane.showMessageDialog(view, "Thêm khách hàng thành công!");
            loadKhachHangList(service.findAllKhachHang());
            clearForm();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi thêm khách hàng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void suaKhachHang() {
        try {
            if (view.getTxtMaND().getText().isEmpty()) {
                JOptionPane.showMessageDialog(view, "Vui lòng chọn khách hàng cần sửa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            KhachHang khachHang = createKhachHangFromForm();
            khachHang.setMaNguoiDung(Integer.parseInt(view.getTxtMaND().getText()));
            service.updateKhachHang(khachHang);
            JOptionPane.showMessageDialog(view, "Cập nhật khách hàng thành công!");
            loadKhachHangList(service.findAllKhachHang());
            clearForm();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi cập nhật khách hàng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xoaKhachHang() {
        if (view.getTxtMaND().getText().isEmpty()) {
            JOptionPane.showMessageDialog(view, "Vui lòng chọn khách hàng cần xóa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int maNguoiDung = Integer.parseInt(view.getTxtMaND().getText());
        int confirm = JOptionPane.showConfirmDialog(view, "Bạn có chắc chắn muốn xóa khách hàng này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                service.deleteKhachHang(maNguoiDung);
                JOptionPane.showMessageDialog(view, "Xóa khách hàng thành công!");
                loadKhachHangList(service.findAllKhachHang());
                clearForm();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(view, "Lỗi khi xóa khách hàng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        view.getTxtMaND().setText("");
        view.getTxtHoTen().setText("");
        view.getTxtSDT().setText("");
        view.getTxtEmail().setText("");
        view.getTxtDiem().setText("");
        view.getTable().clearSelection();
    }

    private KhachHang createKhachHangFromForm() {
        String hoTen = view.getTxtHoTen().getText().trim();
        String sdt = view.getTxtSDT().getText().trim();
        String email = view.getTxtEmail().getText().trim();
        String diemStr = view.getTxtDiem().getText().trim();

        if (hoTen.isEmpty()) throw new IllegalArgumentException("Họ tên không được để trống!");
        if (!isValidEmail(email)) throw new IllegalArgumentException("Email không hợp lệ!");
        if (!isValidPhoneNumber(sdt)) throw new IllegalArgumentException("Số điện thoại không hợp lệ!");

        int diem = Integer.parseInt(diemStr);
        if (diem < 0) throw new IllegalArgumentException("Điểm tích lũy không được âm!");

        return new KhachHang(0, hoTen, sdt, email, LoaiNguoiDung.KHACHHANG, diem);
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("^0\\d{9}$");
    }
}
