package com.cinema.controllers;

import com.cinema.models.Phim;
import com.cinema.models.PhongChieu;
import com.cinema.models.SuatChieu;
import com.cinema.services.PhimService;
import com.cinema.services.PhongChieuService;
import com.cinema.services.SuatChieuService;
import com.cinema.views.admin.SuatChieuView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

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
            JOptionPane.showMessageDialog(view, "Lỗi khi tải dữ liệu suất chiếu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addListeners() {
        view.getSuatChieuSearchField().addActionListener(e -> searchSuatChieu());
        view.getSuatChieuTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = view.getSuatChieuTable().getSelectedRow();
                if (selectedRow >= 0) {
                    displaySuatChieuInfo(selectedRow);
                }
            }
        });
        view.getBtnThemSuat().addActionListener(e -> themSuatChieu());
        view.getBtnSuaSuat().addActionListener(e -> suaSuatChieu());
        view.getBtnXoaSuat().addActionListener(e -> xoaSuatChieu());
        view.getBtnClearSuat().addActionListener(e -> clearForm());
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
        String ngayChieuStr = view.getSuatChieuSearchText().trim();
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
            JOptionPane.showMessageDialog(view, "Lỗi khi tìm kiếm suất chiếu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void loadSuatChieuList(List<SuatChieu> suatChieus) {
        DefaultTableModel model = view.getSuatChieuTableModel();
        model.setRowCount(0);
        for (SuatChieu sc : suatChieus) {
            String ngayGioChieuFormatted = sc.getNgayGioChieu() != null
                    ? sc.getNgayGioChieu().format(formatter)
                    : "Chưa có";
            model.addRow(new Object[]{
                    sc.getMaSuatChieu(),
                    sc.getTenPhim(),
                    sc.getTenPhong(),
                    ngayGioChieuFormatted,
                    sc.getSoSuatChieu()
            });
        }
    }

    private void displaySuatChieuInfo(int row) {
        DefaultTableModel model = view.getSuatChieuTableModel();
        view.getTxtMaSuatChieu().setText(model.getValueAt(row, 0).toString());
        String tenPhim = model.getValueAt(row, 1).toString();
        String tenPhong = model.getValueAt(row, 2).toString();
        String ngayGioChieu = model.getValueAt(row, 3).toString();
        String soSuatChieu = model.getValueAt(row, 4).toString();

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
        view.getTxtSoSuatChieu().setText(soSuatChieu);
    }

    private void themSuatChieu() {
        try {
            SuatChieu suatChieu = createSuatChieuFromForm();
            service.addSuatChieu(suatChieu);
            JOptionPane.showMessageDialog(view, "Thêm suất chiếu thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
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
            JOptionPane.showMessageDialog(view, "Cập nhật suất chiếu thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
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
        try {
            int maSuatChieu = Integer.parseInt(view.getTxtMaSuatChieu().getText());
            int confirm = JOptionPane.showConfirmDialog(view, "Bạn có chắc chắn muốn xóa suất chiếu này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                service.deleteSuatChieu(maSuatChieu);
                JOptionPane.showMessageDialog(view, "Xóa suất chiếu thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadSuatChieuList(service.getAllSuatChieuDetail());
                clearForm();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi xóa suất chiếu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(view, "Mã suất chiếu không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        view.getTxtMaSuatChieu().setText("");
        view.getCbMaPhim().setSelectedIndex(-1);
        view.getCbMaPhong().setSelectedIndex(-1);
        view.getTxtNgayGioChieu().setText("dd/MM/yyyy HH:mm:ss");
        view.getTxtSoSuatChieu().setText("");
        view.getSuatChieuTable().clearSelection();
    }

    private SuatChieu createSuatChieuFromForm() {
        // Retrieve selected Phim and PhongChieu from combo boxes
        Phim selectedPhim = (Phim) view.getCbMaPhim().getSelectedItem();
        PhongChieu selectedPhong = (PhongChieu) view.getCbMaPhong().getSelectedItem();

        // Validate Phim and PhongChieu selection
        if (selectedPhim == null || selectedPhong == null) {
            throw new IllegalArgumentException("Vui lòng chọn phim và phòng chiếu hợp lệ!");
        }

        // Retrieve and validate soSuatChieu from text field
        int soSuatChieu;
        try {
            String soSuatChieuText = view.getTxtSoSuatChieu().getText().trim();
            soSuatChieu = Integer.parseInt(soSuatChieuText);
            if (soSuatChieu <= 0) {
                throw new IllegalArgumentException("Số suất chiếu phải là số nguyên dương!");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Số suất chiếu phải là số nguyên hợp lệ!");
        }

        // Get ngayGioChieu and validate it
        LocalDateTime ngayGioChieu = getLocalDateTime(selectedPhim, selectedPhong);
        if (ngayGioChieu.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Ngày giờ chiếu phải sau thời điểm hiện tại!");
        }

        // Create and return new SuatChieu
        return new SuatChieu(
                0, // maSuatChieu, auto-generated by database
                selectedPhim.getMaPhim(),
                selectedPhong.getMaPhong(),
                ngayGioChieu,
                soSuatChieu
        );

        if (selectedPhim == null || selectedPhong == null) {
            throw new IllegalArgumentException("Vui lòng chọn phim và phòng chiếu!");
        }

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