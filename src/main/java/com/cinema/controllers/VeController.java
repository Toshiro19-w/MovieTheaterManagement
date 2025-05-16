package com.cinema.controllers;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import com.cinema.components.UnderlineTextField;
import com.cinema.enums.TrangThaiVe;
import com.cinema.models.KhachHang;
import com.cinema.models.PhongChieu;
import com.cinema.models.Ve;
import com.cinema.services.PhimService;
import com.cinema.services.PhongChieuService;
import com.cinema.services.SuatChieuService;
import com.cinema.services.VeService;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.ValidationUtils;
import com.cinema.views.admin.VeView;

public class VeController {
    private final VeView view;
    private final VeService service;
    private final PhimService phimService;
    private final SuatChieuService suatChieuService;
    private final ResourceBundle messages;
    private final DateTimeFormatter ngayDatFormatter;
    private final DateTimeFormatter ngayGioChieuFormatter;

    public VeController(VeView view) throws SQLException {
        this.view = view;
        this.service = new VeService(view.getDatabaseConnection());
        this.phimService = new PhimService(view.getDatabaseConnection());
        this.suatChieuService = new SuatChieuService(view.getDatabaseConnection());
        this.messages = ResourceBundle.getBundle("Messages");
        this.ngayDatFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        this.ngayGioChieuFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        
        loadPhongChieuList();
        loadKhuyenMaiList();
        initView();
        addListeners();
        addValidationListeners();
    }

    private void initView() {
        try {
            loadVeList(service.getAllVeDetail());
        } catch (SQLException e) {
            handleException("Lỗi khi tải dữ liệu vé", e);
        }
    }

    private void addListeners() {
        view.getSearchField().addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                searchVe();
            }
        });

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
        
        view.getBtnThem().addActionListener(_ -> themVe());
        view.getBtnSua().addActionListener(_ -> suaVe());
        view.getBtnXoa().addActionListener(_ -> xoaVe());
        view.getBtnClear().addActionListener(_ -> clearForm());
        view.getBtnRefresh().addActionListener(_ -> refreshData());

        view.getCbTenPhong().addActionListener(e -> updateTicketPrices());
        view.getCbNgayGioChieu().addActionListener(e -> updateTicketPrices());
        view.getCbKhuyenMai().addActionListener(e -> updateTicketPrices());
    }

    private void addValidationListeners() {
        // Validate seat code
        UnderlineTextField txtSoGhe = (UnderlineTextField) view.getTxtSoGhe();
        txtSoGhe.getDocument().addDocumentListener(new DocumentListener() {
            private void validate() {
                String soGhe = txtSoGhe.getText().trim();
                if (!ValidationUtils.isValidSeatCode(soGhe)) {
                    ValidationUtils.showError(view.getSoGheErrorLabel(), messages.getString("seatInvalid"));
                    ValidationUtils.setErrorBorder(txtSoGhe);
                } else {
                    ValidationUtils.hideError(view.getSoGheErrorLabel());
                    ValidationUtils.setNormalBorder(txtSoGhe);
                    updateTicketPrices();
                }
            }
            @Override
            public void insertUpdate(DocumentEvent e) { validate(); }
            @Override
            public void removeUpdate(DocumentEvent e) { validate(); }
            @Override
            public void changedUpdate(DocumentEvent e) { validate(); }
        });

        // Validate room selection
        view.getCbTenPhong().addActionListener(e -> {
            String tenPhong = (String) view.getCbTenPhong().getSelectedItem();
            if (!ValidationUtils.isValidString(tenPhong)) {
                ValidationUtils.showError(view.getTenPhongErrorLabel(), messages.getString("roomEmpty"));
                ValidationUtils.setErrorBorder(view.getCbTenPhong());
            } else {
                ValidationUtils.hideError(view.getTenPhongErrorLabel());
                ValidationUtils.setNormalBorder(view.getCbTenPhong());
                loadPhimByPhong(tenPhong);
            }
        });

        // Validate showtime selection
        view.getCbNgayGioChieu().addActionListener(e -> {
            String ngayGioChieu = (String) view.getCbNgayGioChieu().getSelectedItem();
            if (!ValidationUtils.isValidDateTime(ngayGioChieu)) {
                ValidationUtils.showError(view.getNgayGioChieuErrorLabel(), messages.getString("invalidDateFormat"));
                ValidationUtils.setErrorBorder(view.getCbNgayGioChieu());
            } else {
                ValidationUtils.hideError(view.getNgayGioChieuErrorLabel());
                ValidationUtils.setNormalBorder(view.getCbNgayGioChieu());
            }
        });

        // Validate movie selection
        view.getCbTenPhim().addActionListener(e -> {
            String tenPhim = (String) view.getCbTenPhim().getSelectedItem();
            if (!ValidationUtils.isValidString(tenPhim)) {
                ValidationUtils.showError(view.getTenPhimErrorLabel(), messages.getString("movieTitleEmpty"));
                ValidationUtils.setErrorBorder(view.getCbTenPhim());
            } else {
                ValidationUtils.hideError(view.getTenPhimErrorLabel());
                ValidationUtils.setNormalBorder(view.getCbTenPhim());
            }
        });

        // Validate promotion
        view.getCbKhuyenMai().addActionListener(e -> {
            String tenKhuyenMai = (String) view.getCbKhuyenMai().getSelectedItem();
            if (tenKhuyenMai != null && !tenKhuyenMai.isEmpty() && !tenKhuyenMai.equals("Không có")) {
                try {
                    Integer maKhuyenMai = service.getMaKhuyenMai(tenKhuyenMai);
                    if (maKhuyenMai == null) {
                        ValidationUtils.showError(view.getKhuyenMaiErrorLabel(), messages.getString("promotionInvalid"));
                        ValidationUtils.setErrorBorder(view.getCbKhuyenMai());
                    } else {
                        ValidationUtils.hideError(view.getKhuyenMaiErrorLabel());
                        ValidationUtils.setNormalBorder(view.getCbKhuyenMai());
                    }
                } catch (SQLException ex) {
                    ValidationUtils.showError(view.getKhuyenMaiErrorLabel(), messages.getString("databaseError"));
                    ValidationUtils.setErrorBorder(view.getCbKhuyenMai());
                }
            } else {
                ValidationUtils.hideError(view.getKhuyenMaiErrorLabel());
                ValidationUtils.setNormalBorder(view.getCbKhuyenMai());
            }
        });
    }

    private void updateTicketPrices() {
        try {
            String soGhe = view.getTxtSoGhe().getText().trim();
            String tenPhong = (String) view.getCbTenPhong().getSelectedItem();
            String ngayGioChieuStr = (String) view.getCbNgayGioChieu().getSelectedItem();
            String tenKhuyenMai = (String) view.getCbKhuyenMai().getSelectedItem();

            if (ValidationUtils.isValidSeatCode(soGhe) && ValidationUtils.isValidString(tenPhong) && ValidationUtils.isValidDateTime(ngayGioChieuStr)) {
                LocalDateTime ngayGioChieu = LocalDateTime.parse(ngayGioChieuStr, ngayGioChieuFormatter);
                Integer maSuatChieu = service.getMaSuatChieu(ngayGioChieu, tenPhong);
                if (maSuatChieu != null) {
                    Ve tempVe = new Ve();
                    tempVe.setSoGhe(soGhe);
                    service.calculateTicketPrices(tempVe, maSuatChieu, tenKhuyenMai);
                    view.getTxtGiaVeGoc().setText(formatCurrency(tempVe.getGiaVeGoc()));
                    view.getTxtTienGiam().setText(formatCurrency(tempVe.getTienGiam()));
                    view.getTxtGiaVeSauGiam().setText(formatCurrency(tempVe.getGiaVeSauGiam()));
                } else {
                    clearPriceFields();
                }
            } else {
                clearPriceFields();
            }
        } catch (SQLException | DateTimeParseException e) {
            clearPriceFields();
            handleException("Lỗi khi tính giá vé: " + e.getMessage(), e);
        }
    }

    private void clearPriceFields() {
        view.getTxtGiaVeGoc().setText("");
        view.getTxtTienGiam().setText("");
        view.getTxtGiaVeSauGiam().setText("");
    }

    private void loadVeList(List<Ve> veList) {
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);
        for (Ve ve : veList) {
            model.addRow(new Object[]{
                ve.getMaVe(),
                ve.getTrangThai().toString(),
                ve.getSoGhe(),
                formatCurrency(ve.getGiaVeGoc()),
                formatCurrency(ve.getTienGiam()),
                formatCurrency(ve.getGiaVeSauGiam()),
                ve.getNgayDat() != null ? ve.getNgayDat().format(ngayDatFormatter) : "Chưa đặt",
                ve.getTenPhong() != null ? ve.getTenPhong() : "Chưa đặt",
                ve.getNgayGioChieu() != null ? ve.getNgayGioChieu().format(ngayGioChieuFormatter) : "Chưa có",
                ve.getTenPhim(),
                ve.getTenKhuyenMai() != null ? ve.getTenKhuyenMai() : "Không có"
            });
        }
    }  
    
    private void displayVeInfo(int selectedRow) {
        if (selectedRow < 0) return;

        try {
            DefaultTableModel model = view.getTableModel();
            int modelRow = view.getTable().convertRowIndexToModel(selectedRow);
            
            String maVe = model.getValueAt(modelRow, 0).toString();
            String tenKhuyenMai = model.getValueAt(modelRow, 10).toString();
            
            view.getTxtMaVe().setText(maVe);
            view.getCbTrangThai().setSelectedItem(model.getValueAt(modelRow, 1).toString());
            view.getTxtSoGhe().setText(model.getValueAt(modelRow, 2).toString());
            view.getTxtGiaVeGoc().setText(model.getValueAt(modelRow, 3).toString());
            view.getTxtTienGiam().setText(model.getValueAt(modelRow, 4).toString());
            view.getTxtGiaVeSauGiam().setText(model.getValueAt(modelRow, 5).toString());
            view.getTxtNgayDat().setText(model.getValueAt(modelRow, 6).toString());
            view.getCbTenPhong().setSelectedItem(model.getValueAt(modelRow, 7).toString());
            view.getCbNgayGioChieu().setSelectedItem(model.getValueAt(modelRow, 8).toString());
            view.getCbTenPhim().setSelectedItem(model.getValueAt(modelRow, 9).toString());
            
            // Check if promotion is expired
            if (!tenKhuyenMai.equals("Không có") && !service.isPromotionValid(tenKhuyenMai)) {
                view.getCbKhuyenMai().setEnabled(false);
                view.getCbKhuyenMai().removeAllItems();
                view.getCbKhuyenMai().addItem(tenKhuyenMai);
                view.getCbKhuyenMai().setSelectedItem(tenKhuyenMai);
                ValidationUtils.showError(view.getKhuyenMaiErrorLabel(), "Khuyến mãi đã hết hạn, không thể chỉnh sửa");
            } else {
                view.getCbKhuyenMai().setEnabled(true);
                loadKhuyenMaiList();
                view.getCbKhuyenMai().setSelectedItem(tenKhuyenMai);
            }

            updateCustomerInfo(Integer.parseInt(maVe));
        } catch (SQLException e) {
            handleException("Lỗi khi kiểm tra trạng thái khuyến mãi", e);
        } catch (NumberFormatException e) {
            handleException("Lỗi định dạng số", e);
        }
    }

    private void displayVeInfo(Ve ve) {
        if (ve == null) return;
        
        view.getTxtMaVe().setText(String.valueOf(ve.getMaVe()));
        view.getCbTrangThai().setSelectedItem(ve.getTrangThai().toString());
        view.getTxtSoGhe().setText(ve.getSoGhe());
        view.getTxtNgayDat().setText(ve.getNgayDat() != null ? 
            ve.getNgayDat().format(ngayDatFormatter) : "");
        view.getTxtGiaVeGoc().setText(formatCurrency(ve.getGiaVeGoc()));
        view.getTxtGiaVeSauGiam().setText(formatCurrency(ve.getGiaVeSauGiam()));
        view.getTxtTienGiam().setText(formatCurrency(ve.getTienGiam()));
        view.getCbTenPhong().setSelectedItem(ve.getTenPhong());
        view.getCbTenPhim().setSelectedItem(ve.getTenPhim());
        view.getCbNgayGioChieu().setSelectedItem(ve.getNgayGioChieu() != null ?
            ve.getNgayGioChieu().format(ngayGioChieuFormatter) : "");
        
        String tenKhuyenMai = ve.getTenKhuyenMai() != null ? ve.getTenKhuyenMai() : "Không có";
        try {
            if (!tenKhuyenMai.equals("Không có") && !service.isPromotionValid(tenKhuyenMai)) {
                view.getCbKhuyenMai().setEnabled(false);
                view.getCbKhuyenMai().removeAllItems();
                view.getCbKhuyenMai().addItem(tenKhuyenMai);
                view.getCbKhuyenMai().setSelectedItem(tenKhuyenMai);
                ValidationUtils.showError(view.getKhuyenMaiErrorLabel(), "Khuyến mãi đã hết hạn, không thể chỉnh sửa");
            } else {
                view.getCbKhuyenMai().setEnabled(true);
                loadKhuyenMaiList();
                view.getCbKhuyenMai().setSelectedItem(tenKhuyenMai);
            }
        } catch (SQLException e) {
            handleException("Lỗi khi kiểm tra trạng thái khuyến mãi", e);
        }

        if (ve.getMaHoaDon() != 0) {
            updateCustomerInfo(ve.getMaVe());
        } else {
            clearCustomerInfo();
        }
    }

    private void updateCustomerInfo(int maVe) {
        try {
            KhachHang khachHang = service.getKhachHangByMaVe(maVe);
            DefaultTableModel model = view.getTableKhachHangModel();
            model.setRowCount(0);
            if (khachHang != null) {
                model.addRow(new Object[]{
                    khachHang.getHoTen(),
                    khachHang.getSoDienThoai(),
                    khachHang.getEmail(),
                    khachHang.getDiemTichLuy()
                });
            }
        } catch (SQLException e) {
            handleException("Lỗi khi tải thông tin khách hàng", e);
        }
    }    
    
    private void themVe() {
        try {
            if (!validateForm()) {
                return;
            }
            Ve ve = createVeFromForm();
            String tenPhong = (String) view.getCbTenPhong().getSelectedItem();
            String tenKhuyenMai = (String) view.getCbKhuyenMai().getSelectedItem();
            String ngayGioChieuStr = (String) view.getCbNgayGioChieu().getSelectedItem();
            LocalDateTime ngayGioChieu = ngayGioChieuStr != null && !ngayGioChieuStr.isEmpty() ?
                LocalDateTime.parse(ngayGioChieuStr, ngayGioChieuFormatter) : null;

            Ve result = service.saveVe(ve, tenPhong, tenKhuyenMai, ngayGioChieu);
            if (result != null) {
                showSuccess("Thêm vé thành công!");
                loadVeList(service.getAllVeDetail());
                clearForm();
            }
        } catch (SQLException e) {
            handleException("Lỗi khi thêm vé vào cơ sở dữ liệu: " + e.getMessage(), e);
        } catch (DateTimeParseException e) {
            handleException("Lỗi định dạng ngày giờ", e);
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            handleException("Lỗi không xác định khi thêm vé", e);
        }
    }

    private void suaVe() {
        try {
            if (view.getTxtMaVe().getText().isEmpty()) {
                showError("Vui lòng chọn vé cần sửa!");
                return;
            }
            if (!validateForm()) {
                return;
            }
            Ve ve = createVeFromForm();
            ve.setMaVe(Integer.parseInt(view.getTxtMaVe().getText()));
            String tenPhong = (String) view.getCbTenPhong().getSelectedItem();
            String tenKhuyenMai = (String) view.getCbKhuyenMai().getSelectedItem();
            String ngayGioChieuStr = (String) view.getCbNgayGioChieu().getSelectedItem();
            LocalDateTime ngayGioChieu = ngayGioChieuStr != null && !ngayGioChieuStr.isEmpty() ?
                LocalDateTime.parse(ngayGioChieuStr, ngayGioChieuFormatter) : null;

            Ve result = service.updateVe(ve, tenPhong, tenKhuyenMai, ngayGioChieu);
            if (result != null) {
                showSuccess("Cập nhật vé thành công!");
                loadVeList(service.getAllVeDetail());
                clearForm();
            }
        } catch (SQLException e) {
            handleException("Lỗi khi cập nhật vé trong cơ sở dữ liệu: " + e.getMessage(), e);
        } catch (NumberFormatException e) {
            handleException("Lỗi định dạng mã vé", e);
        } catch (DateTimeParseException e) {
            handleException("Lỗi định dạng ngày giờ", e);
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            handleException("Lỗi không xác định khi cập nhật vé", e);
        }
    }

    private void xoaVe() {
        if (view.getTxtMaVe().getText().isEmpty()) {
            showError("Vui lòng chọn vé cần xóa!");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(view,
            "Bạn có chắc chắn muốn xóa vé này?",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int maVe = Integer.parseInt(view.getTxtMaVe().getText());
                service.deleteVe(maVe);
                showSuccess("Xóa vé thành công!");
                loadVeList(service.getAllVeDetail());
                clearForm();
            } catch (SQLException e) {
                handleException("Lỗi khi xóa vé", e);
            }
        }
    }

    private void searchVe() {
        String searchText = view.getSearchField().getText().trim().toLowerCase();
        try {
            if (searchText.isEmpty()) {
                loadVeList(service.getAllVeDetail());
            } else {
                List<Ve> allVe = service.getAllVeDetail();
                List<Ve> filtered = new java.util.ArrayList<>();
                for (Ve ve : allVe) {
                    if (String.valueOf(ve.getMaVe()).toLowerCase().contains(searchText)
                        || ve.getSoGhe().toLowerCase().contains(searchText)
                        || (ve.getTenPhim() != null && ve.getTenPhim().toLowerCase().contains(searchText))
                        || (ve.getTenPhong() != null && ve.getTenPhong().toLowerCase().contains(searchText))
                        || ve.getTrangThai().toString().toLowerCase().contains(searchText)
                        || (ve.getTenKhuyenMai() != null && ve.getTenKhuyenMai().toLowerCase().contains(searchText))) {
                        filtered.add(ve);
                    }
                }
                loadVeList(filtered);
            }
        } catch (SQLException e) {
            handleException("Lỗi khi tìm kiếm vé", e);
        }
    }

    private void refreshData() {
        try {
            loadVeList(service.getAllVeDetail());
            loadKhuyenMaiList();
            showSuccess("Dữ liệu đã được cập nhật!");
        } catch (SQLException e) {
            handleException("Lỗi khi cập nhật dữ liệu", e);
        }
    }

    private void clearForm() {
        view.clearForm();
        view.getCbKhuyenMai().setEnabled(true);
        loadKhuyenMaiList();
    }

    private void clearCustomerInfo() {
        DefaultTableModel model = view.getTableKhachHangModel();
        model.setRowCount(0);
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0 VND";
        java.text.DecimalFormat formatter = new java.text.DecimalFormat("#,### VND");
        return formatter.format(amount);
    }

    private Ve createVeFromForm() throws DateTimeParseException {
        Ve ve = new Ve();
        
        String trangThaiStr = view.getCbTrangThai().getSelectedItem().toString().toUpperCase();
        ve.setTrangThai(TrangThaiVe.valueOf(trangThaiStr));
        ve.setSoGhe(view.getTxtSoGhe().getText().trim());
        
        return ve;
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();
        
        String soGhe = view.getTxtSoGhe().getText().trim();
        if (!ValidationUtils.isValidSeatCode(soGhe)) {
            errors.append("- ").append(messages.getString("seatInvalid")).append("\n");
        }

        String tenPhong = (String) view.getCbTenPhong().getSelectedItem();
        if (!ValidationUtils.isValidString(tenPhong)) {
            errors.append("- ").append(messages.getString("roomEmpty")).append("\n");
        }

        String ngayGioChieu = (String) view.getCbNgayGioChieu().getSelectedItem();
        if (!ValidationUtils.isValidDateTime(ngayGioChieu)) {
            errors.append("- ").append(messages.getString("invalidDateFormat")).append("\n");
        }

        String tenPhim = (String) view.getCbTenPhim().getSelectedItem();
        if (!ValidationUtils.isValidString(tenPhim)) {
            errors.append("- ").append(messages.getString("movieTitleEmpty")).append("\n");
        }

        String tenKhuyenMai = (String) view.getCbKhuyenMai().getSelectedItem();
        if (tenKhuyenMai != null && !tenKhuyenMai.isEmpty() && !tenKhuyenMai.equals("Không có")) {
            try {
                Integer maKhuyenMai = service.getMaKhuyenMai(tenKhuyenMai);
                if (maKhuyenMai == null) {
                    errors.append("- ").append(messages.getString("promotionInvalid")).append("\n");
                }
            } catch (SQLException e) {
                errors.append("- Lỗi khi kiểm tra khuyến mãi: ").append(e.getMessage()).append("\n");
            }
        }

        try {
            if (ValidationUtils.isValidDateTime(ngayGioChieu) && ValidationUtils.isValidSeatCode(soGhe) && ValidationUtils.isValidString(tenPhong)) {
                LocalDateTime ngayGioChieuParsed = LocalDateTime.parse(ngayGioChieu, ngayGioChieuFormatter);
                Integer maSuatChieu = service.getMaSuatChieu(ngayGioChieuParsed, tenPhong);
                if (maSuatChieu != null && service.isGheDaDat(maSuatChieu, soGhe)) {
                    errors.append("- Ghế đã được đặt cho suất chiếu này\n");
                }
            }
        } catch (SQLException | DateTimeParseException e) {
            errors.append("- Lỗi khi kiểm tra trạng thái ghế: ").append(e.getMessage()).append("\n");
        }

        if (errors.length() > 0) {
            showError(errors.toString());
            return false;
        }
        return true;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(view, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(view, message, "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleException(String message, Exception e) {
        System.err.println(message);
        System.err.println("Chi tiết lỗi:");
        e.printStackTrace(System.err);
        showError(message);
    }

    public void refreshView() {
        try {
            loadVeList(service.getAllVeDetail());
            loadKhuyenMaiList();
        } catch (SQLException e) {
            handleException("Không thể làm mới dữ liệu", e);
        }
    }

    private void loadSuatChieuByPhongVaPhim(String tenPhong, String tenPhim) {
        try {
            List<String> suatChieuList = suatChieuService.getThoiGianChieuByPhongVaPhim(tenPhong, tenPhim);
            JComboBox<String> cbNgayGioChieu = view.getCbNgayGioChieu();
            cbNgayGioChieu.removeAllItems();
            for (String thoiGian : suatChieuList) {
                cbNgayGioChieu.addItem(thoiGian);
            }
        } catch (Exception ex) {
            handleException("Lỗi khi tải suất chiếu", ex);
        }
    }

    private void loadPhongChieuList() {
        try {
            List<PhongChieu> phongChieuList = new PhongChieuService(view.getDatabaseConnection()).getAllPhongChieu();
            JComboBox<String> cbTenPhong = view.getCbTenPhong();
            cbTenPhong.removeAllItems();
            for (PhongChieu pc : phongChieuList) {
                cbTenPhong.addItem(pc.getTenPhong());
            }
            if (cbTenPhong.getItemCount() > 0) {
                cbTenPhong.setSelectedIndex(0);
                loadPhimByPhong((String) cbTenPhong.getSelectedItem());
            }
        } catch (Exception e) {
            handleException("Lỗi khi tải danh sách phòng chiếu", e);
        }
    }

    private void loadPhimByPhong(String tenPhong) {
        try {
            List<com.cinema.models.Phim> phimList = phimService.getPhimByTenPhong(tenPhong);
            JComboBox<String> cbTenPhim = view.getCbTenPhim();
            cbTenPhim.removeAllItems();
            for (com.cinema.models.Phim phim : phimList) {
                cbTenPhim.addItem(phim.getTenPhim());
            }
            if (cbTenPhim.getItemCount() > 0) {
                cbTenPhim.setSelectedIndex(0);
                loadSuatChieuByPhongVaPhim(tenPhong, (String) cbTenPhim.getSelectedItem());
            }
        } catch (Exception ex) {
            handleException("Lỗi khi tải danh sách phim", ex);
        }
    }

    private void loadKhuyenMaiList() {
        try {
            List<String> promotions = service.getValidPromotions();
            JComboBox<String> cbKhuyenMai = view.getCbKhuyenMai();
            cbKhuyenMai.removeAllItems();
            for (String promotion : promotions) {
                cbKhuyenMai.addItem(promotion);
            }
            cbKhuyenMai.setSelectedItem("Không có");
        } catch (SQLException e) {
            handleException("Lỗi khi tải danh sách khuyến mãi", e);
        }
    }
}