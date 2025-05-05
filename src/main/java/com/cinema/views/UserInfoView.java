package com.cinema.views;

import com.cinema.controllers.KhachHangController;
import com.cinema.models.*;
import com.cinema.models.repositories.DatVeRepository;
import com.cinema.models.repositories.HoaDonRepository;
import com.cinema.models.repositories.VeRepository;
import com.cinema.services.KhachHangService;
import com.cinema.utils.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class UserInfoView extends JDialog {
    private final String username;
    private final KhachHangController khachHangController;
    private final HoaDonRepository hoaDonRepository;
    private final VeRepository veRepository;
    private final DatVeRepository datVeRepository;
    private JTable bookingTable;

    public UserInfoView(JFrame parent, String username) throws IOException {
        super(parent, "Thông tin cá nhân", true);
        this.username = username;
        DatabaseConnection databaseConnection = new DatabaseConnection();
        this.khachHangController = new KhachHangController(new KhachHangService(databaseConnection));
        this.hoaDonRepository = new HoaDonRepository(databaseConnection);
        this.veRepository = new VeRepository(databaseConnection);
        this.datVeRepository = new DatVeRepository(databaseConnection);

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
        String[] columns = {"Mã vé", "Tên phim", "Suất chiếu", "Ghế", "Số tiền", "Trạng thái", "Hành động"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0);
        bookingTable = new JTable(tableModel);
        loadBookingHistory(tableModel);

        JScrollPane tableScrollPane = new JScrollPane(bookingTable);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        add(tableScrollPane, BorderLayout.CENTER);
    }

    private void loadBookingHistory(DefaultTableModel tableModel) {
        tableModel.setRowCount(0);
        try {
            // Giả định HoaDonRepository có phương thức mới để lấy lịch sử đặt vé theo username
            List<ChiTietHoaDon> chiTietHoaDons = hoaDonRepository.findChiTietByUsername(username);
            for (ChiTietHoaDon chiTiet : chiTietHoaDons) {
                // Lấy trạng thái vé từ VeRepository
                String trangThai = veRepository.findVeByMaVe(chiTiet.getMaVe()).getTrangThai().toString();
                String action = "BOOKED".equals(trangThai) ? "Hủy vé" : "-";
                Object[] row = {
                        chiTiet.getMaVe(),
                        chiTiet.getTenPhim(),
                        chiTiet.getNgayGioChieu() != null ? chiTiet.getNgayGioChieu().format(
                                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A",
                        chiTiet.getSoGhe(),
                        chiTiet.getGiaVe(),
                        chiTiet.getMaHoaDon() > 0 ? "PAID" : "PENDING",
                        action
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            String errorMessage = e.getMessage();
            if (errorMessage.toLowerCase().contains("closed") || errorMessage.toLowerCase().contains("connection")) {
                int option = JOptionPane.showConfirmDialog(this,
                        "Lỗi kết nối cơ sở dữ liệu: Không thể tải lịch sử đặt vé. Vui lòng kiểm tra lại hệ thống.\nBạn có muốn thử lại không?",
                        "Lỗi Kết Nối",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.ERROR_MESSAGE);
                if (option == JOptionPane.YES_OPTION) {
                    loadBookingHistory(tableModel); // Tải lại nếu người dùng chọn Yes
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Lỗi khi tải lịch sử đặt vé: " + errorMessage,
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
            e.printStackTrace();
        }
    }

    private void cancelVe(int maVe) {
        try {
            // Kiểm tra trạng thái vé trước khi hủy
            String trangThai = veRepository.findVeByMaVe(maVe).getTrangThai().toString();
            if (!"BOOKED".equals(trangThai)) {
                JOptionPane.showMessageDialog(this,
                        "Chỉ có thể hủy vé chưa thanh toán (trạng thái BOOKED).",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            datVeRepository.cancelVe(maVe);
            JOptionPane.showMessageDialog(this, "Hủy vé thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadBookingHistory((DefaultTableModel) bookingTable.getModel());
        } catch (SQLException e) {
            String errorMessage = e.getMessage();
            if (errorMessage.toLowerCase().contains("closed") || errorMessage.toLowerCase().contains("connection")) {
                JOptionPane.showMessageDialog(this,
                        "Lỗi kết nối cơ sở dữ liệu: Không thể hủy vé. Vui lòng kiểm tra lại hệ thống.",
                        "Lỗi Kết Nối",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Lỗi khi hủy vé: " + errorMessage,
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
            e.printStackTrace();
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
            button.addActionListener(e -> fireEditingStopped());
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