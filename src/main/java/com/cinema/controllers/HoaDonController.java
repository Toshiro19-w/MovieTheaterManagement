package com.cinema.controllers;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.cinema.models.ChiTietHoaDon;
import com.cinema.models.HoaDon;
import com.cinema.models.NguoiDung;
import com.cinema.services.HoaDonService;
import com.cinema.utils.DatabaseConnection;
import com.cinema.views.admin.HoaDonView;

public class HoaDonController {
    private final HoaDonView view;
    private final HoaDonService service;
    private final DateTimeFormatter ngayGioChieuFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private TableRowSorter<DefaultTableModel> sorter;
    private final String username;

    public HoaDonController(HoaDonView view, String username) throws IOException {
        this.view = view;
        this.username = username;
        this.service = new HoaDonService(new DatabaseConnection());
        initView();
        addListeners();
    }

    private void initView() {
        try {
            loadHoaDonList(service.getAllHoaDon());
            sorter = new TableRowSorter<>(view.getModelHoaDon());
            view.getTableHoaDon().setRowSorter(sorter);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi tải danh sách hóa đơn!");
        }
    }

    private void addListeners() {
        view.getTableHoaDon().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = view.getTableHoaDon().getSelectedRow();
                if (selectedRow >= 0) {
                    displayHoaDonInfo(selectedRow);
                    int maHoaDon = (int) view.getModelHoaDon().getValueAt(selectedRow, 0);
                    displayChiTietVe(maHoaDon);
                    updateTotalAmount();
                }
            }
        });

        view.getSearchField().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                timKiemHoaDon();
            }
        });

        view.getBtnThem().addActionListener(_ -> themHoaDon());
        view.getBtnSua().addActionListener(_ -> suaHoaDon());
        view.getBtnXoa().addActionListener(_ -> xoaHoaDon());
        view.getBtnInHoaDon().addActionListener(_ -> inHoaDon());
        view.getBtnLamMoi().addActionListener(_ -> lamMoi());
    }

    private void themHoaDon() {
        try {
            // Lấy thông tin nhân viên đang đăng nhập
            NguoiDung currentUser = service.getNguoiDungByUsername(username);
            if (currentUser == null) {
                JOptionPane.showMessageDialog(view, "Không tìm thấy thông tin người dùng!");
                return;
            }
            
            // Tạo hóa đơn mới
            HoaDon hoaDon = new HoaDon();
            hoaDon.setMaNhanVien(currentUser.getMaNguoiDung());
            hoaDon.setNgayLap(LocalDateTime.now());
            
            // Thêm hóa đơn vào database
            if (service.insertHoaDon(hoaDon)) {
                JOptionPane.showMessageDialog(view, "Thêm hóa đơn thành công!");
                lamMoi();
            } else {
                JOptionPane.showMessageDialog(view, "Không thể thêm hóa đơn!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi thêm hóa đơn: " + e.getMessage());
        }
    }

    private void suaHoaDon() {
        int selectedRow = view.getTableHoaDon().getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(view, "Vui lòng chọn hóa đơn cần sửa!");
            return;
        }

        try {
            int maHoaDon = (int) view.getModelHoaDon().getValueAt(selectedRow, 0);
            HoaDon hoaDon = service.getHoaDonById(maHoaDon);

            if (hoaDon != null) {
                JTextField txtMaKhachHang = new JTextField(
                    hoaDon.getMaKhachHang() != null ? String.valueOf(hoaDon.getMaKhachHang()) : ""
                );
                JPanel panel = new JPanel(new GridLayout(2, 2));
                panel.add(new JLabel("Mã Khách Hàng (bỏ trống nếu vãng lai):"));
                panel.add(txtMaKhachHang);

                int result = JOptionPane.showConfirmDialog(view, panel, 
                    "Sửa Hóa Đơn", JOptionPane.OK_CANCEL_OPTION);

                if (result == JOptionPane.OK_OPTION) {
                    try {
                        String maKhachHangStr = txtMaKhachHang.getText().trim();
                        if (maKhachHangStr.isEmpty() || maKhachHangStr.equalsIgnoreCase("null")) {
                            hoaDon.setMaKhachHang(null); // khách vãng lai
                        } else {
                            int maKhachHang = Integer.parseInt(maKhachHangStr);
                            hoaDon.setMaKhachHang(maKhachHang);
                        }

                        if (service.updateHoaDon(hoaDon)) {
                            JOptionPane.showMessageDialog(view, "Cập nhật hóa đơn thành công!");
                            lamMoi();
                        } else {
                            JOptionPane.showMessageDialog(view, "Không thể cập nhật hóa đơn!");
                        }
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(view, "Mã khách hàng không hợp lệ!");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi sửa hóa đơn: " + e.getMessage());
        }
    }

    private void xoaHoaDon() {
        int selectedRow = view.getTableHoaDon().getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(view, "Vui lòng chọn hóa đơn cần xóa!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(view,
                "Bạn có chắc chắn muốn xóa hóa đơn này không?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int maHoaDon = (int) view.getModelHoaDon().getValueAt(selectedRow, 0);
                if (service.deleteHoaDon(maHoaDon)) {
                    JOptionPane.showMessageDialog(view, "Xóa hóa đơn thành công!");
                    lamMoi();
                } else {
                    JOptionPane.showMessageDialog(view, "Không thể xóa hóa đơn này!");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(view, "Lỗi khi xóa hóa đơn: " + e.getMessage());
            }
        }
    }

    private void inHoaDon() {
        int selectedRow = view.getTableHoaDon().getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(view, "Vui lòng chọn hóa đơn cần in!");
            return;
        }

        try {
            int maHoaDon = (int) view.getModelHoaDon().getValueAt(selectedRow, 0);
            HoaDon hoaDon = service.getHoaDonById(maHoaDon);
            List<ChiTietHoaDon> chiTietList = service.getChiTietHoaDon(maHoaDon);
            
            if (hoaDon != null && !chiTietList.isEmpty()) {
                // Tạo nội dung hóa đơn
                StringBuilder content = new StringBuilder();
                content.append("HÓA ĐƠN BÁN VÉ\n");
                content.append("Mã hóa đơn: ").append(hoaDon.getMaHoaDon()).append("\n");
                content.append("Ngày lập: ").append(hoaDon.getNgayLap().format(ngayGioChieuFormatter)).append("\n");
                content.append("Nhân viên: ").append(hoaDon.getTenNhanVien()).append("\n");
                content.append("Khách hàng: ").append(hoaDon.getTenKhachHang()).append("\n\n");
                
                content.append("CHI TIẾT VÉ:\n");
                content.append(String.format("%-10s %-30s %-10s %-15s %-20s %-15s\n",
                    "Mã Vé", "Tên Phim", "Số Ghế", "Loại Ghế", "Ngày Chiếu", "Giá Vé"));
                content.append("-".repeat(100)).append("\n");
                
                BigDecimal tongTien = BigDecimal.ZERO;
                for (ChiTietHoaDon ct : chiTietList) {
                    content.append(String.format("%-10s %-30s %-10s %-15s %-20s %-15s\n",
                        ct.getMaVe(),
                        ct.getTenPhim(),
                        ct.getSoGhe(),
                        ct.getLoaiGhe(),
                        ct.getNgayGioChieu().format(ngayGioChieuFormatter),
                        formatCurrency(ct.getGiaVe())));
                    tongTien = tongTien.add(ct.getGiaVe());
                }
                
                content.append("-".repeat(100)).append("\n");
                content.append(String.format("%-85s %-15s", "Tổng tiền:", formatCurrency(tongTien)));
                
                // Hiển thị hóa đơn
                JTextArea textArea = new JTextArea(content.toString());
                textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
                textArea.setEditable(false);
                
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(800, 600));
                
                JOptionPane.showMessageDialog(view, scrollPane, "Hóa Đơn", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi in hóa đơn: " + e.getMessage());
        }
    }

    private void lamMoi() {
        try {
            loadHoaDonList(service.getAllHoaDon());
            view.getSearchField().setText("");
            view.getTxtMaHoaDon().setText("");
            view.getTxtNgayLap().setText("");
            view.getTxtTongTien().setText("");
            view.getModelChiTietHoaDon().setRowCount(0);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi làm mới dữ liệu!");
        }
    }

    private String formatCurrency(BigDecimal amount) {
        return String.format("%,.0f VND", amount);
    }

    public void timKiemHoaDon() {
        String tuKhoa = view.getSearchText().toLowerCase();

        if (tuKhoa.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + tuKhoa, 1)); // Cột 1: Tên NV
        }
    }

    private void loadHoaDonList(List<HoaDon> hoaDons) {
        DefaultTableModel model = view.getModelHoaDon();
        model.setRowCount(0);
        for (HoaDon hd : hoaDons) {
            model.addRow(new Object[]{
                    hd.getMaHoaDon(),
                    hd.getTenNhanVien(),
                    hd.getTenKhachHang(),
                    hd.getNgayLap().format(ngayGioChieuFormatter),
                    formatCurrency(hd.getTongTien())
            });
        }
    }

    private void displayHoaDonInfo(int row) {
        DefaultTableModel model = view.getModelHoaDon();
        view.getTxtMaHoaDon().setText(model.getValueAt(row, 0).toString());
        view.getTxtNgayLap().setText((String) model.getValueAt(row, 3));
        view.getTxtTongTien().setText(model.getValueAt(row, 4).toString());
        try {
            int maHoaDon = (int) model.getValueAt(row, 0);
            loadChiTietHoaDon(maHoaDon);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi tải chi tiết hóa đơn!");
        }
    }

    private void loadChiTietHoaDon(int maHoaDon) throws SQLException {
        List<ChiTietHoaDon> chiTietList = service.getChiTietHoaDon(maHoaDon);
        DefaultTableModel model = view.getModelChiTietHoaDon();
        model.setRowCount(0);
        for (ChiTietHoaDon ct : chiTietList) {
            model.addRow(new Object[]{
                    ct.getMaVe(),
                    ct.getTenPhim(),
                    ct.getSoGhe(),
                    ct.getLoaiGhe(),
                    ct.getNgayGioChieu() != null ? ct.getNgayGioChieu().format(ngayGioChieuFormatter) : "",
                    formatCurrency(ct.getGiaVe())
            });
        }
    }

    private void updateTotalAmount() {
        try {
            int selectedRow = view.getTableHoaDon().getSelectedRow();
            if (selectedRow >= 0) {
                int maHoaDon = (int) view.getModelHoaDon().getValueAt(selectedRow, 0);
                BigDecimal tongTien = service.calculateTotalAmount(maHoaDon);
                view.getTxtTongTien().setText(formatCurrency(tongTien));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi tính tổng tiền: " + e.getMessage());
        }
    }

    private void displayChiTietVe(int maHoaDon) {
        try {
            List<Object[]> chiTietList = service.getChiTietVeWithGia(maHoaDon);
            DefaultTableModel model = view.getModelChiTietHoaDon();
            model.setRowCount(0);

            for (Object[] chiTiet : chiTietList) {
                String khuyenMai = chiTiet[4] != null ? 
                    String.format("%s (%s: %s)", 
                        chiTiet[4], 
                        chiTiet[5], 
                        chiTiet[6]) : "Không có";
                
                model.addRow(new Object[]{
                    chiTiet[0], // maVe
                    chiTiet[1], // soGhe
                    chiTiet[2], // loaiGhe
                    chiTiet[3], // giaGoc
                    khuyenMai,
                    chiTiet[7]  // giaSauGiam
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi hiển thị chi tiết vé: " + e.getMessage());
        }
    }

    public String getUsername() {
        return username;
    }

    public void hienThiDanhSachKhachHang(JTable table) {
        try {
            List<Object[]> danhSachKhachHang = service.getDanhSachKhachHang();
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.setRowCount(0);
            
            for (Object[] khachHang : danhSachKhachHang) {
                model.addRow(khachHang);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Lỗi khi lấy danh sách khách hàng: " + e.getMessage());
        }
    }

    public void hienThiVeCoTheThem(JTable table) {
        try {
            List<Object[]> danhSachVe = service.getVeCoTheThem();
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.setRowCount(0);
            
            for (Object[] ve : danhSachVe) {
                model.addRow(ve);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Lỗi khi lấy danh sách vé: " + e.getMessage());
        }
    }

    public boolean themVeVaoHoaDon(int maHoaDon, int maVe) {
        try {
            if (service.themVeVaoHoaDon(maHoaDon, maVe)) {
                JOptionPane.showMessageDialog(null, "Thêm vé vào hóa đơn thành công!");
                return true;
            } else {
                JOptionPane.showMessageDialog(null, "Thêm vé vào hóa đơn thất bại!");
                return false;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Lỗi khi thêm vé vào hóa đơn: " + e.getMessage());
            return false;
        }
    }

    public boolean capNhatKhachHang(int maHoaDon, int maKhachHang) {
        try {
            if (service.capNhatKhachHang(maHoaDon, maKhachHang)) {
                JOptionPane.showMessageDialog(null, "Cập nhật khách hàng thành công!");
                return true;
            } else {
                JOptionPane.showMessageDialog(null, "Cập nhật khách hàng thất bại!");
                return false;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Lỗi khi cập nhật khách hàng: " + e.getMessage());
            return false;
        }
    }
}