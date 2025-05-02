package com.cinema.controllers;

import com.cinema.models.Phim;
import com.cinema.services.PhimService;
import com.cinema.views.admin.PhimView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

public class PhimController {
    private final PhimView view;
    private final PhimService service;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private TableRowSorter<DefaultTableModel> sorter;

    public PhimController(PhimView view) throws SQLException {
        this.view = view;
        this.service = new PhimService(view.getDatabaseConnection());
        initView();
        addListeners();
    }

    private void initView() {
        try {
            loadPhimList(service.getAllPhimDetail());
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi tải dữ liệu phim!");
        }
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

    private void addListeners() {
        view.getTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = view.getTable().getSelectedRow();
                if (selectedRow >= 0) {
                    displayPhimInfo(selectedRow);
                }
            }
        });

        // Tìm kiếm tự động khi gõ vào ô tìm kiếm
        view.getSearchField().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                timKiemPhim();
            }
        });

        view.getBtnThem().addActionListener(e -> themPhim());
        view.getBtnSua().addActionListener(e -> suaPhim());
        view.getBtnXoa().addActionListener(e -> xoaPhim());
        view.getBtnClear().addActionListener(e -> clearForm());
    }

    public void timKiemPhim() {
        String tuKhoa = view.getSearchText().toLowerCase();

        if (tuKhoa.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + tuKhoa, 1)); // Cột 1: Tên Sách
        }
    }

    private void loadPhimList(List<Phim> phimList) {
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);
        for (Phim phim : phimList) {
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
        String maPhim = model.getValueAt(row, 0).toString();
        String tenPhim = model.getValueAt(row, 1).toString();
        String theLoai = model.getValueAt(row, 2).toString();
        String thoiLuong = model.getValueAt(row, 3).toString().replace(" phút","");
        String ngayKhoiChieu = model.getValueAt(row, 4).toString();
        String nuocSanXuat = model.getValueAt(row, 5).toString();
        String dinhDang = model.getValueAt(row, 6).toString();
        String moTa = model.getValueAt(row, 7).toString();
        String daoDien = model.getValueAt(row, 8).toString();

        view.getTxtMaPhim().setText(maPhim);
        view.getTxtTenPhim().setText(tenPhim);
        view.getTxtTenTheLoai().setText(theLoai);
        view.getTxtThoiLuong().setText(thoiLuong);
        view.getTxtNgayKhoiChieu().setText(ngayKhoiChieu);
        view.getTxtNuocSanXuat().setText(nuocSanXuat);
        view.getTxtDinhDang().setText(dinhDang);
        view.getTxtMoTa().setText(moTa);
        view.getTxtDaoDien().setText(daoDien);

        // Tìm phim để lấy posterPath
        try {
            List<Phim> phimList = service.getAllPhimDetail();
            Phim selectedPhim = null;
            for (Phim phim : phimList) {
                if (phim.getMaPhim() == Integer.parseInt(maPhim)) {
                    selectedPhim = phim;
                    break;
                }
            }

            // Hiển thị ảnh
            if (selectedPhim != null && selectedPhim.getDuongDanPoster() != null && !selectedPhim.getDuongDanPoster().isEmpty()) {
                try {
                    ImageIcon posterIcon = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("images/posters/" + selectedPhim.getDuongDanPoster())));
                    Image scaledImage = posterIcon.getImage().getScaledInstance(150, 200, Image.SCALE_SMOOTH);
                    view.getPosterLabel().setIcon(new ImageIcon(scaledImage));
                    view.getPosterLabel().setText("");
                } catch (Exception e) {
                    e.printStackTrace();
                    view.getPosterLabel().setIcon(null);
                    view.getPosterLabel().setText("Không tìm thấy ảnh");
                }
            } else {
                view.getPosterLabel().setIcon(null);
                view.getPosterLabel().setText("Không có ảnh");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi tải thông tin phim!");
        }
    }

    private void themPhim() {
        try {
            Phim phim = createPhimFromForm();
            service.addPhim(phim);
            JOptionPane.showMessageDialog(view, "Thêm phim thành công!");
            loadPhimList(service.getAllPhimDetail());
            clearForm();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi thêm phim: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(view, "Ngày khởi chiếu không đúng định dạng (dd/MM/yyyy)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
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
            service.updatePhim(phim);
            JOptionPane.showMessageDialog(view, "Cập nhật phim thành công!");
            loadPhimList(service.getAllPhimDetail());
            clearForm();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi cập nhật phim: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(view, "Mã phim không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(view, "Ngày khởi chiếu không đúng định dạng (dd/MM/yyyy)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
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
                service.deletePhim(maPhim);
                JOptionPane.showMessageDialog(view, "Xóa phim thành công!");
                loadPhimList(service.getAllPhimDetail());
                clearForm();
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
        view.getPosterLabel().setIcon(null);
        view.getPosterLabel().setText("");
        view.clearSelectedPosterPath();
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
        String posterPath = view.getSelectedPosterPath();

        if (tenPhim.isEmpty() || tenTheLoai.isEmpty() || thoiLuongStr.isEmpty() || ngayKhoiChieuStr.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ thông tin bắt buộc!");
        }

        int thoiLuong;
        try {
            thoiLuong = Integer.parseInt(thoiLuongStr);
            if (thoiLuong <= 0) {
                throw new IllegalArgumentException("Thời lượng phải lớn hơn 0!");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Thời lượng phải là số nguyên!");
        }

        LocalDate ngayKhoiChieu = LocalDate.parse(ngayKhoiChieuStr, formatter);
        if (ngayKhoiChieu.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày khởi chiếu phải sau ngày hiện tại!");
        }

        return new Phim(0, tenPhim, tenTheLoai, thoiLuong, ngayKhoiChieu, nuocSanXuat, dinhDang, moTa, daoDien, posterPath);
    }
}