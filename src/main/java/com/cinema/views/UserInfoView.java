package com.cinema.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.io.IOException;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.cinema.controllers.KhachHangController;
import com.cinema.models.ChiTietHoaDon;
import com.cinema.models.KhachHang;
import com.cinema.models.repositories.DatVeRepository;
import com.cinema.models.repositories.HoaDonRepository;
import com.cinema.models.repositories.VeRepository;
import com.cinema.services.KhachHangService;
import com.cinema.utils.DatabaseConnection;

public class UserInfoView extends JDialog {
    private final String username;
    private final KhachHangController khachHangController;
    private final HoaDonRepository hoaDonRepository;
    private final VeRepository veRepository;
    private final DatVeRepository datVeRepository;
    private JTable bookingTable;
    private final NumberFormat currencyFormat;

    public UserInfoView(JFrame parent, String username) throws IOException {
        super(parent, "Thông tin cá nhân", true);
        this.username = username;
        DatabaseConnection databaseConnection = new DatabaseConnection();
        this.khachHangController = new KhachHangController(new KhachHangService(databaseConnection));
        this.hoaDonRepository = new HoaDonRepository(databaseConnection);
        this.veRepository = new VeRepository(databaseConnection);
        this.datVeRepository = new DatVeRepository(databaseConnection);
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        setSize(1000, 600);
        setLayout(new BorderLayout());
        setLocationRelativeTo(parent);
        initializeComponents();
    }

    private void initializeComponents() {
        // Thông tin người dùng
        JPanel infoPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        infoPanel.setBackground(Color.WHITE);

        try {
            KhachHang khachHang = khachHangController.getKhachHangByUsername(username);
            infoPanel.add(new JLabel("Tên đăng nhập:"));
            infoPanel.add(new JLabel(username));
            infoPanel.add(new JLabel("Họ tên:"));
            infoPanel.add(new JLabel(khachHang.getHoTen()));
            infoPanel.add(new JLabel("Email:"));
            infoPanel.add(new JLabel(khachHang.getEmail()));
            infoPanel.add(new JLabel("Số điện thoại:"));
            infoPanel.add(new JLabel(khachHang.getSoDienThoai()));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lấy thông tin người dùng: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            infoPanel.add(new JLabel("Thông tin:"));
            infoPanel.add(new JLabel("Không tìm thấy thông tin khách hàng"));
        }

        add(infoPanel, BorderLayout.NORTH);

        // Lịch sử vé
        String[] columns = {"Mã vé", "Tên phim", "Suất chiếu", "Ghế", "Loại ghế", "Số tiền", "Trạng thái", "Hành động"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // Chỉ cho phép chỉnh sửa cột hành động
            }
        };
        bookingTable = new JTable(tableModel);
        bookingTable.getColumnModel().getColumn(7).setCellRenderer(new ButtonRenderer());
        bookingTable.getColumnModel().getColumn(7).setCellEditor(new ButtonEditor(new JCheckBox(), tableModel, this));
        
        // Đặt chiều rộng cột
        bookingTable.getColumnModel().getColumn(0).setPreferredWidth(60);  // Mã vé
        bookingTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Tên phim
        bookingTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Suất chiếu
        bookingTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Ghế
        bookingTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Loại ghế
        bookingTable.getColumnModel().getColumn(5).setPreferredWidth(120); // Số tiền
        bookingTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Trạng thái
        bookingTable.getColumnModel().getColumn(7).setPreferredWidth(100); // Hành động

        JScrollPane tableScrollPane = new JScrollPane(bookingTable);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        add(tableScrollPane, BorderLayout.CENTER);
    }

    private void loadBookingHistory(DefaultTableModel tableModel) {
        tableModel.setRowCount(0);
        try {
            List<ChiTietHoaDon> chiTietHoaDons = hoaDonRepository.findChiTietByUsername(username);
            for (ChiTietHoaDon chiTiet : chiTietHoaDons) {
                String trangThai = veRepository.findVeByMaVe(chiTiet.getMaVe()).getTrangThai().toString();
                String action = "BOOKED".equals(trangThai) ? "Hủy vé" : "-";
                Object[] row = {
                    chiTiet.getMaVe(),
                    chiTiet.getTenPhim(),
                    chiTiet.getNgayGioChieu() != null ? chiTiet.getNgayGioChieu().format(
                            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A",
                    chiTiet.getSoGhe(),
                    chiTiet.getLoaiGhe(),
                    currencyFormat.format(chiTiet.getGiaVe()),
                    getTrangThaiDisplay(trangThai),
                    action
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }

    private String getTrangThaiDisplay(String trangThai) {
        switch (trangThai) {
            case "BOOKED": return "Chờ thanh toán";
            case "PAID": return "Đã thanh toán";
            case "CANCELLED": return "Đã hủy";
            default: return trangThai;
        }
    }

    private void handleDatabaseError(SQLException e) {
        String errorMessage = e.getMessage();
        if (errorMessage.toLowerCase().contains("closed") || errorMessage.toLowerCase().contains("connection")) {
            int option = JOptionPane.showConfirmDialog(this,
                    "Lỗi kết nối cơ sở dữ liệu: Không thể tải lịch sử đặt vé. Vui lòng kiểm tra lại hệ thống.\nBạn có muốn thử lại không?",
                    "Lỗi Kết Nối",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE);
            if (option == JOptionPane.YES_OPTION) {
                loadBookingHistory((DefaultTableModel) bookingTable.getModel());
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi tải lịch sử đặt vé: " + errorMessage,
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
        e.printStackTrace();
    }

    private void cancelVe(int maVe) {
        try {
            String trangThai = veRepository.findVeByMaVe(maVe).getTrangThai().toString();
            if (!"BOOKED".equals(trangThai)) {
                JOptionPane.showMessageDialog(this,
                        "Chỉ có thể hủy vé chưa thanh toán (trạng thái BOOKED).",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc chắn muốn hủy vé này không?",
                    "Xác nhận hủy vé",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                datVeRepository.cancelVe(maVe);
                JOptionPane.showMessageDialog(this, "Hủy vé thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadBookingHistory((DefaultTableModel) bookingTable.getModel());
            }
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }

    // Button renderer cho bảng
    private static class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value.toString());
            setEnabled(value.toString().equals("Hủy vé"));
            return this;
        }
    }

    // Button editor for table
    private class ButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private String label;
        private boolean isPushed;
        private final DefaultTableModel tableModel;
        private final UserInfoView parent;
        private int currentRow;

        public ButtonEditor(JCheckBox checkBox, DefaultTableModel tableModel, UserInfoView parent) {
            super(checkBox);
            this.tableModel = tableModel;
            this.parent = parent;
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(_ -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = value.toString();
            button.setText(label);
            button.setEnabled(label.equals("Hủy vé"));
            isPushed = true;
            currentRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed && label.equals("Hủy vé")) {
                int maVe = (int) tableModel.getValueAt(currentRow, 0);
                parent.cancelVe(maVe);
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        @Override
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }
}