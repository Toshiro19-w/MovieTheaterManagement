package com.cinema.controllers;

import com.cinema.models.Phim;
import com.cinema.services.PhimService;
import com.cinema.views.PhimView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PhimController {
    private final PhimView view;
    private final PhimService service;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public PhimController(PhimView view) throws SQLException {
        this.view = view;
        this.service = new PhimService(view.getDatabaseConnection());
        initView();
        addListeners();
    }

    public List<Phim> getAllPhimDetail() {
        try {
            return service.getAllPhimDetail();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi tải danh sách phim!");
            return List.of(); // Trả về danh sách rỗng nếu có lỗi
        }
    }

    private void initView() {
        try {
            loadPhimList(service.getAllPhimDetail());
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi tải danh sách phim!");
        }
    }

    private void addListeners() {
        // Tìm kiếm
        view.getTxtSearchTenPhim().addActionListener(e -> searchPhim());

        // Chọn phim
        view.getTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = view.getTable().getSelectedRow();
                if (selectedRow >= 0) {
                    displayPhimInfo(selectedRow);
                }
            }
        });

        // Nút thêm
        view.getBtnThem().addActionListener(_ -> themPhim());

        // Nút sửa
        view.getBtnSua().addActionListener(_ -> suaPhim());

        // Nút xóa
        view.getBtnXoa().addActionListener(_ -> xoaPhim());

        // Nút clear
        view.getBtnClear().addActionListener(_ -> clearForm());
    }

    private void searchPhim() {
        String tenPhim = view.getTxtSearchTenPhim().getText().trim();
        String tenTheLoai = view.getTxtSearchTenTheLoai().getText().trim();
        String nuocSanXuat = view.getTxtNuocSanXuat().getText().trim();
        String daoDien = view.getTxtDaoDien().getText().trim();
        try {
            loadPhimList(service.getPhimByTen(tenPhim, tenTheLoai, nuocSanXuat, daoDien));
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi tìm kiếm phim!");
        }
    }

    private void loadPhimList(List<Phim> phims) {
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);
        for (Phim phim : phims) {
            model.addRow(new Object[]{
                    phim.getMaPhim(),
                    phim.getTenPhim(),
                    phim.getTenTheLoai(),
                    phim.getThoiLuong() + " phút",
                    phim.getNgayKhoiChieu() != null ? phim.getNgayKhoiChieu().format(formatter) : "",
                    phim.getNuocSanXuat(),
                    phim.getDinhDang(),
                    phim.getMoTa(),
                    phim.getDaoDien()
            });
        }
    }

    private void displayPhimInfo(int row) {
        DefaultTableModel model = view.getTableModel();
        view.getTxtMaPhim().setText(model.getValueAt(row, 0).toString());
        view.getTxtTenPhim().setText(model.getValueAt(row, 1).toString());
        view.getTxtTenTheLoai().setText(model.getValueAt(row, 2).toString());
        view.getTxtThoiLuong().setText(model.getValueAt(row, 3).toString().replace(" phút", ""));
        view.getTxtNgayKhoiChieu().setText(model.getValueAt(row, 4).toString());
        view.getTxtNuocSanXuat().setText(model.getValueAt(row, 5).toString());
        view.getTxtDinhDang().setText(model.getValueAt(row, 6).toString());
        view.getTxtMoTa().setText(model.getValueAt(row, 7).toString());
        view.getTxtDaoDien().setText(model.getValueAt(row, 8).toString());
    }

    private void themPhim() {
        try {
            Phim phim = createPhimFromForm();
            Phim result = service.addPhim(phim);
            if (result != null) {
                JOptionPane.showMessageDialog(view, "Thêm phim thành công!");
                loadPhimList(service.getAllPhimDetail());
                clearForm();
            } else {
                JOptionPane.showMessageDialog(view, "Thêm phim thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi thêm phim: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(view, "Thời lượng phải là số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(view, e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void suaPhim() {
        try {
            if (view.getTxtMaPhim().getText().isEmpty()) {
                JOptionPane.showMessageDialog(view, "Vui lòng chọn phim cần sửa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Phim phim = createPhimFromForm();
            phim.setMaPhim(Integer.parseInt(view.getTxtMaPhim().getText()));
            Phim result = service.updatePhim(phim);
            if (result != null) {
                JOptionPane.showMessageDialog(view, "Cập nhật phim thành công!");
                loadPhimList(service.getAllPhimDetail());
                clearForm();
            } else {
                JOptionPane.showMessageDialog(view, "Cập nhật phim thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi cập nhật phim: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(view, "Thời lượng phải là số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(view, e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xoaPhim() {
        if (view.getTxtMaPhim().getText().isEmpty()) {
            JOptionPane.showMessageDialog(view, "Vui lòng chọn phim cần xóa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int maPhim = Integer.parseInt(view.getTxtMaPhim().getText());
        int confirm = JOptionPane.showConfirmDialog(view, "Bạn có chắc chắn muốn xóa phim này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean result = service.deletePhim(maPhim);
                if (result) {
                    JOptionPane.showMessageDialog(view, "Xóa phim thành công!");
                    loadPhimList(service.getAllPhimDetail());
                    clearForm();
                } else {
                    JOptionPane.showMessageDialog(view, "Xóa phim thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(view, "Lỗi khi xóa phim: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        view.getTxtMaPhim().setText("");
        view.getTxtTenPhim().setText("");
        view.getTxtTenTheLoai().setText("");
        view.getTxtThoiLuong().setText("");
        view.getTxtNgayKhoiChieu().setText("");
        view.getTxtNuocSanXuat().setText("");
        view.getTxtDinhDang().setText("");
        view.getTxtMoTa().setText("");
        view.getTxtDaoDien().setText("");
        view.getTable().clearSelection();
    }

    private Phim createPhimFromForm() {
        String tenPhim = view.getTxtTenPhim().getText().trim();
        String tenTheLoai = view.getTxtTenTheLoai().getText().trim();
        String thoiLuongStr = view.getTxtThoiLuong().getText().trim();
        String ngayKhoiChieuStr = view.getTxtNgayKhoiChieu().getText().trim();
        String nuocSanXuat = view.getTxtNuocSanXuat().getText().trim();
        String dinhDang = view.getTxtDinhDang().getText().trim();
        String moTa = view.getTxtMoTa().getText().trim();
        String daoDien = view.getTxtDaoDien().getText().trim();

        if (tenPhim.isEmpty()) {
            throw new IllegalArgumentException("Tên phim không được để trống!");
        }
        if (tenTheLoai.isEmpty()) {
            throw new IllegalArgumentException("Thể loại không được để trống!");
        }
        int thoiLuong = Integer.parseInt(thoiLuongStr);
        if (thoiLuong <= 0) {
            throw new IllegalArgumentException("Thời lượng phải là số dương!");
        }
        LocalDate ngayKhoiChieu = ngayKhoiChieuStr.isEmpty() ? null : LocalDate.parse(ngayKhoiChieuStr, formatter);

        return new Phim(0, tenPhim, 0, tenTheLoai, thoiLuong, ngayKhoiChieu, nuocSanXuat, dinhDang, moTa, daoDien, 0);
    }
}