package com.cinema.controllers;

import com.cinema.models.KhachHang;
import com.cinema.enums.TrangThaiVe;
import com.cinema.models.Ve;
import com.cinema.services.VeService;
import com.cinema.utils.ValidationUtils;
import com.cinema.views.admin.VeView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class VeController {
    private final VeView view;
    private final VeService service;
    private final DateTimeFormatter ngayDatFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter ngayGioChieuFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public VeController(VeView view) throws SQLException {
        this.view = view;
        this.service = new VeService(view.getDatabaseConnection());
        initView();
        addListeners();
    }

    private void initView() {
        try {
            loadVeList(service.getAllVeDetail());
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi tải dữ liệu vé!");
        }
    }

    private void addListeners() {
        view.getSearchField().addActionListener(e -> searchVe());
        view.getTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = view.getTable().getSelectedRow();
                if (selectedRow >= 0) {
                    displayVeInfo(selectedRow);
                } else {
                    clearCustomerInfo();
                }
            }
        });
        view.getBtnThem().addActionListener(e -> themVe());
        view.getBtnSua().addActionListener(e -> suaVe());
        view.getBtnXoa().addActionListener(e -> xoaVe());
        view.getBtnClear().addActionListener(e -> clearForm());
    }

    private void loadVeList(List<Ve> veList) {
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);
        for (Ve ve : veList) {
            model.addRow(new Object[]{
                    ve.getMaVe(),
                    ve.getTrangThai().getValue(),
                    formatCurrency(ve.getGiaVe()),
                    ve.getSoGhe(),
                    ve.getNgayDat() != null ? ve.getNgayDat().format(ngayDatFormatter) : "Chưa đặt",
                    ve.getTenPhong() != null ? ve.getTenPhong() : "Chưa đặt",
                    ve.getNgayGioChieu() != null ? ve.getNgayGioChieu().format(ngayGioChieuFormatter) : "Chưa có",
                    ve.getTenPhim()
            });
        }
    }

    private void displayVeInfo(int row) {
        DefaultTableModel model = view.getTableModel();
        view.getTxtMaVe().setText(model.getValueAt(row, 0).toString());
        view.getCbTrangThai().setSelectedItem(model.getValueAt(row, 1).toString());
        view.getTxtGiaVe().setText(model.getValueAt(row, 2).toString().replaceAll("[^\\d]", ""));
        view.getTxtSoGhe().setText(model.getValueAt(row, 3).toString());
        view.getTxtNgayDat().setText(model.getValueAt(row, 4).toString());
        view.getTxtTenPhong().setText(model.getValueAt(row, 5).toString());
        view.getTxtNgayGioChieu().setText(model.getValueAt(row, 6).toString().trim());
        view.getTxtTenPhim().setText(model.getValueAt(row, 7).toString());

        // Update customer info
        updateCustomerInfo(Integer.parseInt(view.getTxtMaVe().getText()));
    }

    private void updateCustomerInfo(int maVe) {
        try {
            KhachHang khachHang = service.getKhachHangByMaVe(maVe);
            DefaultTableModel model = view.getTableKhachHangModel();
            model.setRowCount(0); // Clear existing data
            if (khachHang != null) {
                model.addRow(new Object[]{
                        khachHang.getHoTen(),
                        khachHang.getSoDienThoai(),
                        khachHang.getEmail()
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi tải thông tin khách hàng!");
        }
    }

    private void clearCustomerInfo() {
        DefaultTableModel model = view.getTableKhachHangModel();
        model.setRowCount(0);
    }

    private String formatCurrency(BigDecimal amount) {
        DecimalFormat formatter = new DecimalFormat("#,### VND");
        return formatter.format(amount);
    }

    private BigDecimal parseCurrency(String currencyStr) {
        try {
            String cleanStr = currencyStr.replaceAll("[^\\d]", "");
            return new BigDecimal(cleanStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Giá vé không hợp lệ: " + currencyStr);
        }
    }

    private void themVe() {
        try {
            if (!validateForm()) {
                return;
            }
            Ve ve = createVeFromForm();
            Ve result = service.saveVe(ve);
            if (result != null) {
                JOptionPane.showMessageDialog(view, "Thêm vé thành công!");
                loadVeList(service.getAllVeDetail());
                clearForm();
            } else {
                JOptionPane.showMessageDialog(view, "Thêm vé thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi thêm vé: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(view, "Ngày giờ chiếu không đúng định dạng (dd/MM/yyyy HH:mm:ss)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(view, e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void suaVe() {
        try {
            if (view.getTxtMaVe().getText().isEmpty()) {
                JOptionPane.showMessageDialog(view, "Vui lòng chọn vé cần sửa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!validateForm()) {
                return;
            }
            Ve ve = createVeFromForm();
            ve.setMaVe(Integer.parseInt(view.getTxtMaVe().getText()));
            Ve result = service.updateVe(ve);
            if (result != null) {
                JOptionPane.showMessageDialog(view, "Cập nhật vé thành công!");
                loadVeList(service.getAllVeDetail());
                clearForm();
            } else {
                JOptionPane.showMessageDialog(view, "Cập nhật vé thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi cập nhật vé: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(view, "Mã vé không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(view, "Ngày giờ chiếu không đúng định dạng (dd/MM/yyyy HH:mm:ss)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(view, e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xoaVe() {
        if (view.getTxtMaVe().getText().isEmpty()) {
            JOptionPane.showMessageDialog(view, "Vui lòng chọn vé cần xóa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int maVe = Integer.parseInt(view.getTxtMaVe().getText());
        int confirm = JOptionPane.showConfirmDialog(view, "Bạn có chắc chắn muốn xóa vé này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                service.deleteVe(maVe);
                JOptionPane.showMessageDialog(view, "Xóa vé thành công!");
                loadVeList(service.getAllVeDetail());
                clearForm();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(view, "Lỗi khi xóa vé: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void searchVe() {
        String soGhe = view.getSearchField().getText().trim();
        if (!ValidationUtils.isValidString(soGhe)) {
            try {
                loadVeList(service.getAllVeDetail());
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(view, "Lỗi khi tải danh sách vé!");
            }
            return;
        }
        try {
            List<Ve> veList = service.findBySoGhe(soGhe);
            loadVeList(veList);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi tìm kiếm vé!");
        }
    }

    private boolean validateForm() {
        if (!ValidationUtils.isValidString(view.getTxtSoGhe().getText())) {
            JOptionPane.showMessageDialog(view, "Số ghế không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!ValidationUtils.isValidString(view.getTxtTenPhong().getText())) {
            JOptionPane.showMessageDialog(view, "Tên phòng không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!ValidationUtils.isValidString(view.getTxtTenPhim().getText())) {
            JOptionPane.showMessageDialog(view, "Tên phim không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!ValidationUtils.isValidString(view.getTxtNgayGioChieu().getText()) || view.getTxtNgayGioChieu().getText().equals("dd/MM/yyyy HH:mm:ss")) {
            JOptionPane.showMessageDialog(view, "Ngày giờ chiếu không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private Ve createVeFromForm() {
        TrangThaiVe trangThai = TrangThaiVe.valueOf(view.getCbTrangThai().getSelectedItem().toString());
        String soGhe = view.getTxtSoGhe().getText();
        BigDecimal giaVe = parseCurrency(view.getTxtGiaVe().getText());
        LocalDateTime ngayDat = LocalDateTime.now();
        String tenPhong = view.getTxtTenPhong().getText();
        String ngayGioChieuStr = view.getTxtNgayGioChieu().getText().trim();
        LocalDateTime ngayGioChieu = LocalDateTime.parse(ngayGioChieuStr, ngayGioChieuFormatter);
        String tenPhim = view.getTxtTenPhim().getText();

        return new Ve(0, trangThai, giaVe, soGhe, ngayDat, tenPhong, ngayGioChieu, tenPhim);
    }

    private void clearForm() {
        view.getTxtMaVe().setText("");
        view.getCbTrangThai().setSelectedIndex(0);
        view.getTxtGiaVe().setText("");
        view.getTxtSoGhe().setText("");
        view.getTxtNgayDat().setText("");
        view.getTxtTenPhong().setText("");
        view.getTxtNgayGioChieu().setText("dd/MM/yyyy HH:mm:ss");
        view.getTxtTenPhim().setText("");
        clearCustomerInfo();
        view.getTable().clearSelection();
    }
}