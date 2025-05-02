package com.cinema.controllers;

import com.cinema.models.Phim;
import com.cinema.models.PhongChieu;
import com.cinema.models.SuatChieu;
import com.cinema.services.PhimService;
import com.cinema.services.PhongChieuService;
import com.cinema.services.SuatChieuService;
import com.cinema.views.admin.SuatChieuView;

import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.JOptionPane;

public class SuatChieuController {
    private final SuatChieuView view;
    private final SuatChieuService service;
    private final PhimService phimService;
    private final PhongChieuService phongChieuService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public SuatChieuController(SuatChieuView view) throws SQLException {
        this.view = view;
        this.service = new SuatChieuService(view.getDatabaseConnection());
        this.phimService = new PhimService(view.getDatabaseConnection());
        this.phongChieuService = new PhongChieuService(view.getDatabaseConnection());
        initView();
        addListeners();
    }

    private void initView() {
        try {
            loadSuatChieuList(service.getAllSuatChieuDetail());
            loadPhimToComboBox();
            loadPhongChieuToComboBox();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi tải dữ liệu suất chiếu!");
        }
    }

    private void addListeners() {
        view.getSearchField().addActionListener(e -> searchSuatChieu());
        view.getTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = view.getTable().getSelectedRow();
                if (selectedRow >= 0) {
                    displaySuatChieuInfo(selectedRow);
                }
            }
        });
        view.getBtnThem().addActionListener(e -> themSuatChieu());
        view.getBtnSua().addActionListener(e -> suaSuatChieu());
        view.getBtnXoa().addActionListener(e -> xoaSuatChieu());
        view.getBtnClear().addActionListener(e -> clearForm());
    }

    private void loadPhimToComboBox() throws SQLException {
        List<Phim> phimList = phimService.getAllPhimDetail();
        view.getCbMaPhim().removeAllItems();
        for (Phim phim : phimList) {
            view.getCbMaPhim().addItem(phim);
        }
    }

    private void loadPhongChieuToComboBox() throws SQLException {
        List<PhongChieu> phongList = phongChieuService.getAllPhongChieu();
        view.getCbMaPhong().removeAllItems();
        for (PhongChieu phong : phongList) {
            view.getCbMaPhong().addItem(phong);
        }
    }

    private void searchSuatChieu() {
        String ngayChieuStr = view.getSeacrhText().trim();
        try {
            if (ngayChieuStr.isEmpty() || ngayChieuStr.equals("dd/MM/yyyy HH:mm:ss")) {
                loadSuatChieuList(service.getAllSuatChieuDetail());
            } else {
                LocalDateTime ngayGioChieu = LocalDateTime.parse(ngayChieuStr, formatter);
                loadSuatChieuList(service.searchSuatChieuByNgay(ngayGioChieu));
            }
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(view, "Ngày giờ chiếu không đúng định dạng (dd/MM/yyyy HH:mm:ss)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi tìm kiếm suất chiếu!");
        }
    }

    private void loadSuatChieuList(List<SuatChieu> suatChieus) {
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);
        for (SuatChieu sc : suatChieus) {
            String ngayGioChieuFormatted = sc.getNgayGioChieu() != null
                    ? sc.getNgayGioChieu().format(formatter)
                    : "Chưa có";
            model.addRow(new Object[]{
                    sc.getMaSuatChieu(),
                    sc.getTenPhim(),
                    sc.getTenPhong(),
                    ngayGioChieuFormatted
            });
        }
    }

    private void displaySuatChieuInfo(int row) {
        DefaultTableModel model = view.getTableModel();
        view.getTxtMaSuatChieu().setText(model.getValueAt(row, 0).toString());
        String tenPhim = model.getValueAt(row, 1).toString();
        String tenPhong = model.getValueAt(row, 2).toString(); // Sửa: Sử dụng tenPhong thay vì loaiPhong
        String ngayGioChieu = model.getValueAt(row, 3).toString();

        // Tìm và chọn phim trong combobox
        for (int i = 0; i < view.getCbMaPhim().getItemCount(); i++) {
            Phim phim = (Phim) view.getCbMaPhim().getItemAt(i);
            if (phim.getTenPhim().equals(tenPhim)) {
                view.getCbMaPhim().setSelectedIndex(i);
                break;
            }
        }

        // Tìm và chọn phòng trong combobox
        for (int i = 0; i < view.getCbMaPhong().getItemCount(); i++) {
            PhongChieu phong = (PhongChieu) view.getCbMaPhong().getItemAt(i);
            if (phong.getTenPhong().equals(tenPhong)) {
                view.getCbMaPhong().setSelectedIndex(i);
                break;
            }
        }

        view.getTxtNgayGioChieu().setText(ngayGioChieu);
    }

    private void themSuatChieu() {
        try {
            SuatChieu suatChieu = createSuatChieuFromForm();
            service.addSuatChieu(suatChieu);
            JOptionPane.showMessageDialog(view, "Thêm suất chiếu thành công!");
            loadSuatChieuList(service.getAllSuatChieuDetail());
            clearForm();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi thêm suất chiếu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(view, "Ngày giờ chiếu không đúng định dạng (dd/MM/yyyy HH:mm:ss)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(view, e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void suaSuatChieu() {
        try {
            if (view.getTxtMaSuatChieu().getText().isEmpty()) {
                JOptionPane.showMessageDialog(view, "Vui lòng chọn suất chiếu cần sửa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            SuatChieu suatChieu = createSuatChieuFromForm();
            suatChieu.setMaSuatChieu(Integer.parseInt(view.getTxtMaSuatChieu().getText()));
            service.updateSuatChieu(suatChieu);
            JOptionPane.showMessageDialog(view, "Cập nhật suất chiếu thành công!");
            loadSuatChieuList(service.getAllSuatChieuDetail());
            clearForm();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi cập nhật suất chiếu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(view, "Mã suất chiếu không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(view, "Ngày giờ chiếu không đúng định dạng (dd/MM/yyyy HH:mm:ss)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(view, e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xoaSuatChieu() {
        if (view.getTxtMaSuatChieu().getText().isEmpty()) {
            JOptionPane.showMessageDialog(view, "Vui lòng chọn suất chiếu cần xóa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int maSuatChieu = Integer.parseInt(view.getTxtMaSuatChieu().getText());
        int confirm = JOptionPane.showConfirmDialog(view, "Bạn có chắc chắn muốn xóa suất chiếu này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                service.deleteSuatChieu(maSuatChieu);
                JOptionPane.showMessageDialog(view, "Xóa suất chiếu thành công!");
                loadSuatChieuList(service.getAllSuatChieuDetail());
                clearForm();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(view, "Lỗi khi xóa suất chiếu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        view.getTxtMaSuatChieu().setText("");
        view.getCbMaPhim().setSelectedIndex(-1);
        view.getCbMaPhong().setSelectedIndex(-1);
        view.getTxtNgayGioChieu().setText("dd/MM/yyyy HH:mm:ss");
        view.getTable().clearSelection();
    }

    private SuatChieu createSuatChieuFromForm() {
        Phim selectedPhim = (Phim) view.getCbMaPhim().getSelectedItem();
        PhongChieu selectedPhong = (PhongChieu) view.getCbMaPhong().getSelectedItem();
        LocalDateTime ngayGioChieu = getLocalDateTime(selectedPhim, selectedPhong);
        if (ngayGioChieu.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Ngày giờ chiếu phải sau thời điểm hiện tại!");
        }

        assert selectedPhim != null;
        assert selectedPhong != null;
        return new SuatChieu(0, selectedPhim.getMaPhim(), selectedPhong.getMaPhong(), ngayGioChieu);
    }

    private LocalDateTime getLocalDateTime(Phim selectedPhim, PhongChieu selectedPhong) {
        String ngayGioChieuStr = view.getTxtNgayGioChieu().getText().trim();

        if (selectedPhim == null) {
            throw new IllegalArgumentException("Vui lòng chọn phim!");
        }
        if (selectedPhong == null) {
            throw new IllegalArgumentException("Vui lòng chọn phòng chiếu!");
        }
        if (ngayGioChieuStr.isEmpty() || ngayGioChieuStr.equals("dd/MM/yyyy HH:mm:ss")) {
            throw new IllegalArgumentException("Vui lòng nhập ngày giờ chiếu!");
        }

        return LocalDateTime.parse(ngayGioChieuStr, formatter);
    }
}