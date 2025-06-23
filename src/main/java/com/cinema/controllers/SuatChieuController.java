package com.cinema.controllers;

import java.awt.Component;
import java.awt.Container;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.cinema.models.Phim;
import com.cinema.models.PhongChieu;
import com.cinema.models.SuatChieu;
import com.cinema.models.dto.CustomPaginationPanel;
import com.cinema.models.dto.PaginationResult;
import com.cinema.services.PhimService;
import com.cinema.services.PhongChieuService;
import com.cinema.services.SuatChieuService;
import com.cinema.views.admin.SuatChieuView;

public class SuatChieuController {
    private final SuatChieuView view;
    private final SuatChieuService service;
    private final PhimService phimService;
    private final PhongChieuService phongChieuService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private int currentPage = 1;
    private int pageSize = 10;
    private int totalPages = 1;

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
            loadPhimToComboBox();
            loadPhongChieuToComboBox();
            loadSuatChieuPaginated(currentPage, pageSize);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi tải dữ liệu suất chiếu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addListeners() {
        view.getSuatChieuTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = view.getSuatChieuTable().getSelectedRow();
                if (selectedRow >= 0) {
                    displaySuatChieuInfo(selectedRow);
                }
            }
        });
        view.getBtnThemSuat().addActionListener(_ -> themSuatChieu());
        view.getBtnSuaSuat().addActionListener(_ -> suaSuatChieu());
        view.getBtnXoaSuat().addActionListener(_ -> xoaSuatChieu());
        view.getBtnClearSuat().addActionListener(_ -> clearForm());
    }

    private void loadPhimToComboBox() throws SQLException {
        List<Phim> phimList = phimService.getAllPhim();
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

    private void displaySuatChieuInfo(int row) {
        DefaultTableModel model = view.getSuatChieuTableModel();
        view.getTxtMaSuatChieu().setText(model.getValueAt(row, 0).toString());
        String tenPhim = model.getValueAt(row, 1).toString();
        String tenPhong = model.getValueAt(row, 2).toString();
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
            JOptionPane.showMessageDialog(view, "Thêm suất chiếu thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadSuatChieuPaginated(currentPage, pageSize);
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
            loadSuatChieuPaginated(currentPage, pageSize);
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
                loadSuatChieuPaginated(currentPage, pageSize);
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
                ngayGioChieu
        );
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

    public void loadSuatChieuPaginated(int page, int pageSize) throws SQLException {
        PaginationResult<SuatChieu> result = service.getAllSuatChieuPaginated(page, pageSize);
        DefaultTableModel model = view.getSuatChieuTableModel();
        model.setRowCount(0);
        for (SuatChieu sc : result.getData()) {
            model.addRow(new Object[]{
                sc.getMaSuatChieu(),
                sc.getTenPhim(),
                sc.getTenPhong(),
                sc.getNgayGioChieu(),
                sc.getThoiLuongPhim(),
                sc.getKieuPhim()
            });
        }
        // Tìm pagination panel theo tên
        findPaginationPanelByName(view, "paginationPanel", result);
        
        // Đảm bảo TableRowSorter được cập nhật
        if (view.getSuatChieuTable().getRowSorter() instanceof TableRowSorter) {
            ((TableRowSorter<?>) view.getSuatChieuTable().getRowSorter()).sort();
        }
        // Đảm bảo TableRowSorter được cập nhật
        if (view.getSuatChieuTable().getRowSorter() instanceof TableRowSorter) {
            ((TableRowSorter<?>) view.getSuatChieuTable().getRowSorter()).sort();
        }
    }

    private void findPaginationPanelByName(Component component, String name, PaginationResult<SuatChieu> result) {
        if (component instanceof CustomPaginationPanel && name.equals(component.getName())) {
            CustomPaginationPanel paginationPanel = (CustomPaginationPanel) component;
            paginationPanel.updatePagination(result.getCurrentPage(), result.getTotalPages());
            System.out.println("Pagination updated: page " + result.getCurrentPage() + " of " + result.getTotalPages());
            return;
        }
        
        if (component instanceof Container) {
            Component[] children = ((Container) component).getComponents();
            for (Component child : children) {
                findPaginationPanelByName(child, name, result);
            }
        }
    }

    public int getCurrentPage() {
        return currentPage;
    }
}