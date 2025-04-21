package com.cinema.controllers;

import com.cinema.models.HoaDon;
import com.cinema.models.ChiTietHoaDon;
import com.cinema.services.HoaDonService;
import com.cinema.utils.DatabaseConnection;
import com.cinema.views.HoaDonView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HoaDonController {
    private final HoaDonView view;
    private final HoaDonService service;
    private final DateTimeFormatter ngayGioChieuFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public HoaDonController(HoaDonView view) throws IOException {
        this.view = view;
        this.service = new HoaDonService(new DatabaseConnection());
        initView();
        addListeners();
    }

    private void initView() {
        try {
            loadHoaDonList(service.getAllHoaDon());
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi tải danh sách hóa đơn!");
        }
    }

    private void addListeners() {
        // Tìm kiếm
        view.getTxtSearchID().addActionListener(_ -> searchHoaDon());
        view.getTxtSearchIDKhachHang().addActionListener(_ -> searchHoaDon());
        view.getTxtSearchTenKhachHang().addActionListener(_ -> searchHoaDon());

        // Chọn hóa đơn
        view.getTableHoaDon().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = view.getTableHoaDon().getSelectedRow();
                if (selectedRow >= 0) {
                    displayHoaDonInfo(selectedRow);
                    try {
                        loadChiTietHoaDon((int) view.getModelHoaDon().getValueAt(selectedRow, 0));
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(view, "Lỗi khi tải chi tiết hóa đơn!");
                    }
                }
            }
        });
    }

    private String formatCurrency(BigDecimal amount) {
        return String.format("%,.0f VND", amount);
    }

    private void searchHoaDon() {
        String id = view.getTxtSearchID().getText().trim();
        String idKhachHang = view.getTxtSearchIDKhachHang().getText().trim();
        String tenKhachHang = view.getTxtSearchTenKhachHang().getText().trim();
        try {
            loadHoaDonList(service.searchHoaDon(id, idKhachHang, tenKhachHang));
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi tìm kiếm hóa đơn!");
        }
    }

    private void loadHoaDonList(List<HoaDon> hoaDons) {
        DefaultTableModel model = view.getModelHoaDon();
        model.setRowCount(0);
        for (HoaDon hd : hoaDons) {
            model.addRow(new Object[]{
                    hd.getMaHoaDon(),
                    hd.getTenNhanVien(),
                    hd.getTenKhachHang(),
                    hd.getNgayLap().format(ngayGioChieuFormatter),
                    formatCurrency(hd.getTongTien())
            });
        }
    }

    private void displayHoaDonInfo(int row) {
        DefaultTableModel model = view.getModelHoaDon();
        view.getTxtTenNhanVien().setText((String) model.getValueAt(row, 1));
        view.getTxtNgayLap().setText((String) model.getValueAt(row, 3));
        view.getTxtTongTien().setText(model.getValueAt(row, 4).toString());
    }

    private void loadChiTietHoaDon(int maHoaDon) throws SQLException {
        List<ChiTietHoaDon> chiTietList = service.getChiTietHoaDon(maHoaDon);
        DefaultTableModel model = view.getModelChiTietHoaDon();
        model.setRowCount(0);
        for (ChiTietHoaDon ct : chiTietList) {
            model.addRow(new Object[]{
                    ct.getMaVe(),
                    ct.getTenPhim(),
                    ct.getSoGhe(),
                    ct.getNgayGioChieu() != null ? ct.getNgayGioChieu().format(ngayGioChieuFormatter) : "",
                    formatCurrency(ct.getGiaVe())
            });
        }
    }
}