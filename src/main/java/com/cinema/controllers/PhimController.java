package com.cinema.controllers;

import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.cinema.models.Phim;
import com.cinema.services.PhimService;
import com.cinema.utils.ValidationUtils;
import com.cinema.views.admin.PhimView;

public class PhimController {
    private final PhimView view;
    private final PhimService service;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private TableRowSorter<DefaultTableModel> sorter;
    private ResourceBundle messages;

    public PhimController(PhimView view) throws SQLException {
        this.view = view;
        this.service = new PhimService(view.getDatabaseConnection());
        messages = ResourceBundle.getBundle("Messages");
        initView();
        addListeners();
    }

    private void initView() {
        try {
            // Tải dữ liệu cho ComboBox trước
            loadTheLoaiToComboBox();
            loadTrangThaiToComboBox();
            loadDinhDangToComboBox();

            // Khởi tạo sorter
            sorter = new TableRowSorter<>(view.getTableModel());
            view.getTable().setRowSorter(sorter);
            
            // Tải danh sách phim
            List<Phim> phimList = service.getAllPhim();
            
            // Load dữ liệu vào bảng
            loadPhimList(phimList);
            
            // Reset trạng thái tìm kiếm
            view.getSearchField().setText("");
            sorter.setRowFilter(null);
        } catch (SQLException e) {

            e.printStackTrace();
            JOptionPane.showMessageDialog(view, 
                "Lỗi khi tải dữ liệu: " + e.getMessage(), 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public List<Phim> getAllPhim() {
        try {
            return service.getAllPhim();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi tải danh sách phim: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return List.of();
        }
    }

    private void loadTheLoaiToComboBox() throws SQLException {
        try {
            view.getCbTenTheLoai().removeAllItems();
            List<String> theLoaiList = service.getAllTheLoai();
            if (theLoaiList.isEmpty()) {
                System.out.println("Không có thể loại nào trong cơ sở dữ liệu.");
            }
            for (String theLoai : theLoaiList) {
                view.getCbTenTheLoai().addItem(theLoai);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi tải danh sách thể loại: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            throw e;
        }
    }

    private void loadTrangThaiToComboBox() throws SQLException {
        try {
            view.getCbTrangthai().removeAllItems();
            List<String> trangThaiList = service.getAllTrangThai();
            if (trangThaiList.isEmpty()) {
                System.out.println("Không có trạng thái nào trong cơ sở dữ liệu.");
            }
            for (String trangThai : trangThaiList) {
                view.getCbTrangthai().addItem(trangThai);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi tải danh sách trạng thái: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            throw e;
        }
    }

    private void loadDinhDangToComboBox() throws SQLException {
        try {
            view.getCbKieuPhim().removeAllItems();
            List<String> dinhDangList = service.getAllDinhDang();
            if (dinhDangList.isEmpty()) {
                System.out.println("Không có định dạng nào trong cơ sở dữ liệu.");
            }
            for (String dinhDang : dinhDangList) {
                view.getCbKieuPhim().addItem(dinhDang);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi tải danh sách định dạng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            throw e;
        }
    }

    private void addListeners() {
        view.getTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = view.getTable().getSelectedRow();
                if (selectedRow >= 0) {
                    // Chuyển đổi hàng được chọn từ hàng hiển thị sang hàng thực trong model
                    int modelRow = view.getTable().convertRowIndexToModel(selectedRow);
                    displayPhimInfo(modelRow);
                }
            }
        });

        view.getSearchField().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                timKiemPhim();
            }
        });

        view.getBtnThem().addActionListener(_ -> themPhim());
        view.getBtnSua().addActionListener(_ -> suaPhim());
        view.getBtnXoa().addActionListener(_ -> xoaPhim());
        view.getBtnClear().addActionListener(_ -> clearForm());
    }

    public void timKiemPhim() {
        String tuKhoa = view.getSearchText().toLowerCase();

        if (tuKhoa.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + tuKhoa, 1));
        }
    }

    private void loadPhimList(List<Phim> phimList) {
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0); // Xóa dữ liệu cũ

        // Thêm log để debug
        System.out.println("Đang tải danh sách phim với " + (phimList != null ? phimList.size() : 0) + " phim");

        if (phimList == null || phimList.isEmpty()) {
            System.out.println("Danh sách phim trống");
            return;
        }

        for (Phim phim : phimList) {
            try {
                // Format ngày theo định dạng dd/MM/yyyy
                String ngayKhoiChieuStr = phim.getNgayKhoiChieu() != null 
                    ? phim.getNgayKhoiChieu().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) 
                    : "";
                    
                model.addRow(new Object[]{
                    phim.getMaPhim(),
                    phim.getTenPhim() != null ? phim.getTenPhim() : "",
                    phim.getTenTheLoai() != null ? phim.getTenTheLoai() : "Không xác định",
                    phim.getThoiLuong(),
                    ngayKhoiChieuStr,
                    phim.getTrangThai() != null ? phim.getTrangThai() : "Không xác định"
                });
                
                System.out.println("Đã thêm phim: " + phim.getTenPhim());
            } catch (Exception e) {
                System.err.println("Lỗi khi thêm phim: " + phim.getTenPhim());
                e.printStackTrace();
            }
        }

        // Cập nhật giao diện
        view.getTable().revalidate();
        view.getTable().repaint();
    }

    private void displayPhimInfo(int row) {
        DefaultTableModel model = view.getTableModel();
        String maPhim = model.getValueAt(row, 0).toString();
        String tenPhim = model.getValueAt(row, 1).toString();
        String theLoai = model.getValueAt(row, 2).toString();
        String thoiLuong = model.getValueAt(row, 3).toString();
        String ngayKhoiChieu = model.getValueAt(row, 4).toString();
        String trangThai = model.getValueAt(row, 5).toString();

        view.getTxtMaPhim().setText(maPhim);
        view.getTxtTenPhim().setText(tenPhim);
        view.getTxtThoiLuong().setText(thoiLuong);
        view.getTxtNgayKhoiChieu().setText(ngayKhoiChieu);
        view.getCbTenTheLoai().setSelectedItem(theLoai);
        view.getCbTrangthai().setSelectedItem(trangThai);

        try {
            List<Phim> phimList = service.getAllPhim();
            Phim selectedPhim = null;
            for (Phim phim : phimList) {
                if (phim.getMaPhim() == Integer.parseInt(maPhim)) {
                    selectedPhim = phim;
                    break;
                }
            }

            if (selectedPhim != null) {
                view.getTxtNuocSanXuat().setText(selectedPhim.getNuocSanXuat());
                view.getTxtMoTa().setText(selectedPhim.getMoTa());
                view.getTxtDaoDien().setText(selectedPhim.getDaoDien());
                view.getCbKieuPhim().setSelectedItem(selectedPhim.getKieuPhim());

                if (selectedPhim.getDuongDanPoster() != null && !selectedPhim.getDuongDanPoster().isEmpty()) {
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
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi tải thông tin phim: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void themPhim() {
        try {
            if (!validateForm()) {
                showMessage(messages.getString("movieEmpty"), "warning");
                return;
            }

            Phim phim = createPhimFromForm();
            
            if (service.isMovieTitleExists(phim.getTenPhim(), 0)) {
                showMessage(messages.getString("movieTitleExists"), "error"); 
                return;
            }

            service.addPhim(phim);
            showMessage(messages.getString("movieAddSuccess"), "success");
            loadPhimList(service.getAllPhim());
            clearForm();
        } catch (Exception e) {
            showMessage(messages.getString("movieAddFailed") + ": " + e.getMessage(), "error");
        }
    }

    private void showMessage(String message, String type) {
        int messageType;
        switch(type.toLowerCase()) {
            case "success":
                messageType = JOptionPane.INFORMATION_MESSAGE;
                break;
            case "warning":
                messageType = JOptionPane.WARNING_MESSAGE;
                break;
            case "error":
                messageType = JOptionPane.ERROR_MESSAGE;
                break;
            default:
                messageType = JOptionPane.PLAIN_MESSAGE;
        }
        JOptionPane.showMessageDialog(view, message, getMessageTitle(type), messageType);
    }

    private String getMessageTitle(String type) {
        switch(type.toLowerCase()) {
            case "success":
                return messages.getString("successTitle");
            case "warning":
                return messages.getString("warningTitle");
            case "error":
                return messages.getString("errorTitle");
            default:
                return messages.getString("notificationTitle");
        }
    }    private boolean validateForm() {
        boolean isValid = true;



        // Validate thể loại
        String theLoai = (String) view.getCbTenTheLoai().getSelectedItem();
        if (theLoai == null || theLoai.isEmpty()) {
            ValidationUtils.setErrorBorder(view.getCbTenTheLoai());
            isValid = false;
        } else {
            ValidationUtils.setNormalBorder(view.getCbTenTheLoai());
        }

        // Validate trạng thái
        String trangThai = (String) view.getCbTrangthai().getSelectedItem();
        if (trangThai == null || trangThai.isEmpty()) {
            ValidationUtils.setErrorBorder(view.getCbTrangthai());
            isValid = false;
        } else {
            ValidationUtils.setNormalBorder(view.getCbTrangthai());
        }

        return isValid;
    }

    public void suaPhim() {
        try {
            if (!validateForm()) {
                showMessage(messages.getString("movieEmpty"), "warning");
                return;
            }

            Phim phim = createPhimFromForm();
            service.updatePhim(phim);
            showMessage(messages.getString("movieUpdateSuccess"), "success");
            loadPhimList(service.getAllPhim());
            clearForm();
        } catch (Exception e) {
            showMessage(messages.getString("movieUpdateFailed") + ": " + e.getMessage(), "error");
        }
    }

    public void xoaPhim() {
        if (view.getTxtMaPhim().getText().isEmpty()) {
            showMessage(messages.getString("movieNotSelected"), "warning");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            view, 
            messages.getString("deleteConfirmMessage"),
            messages.getString("confirmTitle"),
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int maPhim = Integer.parseInt(view.getTxtMaPhim().getText());
                service.deletePhim(maPhim);
                showMessage(messages.getString("movieDeleteSuccess"), "success");
                loadPhimList(service.getAllPhim());
                clearForm();
            } catch (Exception e) {
                showMessage(messages.getString("movieDeleteFailed") + ": " + e.getMessage(), "error");
            }
        }
    }

    private void clearForm() {
        view.getTxtMaPhim().setText("");
        view.getTxtTenPhim().setText("");
        view.getCbTenTheLoai().setSelectedIndex(-1);
        view.getTxtThoiLuong().setText("");
        view.getTxtNgayKhoiChieu().setText("");
        view.getTxtNuocSanXuat().setText("");
        view.getCbKieuPhim().setSelectedIndex(-1);
        view.getTxtMoTa().setText("");
        view.getTxtDaoDien().setText("");
        view.getPosterLabel().setIcon(null);
        view.getPosterLabel().setText("");
        view.clearSelectedPosterPath();
        view.getCbTrangthai().setSelectedIndex(-1);
        view.getTable().clearSelection();
    }

    private Phim createPhimFromForm() {
        String tenPhim = view.getTxtTenPhim().getText().trim();
        String tenTheLoai = (String) view.getCbTenTheLoai().getSelectedItem();
        String thoiLuongStr = view.getTxtThoiLuong().getText().trim();
        String ngayKhoiChieuStr = view.getTxtNgayKhoiChieu().getText().trim();
        String nuocSanXuat = view.getTxtNuocSanXuat().getText().trim();
        String dinhDang = (String) view.getCbKieuPhim().getSelectedItem();
        String moTa = view.getTxtMoTa().getText().trim();
        String daoDien = view.getTxtDaoDien().getText().trim();
        String posterPath = view.getSelectedPosterPath();
        String trangThai = (String) view.getCbTrangthai().getSelectedItem();

        if (tenPhim.isEmpty() || tenTheLoai == null || thoiLuongStr.isEmpty() || ngayKhoiChieuStr.isEmpty() || nuocSanXuat.isEmpty()) {
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

        LocalDate ngayKhoiChieu;
        try {
            ngayKhoiChieu = LocalDate.parse(ngayKhoiChieuStr, formatter);
            if (ngayKhoiChieu.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Ngày khởi chiếu phải sau ngày hiện tại!");
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Ngày khởi chiếu không đúng định dạng (dd/MM/yyyy)!");
        }

        return new Phim(0, tenPhim, tenTheLoai, thoiLuong, ngayKhoiChieu, nuocSanXuat, dinhDang, moTa, daoDien, posterPath, trangThai);
    }
}