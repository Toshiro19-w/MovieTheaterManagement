package com.cinema.controllers;

import com.cinema.models.PhongChieu;
import com.cinema.services.PhongChieuService;
import com.cinema.views.PhongChieuView;

import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class PhongChieuController {
    private final PhongChieuView view;
    private final PhongChieuService service;

    public PhongChieuController(PhongChieuView view) {
        this.view = view;
        this.service = new PhongChieuService(view.getDatabaseConnection());
        initView();
        addListeners();
    }

    private void initView() {
        try {
            loadPhongChieuList(service.getAllPhongChieu());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(view, "Lỗi khi tải danh sách phòng chiếu: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addListeners() {
        view.getTxtSearchTenPhong().addActionListener(_ -> searchPhongChieu());
        view.getTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = view.getTable().getSelectedRow();
                if (selectedRow >= 0) {
                    displayPhongChieuInfo(selectedRow);
                }
            }
        });
        view.getBtnThem().addActionListener(_ -> themPhongChieu());
        view.getBtnClear().addActionListener(_ -> clearForm());
    }

    private void searchPhongChieu() {
        String tenPhong = view.getTxtSearchTenPhong().getText().trim();
        try {
            loadPhongChieuList(service.searchPhongChieuByTenPhong(tenPhong));
            if (view.getTableModel().getRowCount() == 0) {
                JOptionPane.showMessageDialog(view, "Không tìm thấy phòng chiếu!",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                loadPhongChieuList(service.getAllPhongChieu());
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(view, "Lỗi khi tìm kiếm phòng chiếu: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPhongChieuList(java.util.List<PhongChieu> phongChieus) {
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);
        for (PhongChieu pc : phongChieus) {
            model.addRow(new Object[]{
                    pc.getMaPhong(),
                    pc.getTenPhong(),
                    pc.getSoLuongGhe(),
                    pc.getLoaiPhong()
            });
        }
    }

    private void displayPhongChieuInfo(int row) {
        DefaultTableModel model = view.getTableModel();
        view.getTxtMaPhong().setText(model.getValueAt(row, 0).toString());
        view.getTxtTenPhong().setText(model.getValueAt(row, 1).toString());
        view.getTxtSoLuongGhe().setText(model.getValueAt(row, 2).toString());
        view.getTxtLoaiPhong().setText(model.getValueAt(row, 3).toString());
    }

    private void themPhongChieu() {
        try {
            PhongChieu phongChieu = createPhongChieuFromForm();
            service.addPhongChieu(phongChieu);
            JOptionPane.showMessageDialog(view, "Thêm phòng chiếu thành công!");
            loadPhongChieuList(service.getAllPhongChieu());
            clearForm();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(view, "Lỗi khi thêm phòng chiếu: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(view, "Số lượng ghế phải là số!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(view, e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        view.getTxtMaPhong().setText("");
        view.getTxtTenPhong().setText("");
        view.getTxtSoLuongGhe().setText("");
        view.getTxtLoaiPhong().setText("");
        view.getTable().clearSelection();
    }

    private PhongChieu createPhongChieuFromForm() {
        String tenPhong = view.getTxtTenPhong().getText().trim();
        String soLuongGheStr = view.getTxtSoLuongGhe().getText().trim();
        String loaiPhong = view.getTxtLoaiPhong().getText().trim();

        if (soLuongGheStr.isEmpty() || loaiPhong.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ thông tin!");
        }

        int soLuongGhe = Integer.parseInt(soLuongGheStr);
        if (soLuongGhe <= 0) {
            throw new IllegalArgumentException("Số lượng ghế phải lớn hơn 0!");
        }

        return new PhongChieu(0, tenPhong, soLuongGhe, loaiPhong);
    }
}