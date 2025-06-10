package com.cinema.controllers;

import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.cinema.components.UnderlineTextField;
import com.cinema.enums.TrangThaiVe;
import com.cinema.models.KhachHang;
import com.cinema.models.PhongChieu;
import com.cinema.models.Ve;
import com.cinema.models.dto.CustomPaginationPanel;
import com.cinema.models.dto.PaginationResult;
import com.cinema.services.PhimService;
import com.cinema.services.PhongChieuService;
import com.cinema.services.SuatChieuService;
import com.cinema.services.VeService;
import com.cinema.utils.FormatUtils;
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
    private TableRowSorter<DefaultTableModel> tableSorter;
    
    // Cấu hình phân trang
    private static final int PAGE_SIZE = 10;

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
        setupPagination();
    }

    private void initView() {
        try {
            loadVePaginated(1, PAGE_SIZE);
        } catch (SQLException e) {
            handleException("Lỗi khi tải dữ liệu vé", e);
        }
    }

    private void addListeners() {
        // Thiết lập TableRowSorter cho bảng
        tableSorter = new TableRowSorter<>(view.getTableModel());
        view.getTable().setRowSorter(tableSorter);
        
        // Thêm listener cho tìm kiếm sử dụng TableRowSorter
        view.getSearchField().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { searchVe(); }
            @Override
            public void removeUpdate(DocumentEvent e) { searchVe(); }
            @Override
            public void changedUpdate(DocumentEvent e) { searchVe(); }
        });

        // Sử dụng một biến để theo dõi lần chọn cuối cùng để tránh xử lý nhiều lần
        view.getTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = view.getTable().getSelectedRow();
                if (selectedRow >= 0) {
                    // Sử dụng SwingUtilities.invokeLater để tránh vấn đề với event dispatch thread
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        try {
                            displayVeInfo(selectedRow);
                        } catch (Exception ex) {
                            handleException("Lỗi khi hiển thị thông tin vé", ex);
                        }
                    });
                } else {
                    clearForm();
                }
            }
        });        
        
        view.getBtnThem().addActionListener(_ -> themVe());
        view.getBtnSua().addActionListener(_ -> suaVe());
        view.getBtnXoa().addActionListener(_ -> xoaVe());
        view.getBtnClear().addActionListener(_ -> clearForm());
        view.getBtnRefresh().addActionListener(_ -> refreshData());
    }
    
    private void setupPagination() {
        // Thiết lập tên cho pagination panel để có thể tìm thấy nó
        view.getPaginationPanel().setName("paginationPanel");
        
        // Thiết lập trực tiếp listener cho pagination panel
        view.getPaginationPanel().setPageChangeListener(page -> {
            try {
                loadVePaginated(page, PAGE_SIZE);
            } catch (SQLException e) {
                handleException("Lỗi khi chuyển trang", e);
            }
        });
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
                
                // Cập nhật thời gian chiếu khi phim thay đổi
                String tenPhong = (String) view.getCbTenPhong().getSelectedItem();
                if (ValidationUtils.isValidString(tenPhong)) {
                    loadSuatChieuByPhongVaPhim(tenPhong, tenPhim);
                }
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

    /**
     * Tải danh sách vé theo phân trang
     * 
     * @param page Trang hiện tại
     * @param pageSize Số lượng vé trên mỗi trang
     * @throws SQLException Nếu có lỗi truy vấn CSDL
     */
    public void loadVePaginated(int page, int pageSize) throws SQLException {
        try {
            // Lấy dữ liệu phân trang từ service
            PaginationResult<Ve> result = service.getAllVePaginated(page, pageSize);
            
            // Cập nhật dữ liệu bảng
            DefaultTableModel model = view.getTableModel();
            model.setRowCount(0);
            
            // Sử dụng batch processing để thêm dữ liệu vào bảng
            for (Ve ve : result.getData()) {
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
            
            // Cập nhật trực tiếp pagination panel
            CustomPaginationPanel paginationPanel = view.getPaginationPanel();
            if (paginationPanel != null) {
                paginationPanel.updatePagination(result.getCurrentPage(), result.getTotalPages());
            }
            
            // Đảm bảo TableRowSorter được cập nhật
            if (view.getTable().getRowSorter() instanceof TableRowSorter) {
                ((TableRowSorter<?>) view.getTable().getRowSorter()).sort();
            }
            
        } catch (SQLException e) {
            handleException("Lỗi khi tải dữ liệu vé", e);
            throw e;
        }
    }
    
    private void displayVeInfo(int selectedRow) {
        if (selectedRow < 0) return;

        try {
            DefaultTableModel model = view.getTableModel();
            int modelRow = view.getTable().convertRowIndexToModel(selectedRow);
            
            // Đặt timeout cho các thao tác CSDL
            String maVe = model.getValueAt(modelRow, 0).toString();
            
            // Sử dụng SwingWorker để tải thông tin vé trong background
            new javax.swing.SwingWorker<Ve, Void>() {
                @Override
                protected Ve doInBackground() throws Exception {
                    try {
                        return service.findVeByMaVe(Integer.parseInt(maVe));
                    } catch (Exception e) {
                        System.err.println("Lỗi khi tải thông tin vé: " + e.getMessage());
                        return null;
                    }
                }
                
                @Override
                protected void done() {
                    try {
                        Ve ve = get();
                        if (ve != null) {
                            displayVeInfo(ve);
                        } else {
                            // Fallback nếu không lấy được từ service
                            displayVeInfoFromTable(selectedRow);
                        }
                    } catch (Exception e) {
                        System.err.println("Lỗi khi hiển thị thông tin vé: " + e.getMessage());
                        // Fallback nếu có lỗi
                        displayVeInfoFromTable(selectedRow);
                    }
                }
            }.execute();
            
        } catch (Exception e) {
            // Bắt tất cả các ngoại lệ để tránh treo ứng dụng
            System.err.println("Lỗi không xác định khi hiển thị thông tin vé: " + e.getMessage());
            e.printStackTrace(System.err);
            clearForm(); // Xóa form nếu có lỗi
        }
    }

    // Phương thức mới để hiển thị thông tin vé từ bảng (fallback)
    private void displayVeInfoFromTable(int selectedRow) {
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
            
            // Không tải thông tin khuyến mãi và khách hàng để tránh lỗi
            view.getCbKhuyenMai().setEnabled(true);
            view.getCbKhuyenMai().removeAllItems();
            view.getCbKhuyenMai().addItem(tenKhuyenMai);
            view.getCbKhuyenMai().setSelectedItem(tenKhuyenMai);
            
            // Xóa thông tin khách hàng
            clearCustomerFields();
        } catch (Exception e) {
            System.err.println("Lỗi khi hiển thị thông tin vé từ bảng: " + e.getMessage());
            clearForm();
        }
    }

    private void displayVeInfo(Ve ve) {
        if (ve == null) {
            System.out.println("displayVeInfo: ve là null");
            return;
        }
        
        System.out.println("\n=== Bắt đầu displayVeInfo ===");
        System.out.println("Thông tin vé:");
        System.out.println("- Mã vé: " + ve.getMaVe());
        System.out.println("- Mã hóa đơn: " + ve.getMaHoaDon());
        System.out.println("- Trạng thái: " + ve.getTrangThai());
        
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
            System.out.println("Vé có mã hóa đơn: " + ve.getMaHoaDon() + ", gọi loadKhachHangInfo");
            try {
                loadKhachHangInfo(ve.getMaVe());
            } catch (Exception e) {
                System.err.println("Lỗi khi tải thông tin khách hàng: " + e.getMessage());
                e.printStackTrace();
                clearCustomerFields();
            }
        } else {
            System.out.println("Vé không có mã hóa đơn, xóa thông tin khách hàng");
            clearCustomerFields();
        }
        System.out.println("=== Kết thúc displayVeInfo ===\n");
    }    private void loadKhachHangInfo(int maVe) {
        // Xóa thông tin khách hàng cũ trước khi tải thông tin mới
        clearCustomerFields();
        
        try {
            System.out.println("\n=== Bắt đầu loadKhachHangInfo ===");
            System.out.println("Đang tải thông tin khách hàng cho vé: " + maVe);
            
            // Kiểm tra vé
            Ve ve = service.findVeByMaVe(maVe);
            if (ve == null) {
                System.out.println("Không tìm thấy vé với mã: " + maVe);
                return;
            }
            System.out.println("Tìm thấy vé - MaHoaDon: " + ve.getMaHoaDon());
            
            // Chỉ tải thông tin khách hàng nếu vé có hóa đơn
            if (ve.getMaHoaDon() == 0) {
                System.out.println("Vé chưa có hóa đơn, bỏ qua tìm khách hàng");
                return;
            }
            
            // Tải thông tin khách hàng
            KhachHang khachHang = service.getKhachHangByMaVe(maVe);
            System.out.println("Kết quả tìm khách hàng: " + (khachHang != null ? "Tìm thấy" : "Không tìm thấy"));
            
            if (khachHang != null) {
                System.out.println("Đã tìm thấy khách hàng: " + khachHang.getHoTen());
                // Cập nhật UI
                view.getTxtTenKhachHang().setText(khachHang.getHoTen());
                view.getTxtSoDienThoai().setText(khachHang.getSoDienThoai());
                view.getTxtEmail().setText(khachHang.getEmail());
                view.getTxtDiemTichLuy().setText(String.valueOf(khachHang.getDiemTichLuy()));
                
                // Kiểm tra xem các trường đã được set đúng chưa
                System.out.println("Giá trị các trường sau khi set:");
                System.out.println("- Tên: " + view.getTxtTenKhachHang().getText());
                System.out.println("- SĐT: " + view.getTxtSoDienThoai().getText());
                System.out.println("- Email: " + view.getTxtEmail().getText());
                System.out.println("- Điểm: " + view.getTxtDiemTichLuy().getText());
                
                disableCustomerFields();
            } else {
                System.out.println("Không tìm thấy thông tin khách hàng cho vé: " + maVe);
            }
            System.out.println("=== Kết thúc loadKhachHangInfo ===\n");
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi tải thông tin khách hàng:");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Lỗi không xác định khi tải thông tin khách hàng:");
            e.printStackTrace();
        }
    }

    // Phương thức vô hiệu hóa các trường
    private void disableCustomerFields() {
        view.getTxtTenKhachHang().setEnabled(false);
        view.getTxtSoDienThoai().setEnabled(false);
        view.getTxtEmail().setEnabled(false);
        view.getTxtDiemTichLuy().setEnabled(false);
    }

    // Thêm phương thức mới để clear các trường
    private void clearCustomerFields() {
        view.getTxtTenKhachHang().setText("");
        view.getTxtSoDienThoai().setText("");
        view.getTxtEmail().setText("");
        view.getTxtDiemTichLuy().setText("");
        // Vẫn giữ trạng thái disable các trường
        disableCustomerFields();
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
                view.showMessage("Thêm vé thành công", true);
                // Giữ nguyên trang hiện tại khi thêm vé mới
                int currentPage = view.getPaginationPanel().getCurrentPage();
                loadVePaginated(currentPage, PAGE_SIZE);
                displayVeInfo(result); // Hiển thị vé vừa thêm với thông tin đầy đủ
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
                view.showMessage("Cập nhật vé thanh công", true);
                // Giữ nguyên trang hiện tại khi sửa vé
                int currentPage = view.getPaginationPanel().getCurrentPage();
                loadVePaginated(currentPage, PAGE_SIZE);
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
                view.showMessage("Xoá vé thành công", true);
                
                // Giữ nguyên trang hiện tại khi xóa vé
                int currentPage = view.getPaginationPanel().getCurrentPage();
                int totalPages = view.getPaginationPanel().getTotalPages();
                
                // Nếu xóa vé cuối cùng của trang cuối, quay lại trang trước đó
                if (currentPage == totalPages && view.getTableModel().getRowCount() == 1) {
                    currentPage = Math.max(1, currentPage - 1);
                }
                
                loadVePaginated(currentPage, PAGE_SIZE);
                clearForm();
            } catch (SQLException e) {
                handleException("Lỗi khi xóa vé", e);
            }
        }
    }

    private void searchVe() {
        String searchText = view.getSearchField().getText().trim();
        if (searchText.isEmpty()) {
            tableSorter.setRowFilter(null);
        } else {
            try {
                // Tìm kiếm không phân biệt chữ hoa/thường trong các cột
                tableSorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText, 0, 1, 2, 7, 9, 10));
            } catch (java.util.regex.PatternSyntaxException e) {
                // Xử lý trường hợp chuỗi tìm kiếm có ký tự đặc biệt của regex
                String escapedText = java.util.regex.Pattern.quote(searchText);
                tableSorter.setRowFilter(RowFilter.regexFilter("(?i)" + escapedText, 0, 1, 2, 7, 9, 10));
            }
        }
    }

    private void refreshData() {
        try {
            // Giữ nguyên trang hiện tại khi làm mới dữ liệu
            int currentPage = view.getPaginationPanel().getCurrentPage();
            loadVePaginated(currentPage, PAGE_SIZE);
            loadKhuyenMaiList();
            view.showMessage("Cập nhật dữ liệu thanh công", true);
        } catch (SQLException e) {
            handleException("Lỗi khi cập nhật dữ liệu", e);
        }
    }

    private void clearForm() {
        view.clearForm();
        view.getCbKhuyenMai().setEnabled(true);
        loadKhuyenMaiList();
    }

    private String formatCurrency(BigDecimal amount) {
        return FormatUtils.formatCurrency(amount);
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

    private void handleException(String message, Exception e) {
        System.err.println(message);
        System.err.println("Chi tiết lỗi:");
        e.printStackTrace(System.err);
        
        // Kiểm tra xem lỗi có phải là lỗi kết nối CSDL không
        if (e instanceof SQLException) {
            SQLException sqlEx = (SQLException) e;
            if (sqlEx.getErrorCode() == 0 || // Lỗi kết nối
                sqlEx.getMessage().contains("Connection") || 
                sqlEx.getMessage().contains("connect") ||
                sqlEx.getMessage().contains("timeout")) {
                showError("Lỗi kết nối đến cơ sở dữ liệu. Vui lòng kiểm tra kết nối và thử lại sau.");
                return;
            }
            
            // Xử lý lỗi dữ liệu không tìm thấy
            if (sqlEx.getMessage().contains("not found") || 
                sqlEx.getMessage().contains("không tìm thấy") ||
                sqlEx.getMessage().contains("không tồn tại")) {
                showError("Dữ liệu yêu cầu không tồn tại trong cơ sở dữ liệu.");
                return;
            }
        }
        
        // Hiển thị thông báo lỗi chung nếu không phải các trường hợp đặc biệt
        showError(message);
    }

    public void refreshView() {
        try {
            // Giữ nguyên trang hiện tại khi làm mới view
            int currentPage = view.getPaginationPanel().getCurrentPage();
            loadVePaginated(currentPage, PAGE_SIZE);
            loadKhuyenMaiList();
        } catch (SQLException e) {
            handleException("Không thể làm mới dữ liệu", e);
        }
    }

    private void loadSuatChieuByPhongVaPhim(String tenPhong, String tenPhim) {
        try {
            // Lưu lại item đang chọn
            String selectedItem = null;
            if (view.getCbNgayGioChieu().getSelectedItem() != null) {
                selectedItem = view.getCbNgayGioChieu().getSelectedItem().toString();
            }
            
            // Tạm thời tắt sự kiện để tránh gọi nhiều lần
            JComboBox<String> cbNgayGioChieu = view.getCbNgayGioChieu();
            ActionListener[] listeners = cbNgayGioChieu.getActionListeners();
            for (ActionListener listener : listeners) {
                cbNgayGioChieu.removeActionListener(listener);
            }
            
            // Tải danh sách suất chiếu
            List<String> suatChieuList = suatChieuService.getThoiGianChieuByPhongVaPhim(tenPhong, tenPhim);
            cbNgayGioChieu.removeAllItems();
            
            // Thêm các suất chiếu vào combobox
            for (String thoiGian : suatChieuList) {
                cbNgayGioChieu.addItem(thoiGian);
            }
            
            // Khôi phục các sự kiện
            for (ActionListener listener : listeners) {
                cbNgayGioChieu.addActionListener(listener);
            }
            
            // Khôi phục item đã chọn hoặc chọn mặc định
            boolean found = false;
            if (selectedItem != null) {
                for (int i = 0; i < cbNgayGioChieu.getItemCount(); i++) {
                    if (selectedItem.equals(cbNgayGioChieu.getItemAt(i))) {
                        cbNgayGioChieu.setSelectedIndex(i);
                        found = true;
                        break;
                    }
                }
            }
            
            if (!found && cbNgayGioChieu.getItemCount() > 0) {
                cbNgayGioChieu.setSelectedIndex(0);
            }
        } catch (Exception ex) {
            System.err.println("Lỗi khi tải suất chiếu: " + ex.getMessage());
            ex.printStackTrace(System.err);
        }
    }

    private void loadPhongChieuList() {
        try {
            // Lưu lại item đang chọn
            String selectedItem = null;
            if (view.getCbTenPhong().getSelectedItem() != null) {
                selectedItem = view.getCbTenPhong().getSelectedItem().toString();
            }
            
            // Tạm thời tắt sự kiện để tránh gọi nhiều lần
            JComboBox<String> cbTenPhong = view.getCbTenPhong();
            ActionListener[] listeners = cbTenPhong.getActionListeners();
            for (ActionListener listener : listeners) {
                cbTenPhong.removeActionListener(listener);
            }
            
            // Tải danh sách phòng chiếu
            List<PhongChieu> phongChieuList = new PhongChieuService(view.getDatabaseConnection()).getAllPhongChieu();
            cbTenPhong.removeAllItems();
            
            // Thêm các phòng chiếu vào combobox
            for (PhongChieu pc : phongChieuList) {
                cbTenPhong.addItem(pc.getTenPhong());
            }
            
            // Khôi phục các sự kiện
            for (ActionListener listener : listeners) {
                cbTenPhong.addActionListener(listener);
            }
            
            // Khôi phục item đã chọn hoặc chọn mặc định
            boolean found = false;
            if (selectedItem != null) {
                for (int i = 0; i < cbTenPhong.getItemCount(); i++) {
                    if (selectedItem.equals(cbTenPhong.getItemAt(i))) {
                        cbTenPhong.setSelectedIndex(i);
                        found = true;
                        break;
                    }
                }
            }
            
            if (!found && cbTenPhong.getItemCount() > 0) {
                cbTenPhong.setSelectedIndex(0);
            }
            
            // Tải phim nếu có phòng được chọn
            if (cbTenPhong.getSelectedItem() != null) {
                loadPhimByPhong((String) cbTenPhong.getSelectedItem());
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tải danh sách phòng chiếu: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    private void loadPhimByPhong(String tenPhong) {
        try {
            // Lưu lại item đang chọn
            String selectedItem = null;
            if (view.getCbTenPhim().getSelectedItem() != null) {
                selectedItem = view.getCbTenPhim().getSelectedItem().toString();
            }
            
            // Tạm thời tắt sự kiện để tránh gọi nhiều lần
            JComboBox<String> cbTenPhim = view.getCbTenPhim();
            ActionListener[] listeners = cbTenPhim.getActionListeners();
            for (ActionListener listener : listeners) {
                cbTenPhim.removeActionListener(listener);
            }
            
            // Tải danh sách phim
            List<com.cinema.models.Phim> phimList = phimService.getPhimByTenPhong(tenPhong);
            cbTenPhim.removeAllItems();
            
            // Thêm các phim vào combobox
            for (com.cinema.models.Phim phim : phimList) {
                cbTenPhim.addItem(phim.getTenPhim());
            }
            
            // Khôi phục các sự kiện
            for (ActionListener listener : listeners) {
                cbTenPhim.addActionListener(listener);
            }
            
            // Khôi phục item đã chọn hoặc chọn mặc định
            boolean found = false;
            if (selectedItem != null) {
                for (int i = 0; i < cbTenPhim.getItemCount(); i++) {
                    if (selectedItem.equals(cbTenPhim.getItemAt(i))) {
                        cbTenPhim.setSelectedIndex(i);
                        found = true;
                        break;
                    }
                }
            }
            
            if (!found && cbTenPhim.getItemCount() > 0) {
                cbTenPhim.setSelectedIndex(0);
            }
            
            // Tải suất chiếu nếu có phim được chọn
            if (cbTenPhim.getSelectedItem() != null) {
                loadSuatChieuByPhongVaPhim(tenPhong, (String) cbTenPhim.getSelectedItem());
            }
        } catch (Exception ex) {
            System.err.println("Lỗi khi tải danh sách phim: " + ex.getMessage());
            ex.printStackTrace(System.err);
        }
    }

    private void loadKhuyenMaiList() {
        try {
            // Lưu lại item đang chọn
            String selectedItem = null;
            if (view.getCbKhuyenMai().getSelectedItem() != null) {
                selectedItem = view.getCbKhuyenMai().getSelectedItem().toString();
            }
            
            // Tải danh sách khuyến mãi
            List<String> promotions = service.getValidPromotions();
            JComboBox<String> cbKhuyenMai = view.getCbKhuyenMai();
            
            // Tạm thời tắt sự kiện để tránh gọi nhiều lần
            ActionListener[] listeners = cbKhuyenMai.getActionListeners();
            for (ActionListener listener : listeners) {
                cbKhuyenMai.removeActionListener(listener);
            }
            
            cbKhuyenMai.removeAllItems();
            for (String promotion : promotions) {
                cbKhuyenMai.addItem(promotion);
            }
            
            // Khôi phục item đã chọn hoặc chọn mặc định
            if (selectedItem != null && promotions.contains(selectedItem)) {
                cbKhuyenMai.setSelectedItem(selectedItem);
            } else {
                cbKhuyenMai.setSelectedItem("Không có");
            }
            
            // Khôi phục các sự kiện
            for (ActionListener listener : listeners) {
                cbKhuyenMai.addActionListener(listener);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tải danh sách khuyến mãi: " + e.getMessage());
            e.printStackTrace(System.err);
            
            // Đảm bảo combobox luôn có ít nhất một item
            JComboBox<String> cbKhuyenMai = view.getCbKhuyenMai();
            if (cbKhuyenMai.getItemCount() == 0) {
                cbKhuyenMai.addItem("Không có");
                cbKhuyenMai.setSelectedIndex(0);
            }
        }
    }
}