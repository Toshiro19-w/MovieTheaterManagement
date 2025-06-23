package com.cinema.controllers;

import java.awt.Component;
import java.awt.Container;
import java.awt.Image;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.cinema.components.MultiSelectComboBox;
import com.cinema.models.NhanVien;
import com.cinema.models.Phim;
import com.cinema.models.dto.CustomPaginationPanel;
import com.cinema.models.dto.PaginationResult;
import com.cinema.models.repositories.PhimRepository;
import com.cinema.services.PhimService;
import com.cinema.utils.LogUtils;
import com.cinema.views.admin.PhimView;

public class PhimController {
    private PhimView view;
    private PhimService service;
    private Map<Integer, String> theLoaiMap;
    private NhanVien currentNhanVien;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public PhimController(PhimView view, NhanVien currentNhanVien) throws SQLException {
        this.view = view;
        this.service = new PhimService(view.getDatabaseConnection());
        this.currentNhanVien = currentNhanVien;
        
        try {
            loadComboBoxData();
            loadPhimPaginated(1, 10);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(view, "Lỗi khi tải dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
        
        setupTableSelectionListener();
    }

    /**
     * Lấy mã người dùng hiện tại từ nhân viên đăng nhập
     */
    private int getCurrentUserId() {
        if (currentNhanVien != null) {
            return currentNhanVien.getMaNguoiDung();
        }
        return -1; // Trả về -1 nếu không có người dùng
    }

    public void loadTheLoaiList() throws SQLException {
        PhimRepository phimRepository = new PhimRepository(view.getDatabaseConnection());
        List<String> theLoaiList = phimRepository.getAllTheLoai();
        
        // Lấy danh sách thể loại đã chọn trước đó để giữ lại trạng thái
        List<Object> selectedIds = view.getCbTenTheLoai().getSelectedIds();
        
        // Xóa tất cả các item hiện tại
        view.getCbTenTheLoai().removeAllItems();
        
        // Thêm lại các item mới
        for (int i = 0; i < theLoaiList.size(); i++) {
            String tenTheLoai = theLoaiList.get(i);
            // Sử dụng index+1 làm ID tạm thời
            int id = i + 1;
            try {
                // Hoặc lấy ID thực từ repository nếu cần
                id = phimRepository.getMaTheLoaiByTen(tenTheLoai);
            } catch (SQLException e) {
                // Nếu không tìm thấy, giữ nguyên ID tạm
            }
            
            // Kiểm tra xem item này có nằm trong danh sách đã chọn trước đó không
            boolean selected = selectedIds.contains(id);
            view.getCbTenTheLoai().addItem(id, tenTheLoai, selected);
        }
    }

    private void loadComboBoxData() throws SQLException {
        // Tải dữ liệu cho combobox thể loại
        theLoaiMap = service.getAllTheLoaiMap();
        MultiSelectComboBox cbTenTheLoai = view.getCbTenTheLoai();
        cbTenTheLoai.removeAllItems();
        
        for (Map.Entry<Integer, String> entry : theLoaiMap.entrySet()) {
            cbTenTheLoai.addItem(entry.getKey(), entry.getValue(), false);
        }
        
        // Tải dữ liệu cho combobox kiểu phim
        List<String> dinhDangList = service.getAllDinhDang();
        view.getCbKieuPhim().removeAllItems();
        for (String dinhDang : dinhDangList) {
            view.getCbKieuPhim().addItem(dinhDang);
        }
    }

    /**
     * Tải danh sách phim theo phân trang
     * 
     * @param page Trang hiện tại
     * @param pageSize Số lượng phim trên mỗi trang
     * @throws SQLException Nếu có lỗi truy vấn CSDL
     */
    public void loadPhimPaginated(int page, int pageSize) throws SQLException {
        try {
            // Lấy dữ liệu phân trang từ service
            PaginationResult<Phim> result = service.getAllPhimPaginated(page, pageSize);
            
            // Cập nhật dữ liệu bảng
            DefaultTableModel model = view.getTableModel();
            model.setRowCount(0);
            
            for (Phim phim : result.getData()) {
                ImageIcon posterIcon = null;
                if (phim.getDuongDanPoster() != null && !phim.getDuongDanPoster().isEmpty()) {
                    try {
                        // Đường dẫn cố định đến thư mục posters
                        String posterDir = "src/main/resources/images/posters/";
                        File posterFile = new File(posterDir + phim.getDuongDanPoster());
                        
                        if (posterFile.exists()) {
                            ImageIcon icon = new ImageIcon(posterFile.getAbsolutePath());
                            Image img = icon.getImage().getScaledInstance(70, 100, Image.SCALE_SMOOTH);
                            posterIcon = new ImageIcon(img);
                        } else {
                            // Thử tải từ resources
                            java.net.URL imageUrl = getClass().getClassLoader().getResource("images/posters/" + phim.getDuongDanPoster());
                            if (imageUrl != null) {
                                ImageIcon icon = new ImageIcon(imageUrl);
                                Image img = icon.getImage().getScaledInstance(70, 100, Image.SCALE_SMOOTH);
                                posterIcon = new ImageIcon(img);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Lỗi tải poster cho bảng: " + e.getMessage());
                    }
                }
                
                model.addRow(new Object[]{
                    posterIcon,
                    phim.getTenPhim(),
                    phim.getTenTheLoai(),
                    phim.getThoiLuong(),
                    phim.getNgayKhoiChieu() != null ? phim.getNgayKhoiChieu().format(DATE_FORMATTER) : "",
                    phim.getTrangThai()
                });
            }
        
            // Tìm pagination panel theo tên
            findPaginationPanelByName(view, "paginationPanel", result);
            
            // Đảm bảo TableRowSorter được cập nhật
            if (view.getTable().getRowSorter() instanceof TableRowSorter) {
                ((TableRowSorter<?>) view.getTable().getRowSorter()).sort();
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(view, 
                "Lỗi khi tải dữ liệu phim: " + e.getMessage(), 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
            throw e;
        }
    }
    
    // Tìm kiếm PaginationPanel theo tên
    private void findPaginationPanelByName(Component component, String name, PaginationResult<Phim> result) {
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
    
    private void setupTableSelectionListener() {
        view.getTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = view.getTable().getSelectedRow();
                if (selectedRow >= 0) {
                    try {
                        int maPhim = getSelectedMaPhim();
                        if (maPhim > 0) {
                            Phim phim = service.getPhimById(maPhim);
                            if (phim != null) {
                                populateForm(phim);
                            }
                        }
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(view, "Lỗi khi tải thông tin phim: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    }

    private int getSelectedMaPhim() throws SQLException {
        int selectedRow = view.getTable().getSelectedRow();
        if (selectedRow >= 0) {
            // Chuyển đổi từ view index sang model index khi sử dụng TableRowSorter
            int modelRow = view.getTable().convertRowIndexToModel(selectedRow);
            
            String tenPhim = view.getTableModel().getValueAt(modelRow, 1).toString();
            String theLoai = view.getTableModel().getValueAt(modelRow, 2).toString();
            int thoiLuong = (int) view.getTableModel().getValueAt(modelRow, 3);
            
            // Tìm phim theo các thông tin này
            List<Phim> phimList = service.getAllPhim();
            for (Phim phim : phimList) {
                if (phim.getTenPhim().equals(tenPhim) && 
                    phim.getThoiLuong() == thoiLuong) {
                    return phim.getMaPhim();
                }
            }
        }
        return -1;
    }

    private void populateForm(Phim phim) {
        view.getTxtMaPhim().setText(String.valueOf(phim.getMaPhim()));
        view.getTxtTenPhim().setText(phim.getTenPhim());
        view.getTxtThoiLuong().setText(String.valueOf(phim.getThoiLuong()));
        view.getTxtNgayKhoiChieu().setText(phim.getNgayKhoiChieu() != null ? phim.getNgayKhoiChieu().format(DATE_FORMATTER) : "");
        view.getTxtMoTa().setText(phim.getMoTa());
        view.getTxtDaoDien().setText(phim.getDaoDien());
        
        // Set combobox values - Cập nhật để hỗ trợ nhiều thể loại
        MultiSelectComboBox cbTenTheLoai = view.getCbTenTheLoai();
        List<Integer> selectedTheLoaiIds = phim.getMaTheLoaiList();
        cbTenTheLoai.setSelectedIds(selectedTheLoaiIds);
        
        view.getTxtTrangThai().setText(phim.getTrangThai());
        view.getCbKieuPhim().setSelectedItem(phim.getKieuPhim());
        view.getCbNuocSanXuat().setSelectedItem(phim.getNuocSanXuat());
        
        // Set poster
        if (phim.getDuongDanPoster() != null && !phim.getDuongDanPoster().isEmpty()) {
            try {
                // Đường dẫn cố định đến thư mục posters
                String posterDir = "src/main/resources/images/posters/";
                File posterFile = new File(posterDir + phim.getDuongDanPoster());
                
                // Kiểm tra xem file có tồn tại không
                if (posterFile.exists()) {
                    ImageIcon icon = new ImageIcon(posterFile.getAbsolutePath());
                    Image img = icon.getImage().getScaledInstance(70, 100, Image.SCALE_SMOOTH);
                    view.getPosterLabel().setIcon(new ImageIcon(img));
                    view.getPosterLabel().setText("");
                    view.setSelectedPosterPath(posterFile.getAbsolutePath());
                } else {
                    // Thử tải từ resources
                    java.net.URL imageUrl = getClass().getClassLoader().getResource("images/posters/" + phim.getDuongDanPoster());
                    if (imageUrl != null) {
                        ImageIcon icon = new ImageIcon(imageUrl);
                        Image img = icon.getImage().getScaledInstance(70, 100, Image.SCALE_SMOOTH);
                        view.getPosterLabel().setIcon(new ImageIcon(img));
                        view.getPosterLabel().setText("");
                        view.setSelectedPosterPath(phim.getDuongDanPoster());
                    } else {
                        System.err.println("Không tìm thấy ảnh: " + phim.getDuongDanPoster());
                        view.getPosterLabel().setIcon(null);
                        view.getPosterLabel().setText("Không tìm thấy ảnh");
                        view.clearSelectedPosterPath();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                view.getPosterLabel().setIcon(null);
                view.getPosterLabel().setText("Lỗi tải ảnh");
                view.clearSelectedPosterPath();
            }
        } else {
            view.getPosterLabel().setIcon(null);
            view.getPosterLabel().setText("Không có ảnh");
            view.clearSelectedPosterPath();
        }
    }    
    
    public void themPhim() throws SQLException {
        Phim phim = getPhimFromForm();
        
        // Xử lý poster nếu có
        if (view.getSelectedPosterPath() != null) {
            String savedPath = savePoster(view.getSelectedPosterPath());
            phim.setDuongDanPoster(savedPath);
        }
        
        // Thiết lập trạng thái mặc định cho phim mới
        phim.setTrangThai("upcoming");
        
        // Lưu phim vào database thông qua service
        int maPhim = service.addPhim(phim);
        
        // Ghi log hoạt động
        String moTa = String.format("Thêm phim mới: %s (%s, %d phút)", 
            phim.getTenPhim(), 
            phim.getKieuPhim(), 
            phim.getThoiLuong()
        );
        LogUtils.logThemPhim(maPhim, moTa, getCurrentUserId());

        JOptionPane.showMessageDialog(view, "Thêm phim thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        view.clearForm();
        loadPhimPaginated(1, 10);
    }

    public void suaPhim() throws SQLException {
        Phim phim = getPhimFromForm();
        
        // Xử lý poster nếu có thay đổi
        if (view.getSelectedPosterPath() != null) {
            String savedPath = savePoster(view.getSelectedPosterPath());
            phim.setDuongDanPoster(savedPath);
        }
        
        // Cập nhật phim trong database thông qua service
        service.updatePhim(phim);
        
        JOptionPane.showMessageDialog(view, "Cập nhật phim thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        loadPhimPaginated(1, 10);
    }    
    
    public void xoaPhim() throws SQLException {
        String maPhimText = view.getTxtMaPhim().getText();
        if (maPhimText.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Vui lòng chọn phim cần xóa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int maPhim = Integer.parseInt(maPhimText);
        
        // Lấy thông tin phim trước khi xóa để ghi log
        Phim phim = service.getPhimById(maPhim);
        if (phim != null) {
            service.deletePhim(maPhim);
            
            // Ghi log hoạt động
            String moTa = String.format("Xóa phim: %s (ID: %d)", 
                phim.getTenPhim(), 
                phim.getMaPhim()
            );
            LogUtils.logXoaPhim(maPhim, moTa, getCurrentUserId());
        }
        
        JOptionPane.showMessageDialog(view, "Xóa phim thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        view.clearForm();
        loadPhimPaginated(1, 10);
    }

    private Phim getPhimFromForm() throws SQLException {
        Phim phim = new Phim();
        
        // Set mã phim nếu đang sửa
        String maPhimText = view.getTxtMaPhim().getText();
        if (!maPhimText.isEmpty()) {
            phim.setMaPhim(Integer.parseInt(maPhimText));
        }
        
        phim.setTenPhim(view.getTxtTenPhim().getText());
        phim.setThoiLuong(Integer.parseInt(view.getTxtThoiLuong().getText()));
        
        // Parse ngày khởi chiếu
        String ngayKCText = view.getTxtNgayKhoiChieu().getText();
        if (!ngayKCText.isEmpty()) {
            LocalDate ngayKC = LocalDate.parse(ngayKCText, DATE_FORMATTER);
            phim.setNgayKhoiChieu(ngayKC);
        }
        
        phim.setNuocSanXuat(view.getCbNuocSanXuat().getText());
        phim.setMoTa(view.getTxtMoTa().getText());
        phim.setDaoDien(view.getTxtDaoDien().getText());
        
        // Set thể loại - Cập nhật để hỗ trợ nhiều thể loại
        MultiSelectComboBox cbTenTheLoai = view.getCbTenTheLoai();
        List<Object> selectedIds = cbTenTheLoai.getSelectedIds();
        List<Integer> maTheLoaiList = selectedIds.stream()
                .map(id -> (Integer) id)
                .collect(Collectors.toList());
        phim.setMaTheLoaiList(maTheLoaiList);
        
        // Tạo chuỗi tên thể loại để hiển thị
        List<String> selectedTexts = cbTenTheLoai.getSelectedTexts();
        String tenTheLoai = String.join(", ", selectedTexts);
        phim.setTenTheLoai(tenTheLoai);
        
        // Set trạng thái và kiểu phim
        phim.setTrangThai(view.getTxtTrangThai().getText());
        phim.setKieuPhim((String) view.getCbKieuPhim().getSelectedItem());
        
        return phim;
    }

    private String savePoster(String sourcePath) {
        try {
            // Tạo thư mục posters nếu chưa tồn tại
            String posterDir = "src/main/resources/images/posters/";
            File postersDir = new File(posterDir);
            if (!postersDir.exists()) {
                postersDir.mkdirs();
            }
            
            // Lấy tên phim từ form để đặt tên file
            String tenPhim = view.getTxtTenPhim().getText().trim();
            // Loại bỏ các ký tự không hợp lệ trong tên file
            tenPhim = tenPhim.replaceAll("[^a-zA-Z0-9]", "_");
            
            // Tạo tên file dựa trên tên phim
            String fileName = tenPhim + getFileExtension(sourcePath);
            Path targetPath = Paths.get(posterDir, fileName);
            
            // Copy file poster vào thư mục posters
            Files.copy(Paths.get(sourcePath), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            // Trả về tên file để lưu vào database
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi lưu poster: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private String getFileExtension(String path) {
        int lastDotIndex = path.lastIndexOf('.');
        if (lastDotIndex >= 0) {
            return path.substring(lastDotIndex);
        }
        return "";
    }
    
    /**
     * Hiển thị chi tiết phim khi chọn một dòng trong bảng
     * @param selectedRow Dòng được chọn trong bảng
     * @throws SQLException Nếu có lỗi truy vấn CSDL
     */
    public void hienThiChiTietPhim(int selectedRow) throws SQLException {
        if (selectedRow >= 0) {
            try {
                // Chuyển đổi từ view index sang model index khi sử dụng TableRowSorter
                int modelRow = view.getTable().convertRowIndexToModel(selectedRow);
                
                // Lấy thông tin từ model
                String tenPhim = view.getTableModel().getValueAt(modelRow, 1).toString();
                int thoiLuong = (int) view.getTableModel().getValueAt(modelRow, 3);
                
                // Tìm phim theo các thông tin này
                Phim phim = null;
                List<Phim> phimList = service.getAllPhim();
                for (Phim p : phimList) {
                    if (p.getTenPhim().equals(tenPhim) && p.getThoiLuong() == thoiLuong) {
                        phim = p;
                        break;
                    }
                }
                
                // Hiển thị thông tin phim
                if (phim != null) {
                    populateForm(phim);
                }
            } catch (Exception e) {
                System.err.println("Lỗi khi hiển thị chi tiết phim: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(view, 
                    "Lỗi khi hiển thị chi tiết phim: " + e.getMessage(), 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Phương thức tìm kiếm phim (giữ lại để tương thích với mã hiện tại)
     * Lưu ý: Chức năng tìm kiếm thực tế được xử lý bởi TableRowSorter trong PhimView
     * 
     * @param keyword Từ khóa tìm kiếm
     * @throws SQLException Nếu có lỗi truy vấn CSDL
     */
    public void timKiemPhim(String keyword) throws SQLException {
        // Không làm gì cả, việc tìm kiếm được xử lý bởi TableRowSorter trong PhimView
    }
}