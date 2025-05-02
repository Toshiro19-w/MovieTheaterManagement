package com.cinema.controllers;

import com.cinema.models.HoaDon;
import com.cinema.models.ChiTietHoaDon;
import com.cinema.services.HoaDonService;
import com.cinema.utils.DatabaseConnection;
import com.cinema.views.admin.HoaDonView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HoaDonController {
    private final HoaDonView view;
    private final HoaDonService service;
    private final DateTimeFormatter ngayGioChieuFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private TableRowSorter<DefaultTableModel> sorter;

    public HoaDonController(HoaDonView view) throws IOException {
        this.view = view;
        this.service = new HoaDonService(new DatabaseConnection());
        initView();
        addListeners();
    }

    private void initView() {
        try {
            loadHoaDonList(service.getAllHoaDon());
            sorter = new TableRowSorter<>(view.getModelHoaDon());
            view.getTableHoaDon().setRowSorter(sorter);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi tải danh sách hóa đơn!");
        }
    }

    private void addListeners() {
        view.getTableHoaDon().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = view.getTableHoaDon().getSelectedRow();
                if (selectedRow >= 0) {
                    displayHoaDonInfo(selectedRow);
                }
            }
        });

        // Tìm kiếm tự động khi gõ vào ô tìm kiếm
        view.getSearchField().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                timKiemHoaDon();
            }
        });
    }

    private String formatCurrency(BigDecimal amount) {
        return String.format("%,.0f VND", amount);
    }

    public void timKiemHoaDon() {
        String tuKhoa = view.getSearchText().toLowerCase();

        if (tuKhoa.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + tuKhoa, 1)); // Cột 1: Tên NV
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
        try {
            int maHoaDon = (int) model.getValueAt(row, 0);
            loadChiTietHoaDon(maHoaDon);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi tải chi tiết hóa đơn!");
        }
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