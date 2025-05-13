package com.cinema.controllers;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
import com.cinema.utils.ValidationUtils;
import com.cinema.views.admin.VeView;

public class VeController {
    private final VeView view;
    private final VeService service;
    private final PhimService phimService;
    private final DateTimeFormatter ngayDatFormatter;
    private final DateTimeFormatter ngayGioChieuFormatter;

    public VeController(VeView view) throws SQLException {
        this.view = view;
        this.service = new VeService(view.getDatabaseConnection());
        this.phimService = new PhimService(view.getDatabaseConnection());
        this.ngayDatFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        this.ngayGioChieuFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        
        loadPhongChieuList();
        initView();
        addListeners();
        addSoGheValidation();
        addGiaVeValidation();
        addPhongChieuListener();
        addPhimListener();
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
        });        view.getBtnThem().addActionListener(_ -> themVe());
        view.getBtnSua().addActionListener(_ -> suaVe());
        view.getBtnXoa().addActionListener(_ -> xoaVe());
        view.getBtnClear().addActionListener(_ -> clearForm());
        view.getBtnRefresh().addActionListener(_ -> refreshData());
    }

    private void loadVeList(List<Ve> veList) {
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);
        for (Ve ve : veList) {
            model.addRow(new Object[]{
                ve.getMaVe(),
                ve.getTrangThai().toString().toLowerCase(),
                formatCurrency(ve.getGiaVe()),
                ve.getSoGhe(),
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
            
            view.getTxtMaVe().setText(model.getValueAt(modelRow, 0).toString());
            view.getCbTrangThai().setSelectedItem(model.getValueAt(modelRow, 1).toString());
            view.getTxtGiaVe().setText(model.getValueAt(modelRow, 2).toString().replaceAll("[^\\d]", ""));
            view.getTxtSoGhe().setText(model.getValueAt(modelRow, 3).toString());
            view.getTxtNgayDat().setText(model.getValueAt(modelRow, 4).toString());
            view.getCbTenPhong().setToolTipText(model.getValueAt(modelRow, 5).toString());
            view.getCbNgayGioChieu().setToolTipText(model.getValueAt(modelRow, 6).toString());
            view.getCbTenPhim().setToolTipText(model.getValueAt(modelRow, 7).toString());
            view.getTxtKhuyenMai().setText(model.getValueAt(modelRow, 8).toString());

            updateCustomerInfo(Integer.parseInt(view.getTxtMaVe().getText()));
        } catch (NumberFormatException e) {
            handleException("Lỗi định dạng số", e);
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
                    khachHang.getEmail(),
                    khachHang.getDiemTichLuy()
                });
            }
        } catch (SQLException e) {
            handleException("Lỗi khi tải thông tin khách hàng", e);
        }
    }    private void themVe() {
        try {
            if (!validateForm()) {
                return;
            }
            Ve ve = createVeFromForm();
            Ve result = service.saveVe(ve);
            if (result != null) {
                showSuccess("Thêm vé thành công!");
                loadVeList(service.getAllVeDetail());
                clearForm();
            }
        } catch (SQLException e) {
            handleException("Lỗi khi thêm vé vào cơ sở dữ liệu", e);
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
            
            Ve result = service.updateVe(ve);
            if (result != null) {
                showSuccess("Cập nhật vé thành công!");
                loadVeList(service.getAllVeDetail());
                clearForm();
            }
        } catch (SQLException e) {
            handleException("Lỗi khi cập nhật vé trong cơ sở dữ liệu", e);
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
            showSuccess("Dữ liệu đã được cập nhật!");
        } catch (SQLException e) {
            handleException("Lỗi khi cập nhật dữ liệu", e);
        }
    }

    private void clearForm() {
        view.getTxtMaVe().setText("");
        view.getCbTrangThai().setSelectedIndex(0);
        view.getTxtGiaVe().setText("");
        view.getTxtSoGhe().setText("");
        view.getTxtNgayDat().setText("");
        view.getCbTenPhong().setToolTipText("");
        view.getCbNgayGioChieu().setToolTipText("");
        view.getCbTenPhim().setToolTipText("");
        clearCustomerInfo();
    }

    private void clearCustomerInfo() {
        DefaultTableModel model = view.getTableKhachHangModel();
        model.setRowCount(0);
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0 VND";
        java.text.DecimalFormat formatter = new java.text.DecimalFormat("#,### VND");
        return formatter.format(amount);
    }    // Currency formatting moved to formatCurrency method

    private Ve createVeFromForm() throws DateTimeParseException {
        Ve ve = new Ve();
        
        // Set trạng thái
        String trangThaiStr = view.getCbTrangThai().getSelectedItem().toString().toUpperCase();
        ve.setTrangThai(TrangThaiVe.valueOf(trangThaiStr));
        
        // Set giá vé
        try {
            BigDecimal giaVe = new BigDecimal(view.getTxtGiaVe().getText().trim());
            ve.setGiaVe(giaVe);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Giá vé không hợp lệ");
        }
        
        // Get common values
        String soGhe = view.getTxtSoGhe().getText().trim();
        String tenPhong = (String) view.getCbTenPhong().getSelectedItem();
        
        if (soGhe.isEmpty() || tenPhong == null || tenPhong.trim().isEmpty()) {
            throw new IllegalArgumentException("Số ghế và tên phòng không được để trống");
        }

        // Set số ghế
        ve.setSoGhe(soGhe);
        
        // Set ngày đặt
        String ngayDatStr = view.getTxtNgayDat().getText().trim();
        if (ngayDatStr.isEmpty()) {
            ve.setNgayDat(LocalDateTime.now());
        } else {
            ve.setNgayDat(LocalDateTime.parse(ngayDatStr, ngayDatFormatter));
        }
        
        // Set mã ghế
        String sqlGhe = "SELECT maGhe FROM Ghe g JOIN PhongChieu pc ON g.maPhong = pc.maPhong WHERE g.soGhe = ? AND pc.tenPhong = ?";
        try (PreparedStatement stmt = view.getDatabaseConnection().getConnection().prepareStatement(sqlGhe)) {
            stmt.setString(1, soGhe);
            stmt.setString(2, tenPhong);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                ve.setMaGhe(rs.getInt("maGhe"));
            } else {
                throw new IllegalArgumentException("Không tìm thấy ghế phù hợp");
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException("Lỗi khi lấy mã ghế: " + e.getMessage());
        }
        
        // Set khuyến mãi (nếu có)
        String tenKhuyenMai = view.getTxtKhuyenMai().getText().trim();
        if (!tenKhuyenMai.isEmpty() && !tenKhuyenMai.equals("Không có")) {
            String sqlKm = "SELECT maKhuyenMai FROM KhuyenMai WHERE tenKhuyenMai = ? AND trangThai = 'HoatDong' AND NOW() BETWEEN ngayBatDau AND ngayKetThuc";
            try (PreparedStatement stmt = view.getDatabaseConnection().getConnection().prepareStatement(sqlKm)) {
                stmt.setString(1, tenKhuyenMai);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    ve.setMaKhuyenMai(rs.getInt("maKhuyenMai"));
                    ve.setTenKhuyenMai(tenKhuyenMai);
                } else {
                    throw new IllegalArgumentException("Khuyến mãi không hợp lệ hoặc đã hết hạn");
                }
            } catch (SQLException e) {
                throw new IllegalArgumentException("Lỗi khi lấy mã khuyến mãi: " + e.getMessage());
            }
        }
        
        return ve;
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();
        
        // Kiểm tra giá vé
        String giaVe = view.getTxtGiaVe().getText().trim();
        if (giaVe.isEmpty()) {
            errors.append("- Giá vé không được để trống\n");
        } else if (!ValidationUtils.isValidNumber(giaVe)) {
            errors.append("- Giá vé phải là số\n");
        }

        // Kiểm tra số ghế
        String soGhe = view.getTxtSoGhe().getText().trim();
        if (soGhe.isEmpty()) {
            errors.append("- Số ghế không được để trống\n");
        }

        // Kiểm tra tên phòng
        String tenPhong = (String) view.getCbTenPhong().getSelectedItem();
        if (tenPhong == null || tenPhong.trim().isEmpty()) {
            errors.append("- Tên phòng không được để trống\n");
        }

        // Kiểm tra khuyến mãi nếu có
        String tenKhuyenMai = view.getTxtKhuyenMai().getText().trim();
        if (!tenKhuyenMai.isEmpty() && !tenKhuyenMai.equals("Không có")) {
            try {
                Integer maKhuyenMai = service.getMaKhuyenMai(tenKhuyenMai);
                if (maKhuyenMai == null) {
                    errors.append("- Khuyến mãi không hợp lệ hoặc đã hết hạn\n");
                }
            } catch (SQLException e) {
                errors.append("- Lỗi khi kiểm tra khuyến mãi: ").append(e.getMessage()).append("\n");
            }
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
        showError(message + (e.getMessage() != null ? ": " + e.getMessage() : ""));
    }

    public void refreshView() {
        try {
            loadVeList(service.getAllVeDetail());
        } catch (SQLException e) {
            handleException("Không thể làm mới dữ liệu", e);
        }
    }

    private void addSoGheValidation() {
        UnderlineTextField txtSoGhe = (UnderlineTextField) view.getTxtSoGhe();
        JLabel soGheErrorLabel = view.getSoGheErrorLabel();
        ResourceBundle messages = ResourceBundle.getBundle("Messages");
        txtSoGhe.getDocument().addDocumentListener(new DocumentListener() {
            private void validate() {
                String value = txtSoGhe.getText().trim();
                if (value.isEmpty()) {
                    soGheErrorLabel.setText(messages.getString("seatInvalid"));
                    soGheErrorLabel.setVisible(true);
                    txtSoGhe.setError(true);
                } else if (!ValidationUtils.isValidSeatCode(value)) {
                    soGheErrorLabel.setText(messages.getString("seatInvalid"));
                    soGheErrorLabel.setVisible(true);
                    txtSoGhe.setError(true);
                } else {
                    soGheErrorLabel.setText("");
                    soGheErrorLabel.setVisible(false);
                    txtSoGhe.setError(false);
                }
            }
            @Override
            public void insertUpdate(DocumentEvent e) { validate(); }
            @Override
            public void removeUpdate(DocumentEvent e) { validate(); }
            @Override
            public void changedUpdate(DocumentEvent e) { validate(); }
        });
    }

    private void addGiaVeValidation() {
        UnderlineTextField txtGiaVe = (UnderlineTextField) view.getTxtGiaVe();
        JLabel giaVeErrorLabel = view.getGiaVeErrorLabel();
        ResourceBundle messages = ResourceBundle.getBundle("Messages");
        txtGiaVe.getDocument().addDocumentListener(new DocumentListener() {
            private void validate() {
                String value = txtGiaVe.getText().trim();
                if (value.isEmpty()) {
                    giaVeErrorLabel.setText(messages.getString("ticketPriceInvalid"));
                    giaVeErrorLabel.setVisible(true);
                    txtGiaVe.setError(true);
                } else if (!ValidationUtils.isValidTicketPrice(value)) {
                    giaVeErrorLabel.setText(messages.getString("ticketPriceInvalid"));
                    giaVeErrorLabel.setVisible(true);
                    txtGiaVe.setError(true);
                } else {
                    giaVeErrorLabel.setText("");
                    giaVeErrorLabel.setVisible(false);
                    txtGiaVe.setError(false);
                }
            }
            @Override
            public void insertUpdate(DocumentEvent e) { validate(); }
            @Override
            public void removeUpdate(DocumentEvent e) { validate(); }
            @Override
            public void changedUpdate(DocumentEvent e) { validate(); }
        });
    }

    private void addPhongChieuListener() {
        JComboBox<String> cbTenPhong = view.getCbTenPhong();
        cbTenPhong.addActionListener(e -> {
            String tenPhong = (String) cbTenPhong.getSelectedItem();
            if (tenPhong != null && !tenPhong.isEmpty()) {
                loadPhimByPhong(tenPhong);
                // Sau khi load phim, cũng load luôn suất chiếu nếu có phim đầu tiên
                JComboBox<String> cbTenPhim = view.getCbTenPhim();
                if (cbTenPhim.getItemCount() > 0) {
                    cbTenPhim.setSelectedIndex(0);
                    loadSuatChieuByPhongVaPhim(tenPhong, (String) cbTenPhim.getSelectedItem());
                }
            }
        });
    }

    private void addPhimListener() {
        JComboBox<String> cbTenPhim = view.getCbTenPhim();
        cbTenPhim.addActionListener(e -> {
            String tenPhong = (String) view.getCbTenPhong().getSelectedItem();
            String tenPhim = (String) cbTenPhim.getSelectedItem();
            if (tenPhong != null && !tenPhong.isEmpty() && tenPhim != null && !tenPhim.isEmpty()) {
                loadSuatChieuByPhongVaPhim(tenPhong, tenPhim);
            }
        });
    }

    private void loadSuatChieuByPhongVaPhim(String tenPhong, String tenPhim) {
        try {
            SuatChieuService suatChieuService = new SuatChieuService(view.getDatabaseConnection());
            List<String> suatChieuList = suatChieuService.getThoiGianChieuByPhongVaPhim(tenPhong, tenPhim);
            JComboBox<String> cbNgayGioChieu = view.getCbNgayGioChieu();
            cbNgayGioChieu.removeAllItems();
            for (String thoiGian : suatChieuList) {
                cbNgayGioChieu.addItem(thoiGian);
            }
        } catch (Exception ex) {
            // Có thể show toast lỗi hoặc log
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
            // Nếu có phòng đầu tiên thì nạp phim cho phòng đó luôn
            if (cbTenPhong.getItemCount() > 0) {
                cbTenPhong.setSelectedIndex(0);
                loadPhimByPhong((String) cbTenPhong.getSelectedItem());
            }
        } catch (Exception e) {
            // Có thể show toast lỗi hoặc log
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
        } catch (Exception ex) {
            // Có thể show toast lỗi hoặc log
        }
    }
}