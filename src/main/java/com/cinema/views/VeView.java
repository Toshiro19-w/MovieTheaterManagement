package com.cinema.views;

import com.cinema.controllers.VeController;
import com.cinema.models.TrangThaiVe;
import com.cinema.models.Ve;
import com.cinema.services.VeService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class VeView extends JFrame {
    private Connection conn;
    private DefaultTableModel tableModel;
    private VeController veController;
    private JTextField searchField;
    private JTable table;

    public VeView() {
        veController = new VeController(new VeService(conn));
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Quản lý Vé");
        setSize(1200, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        String[] columnNames = {"Mã Vé", "Mã Suất Chiếu", "Mã KH", "Mã HĐ", "Số Ghế", "Giá Vé", "Trạng Thái", "Ngày Đặt"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchField = new JTextField(20);
        JButton btnLoad = createButton("Tải DS Vé", this::loadTickets);
        JButton btnSearch = createButton("Tìm Kiếm", this::searchTickets);
        JButton btnAdd = createButton("Thêm Vé", this::addTicket);
        JButton btnDelete = createButton("Xóa Vé", this::deleteTicket);

        controlPanel.add(new JLabel("Tìm kiếm:"));
        controlPanel.add(searchField);
        controlPanel.add(btnLoad);
        controlPanel.add(btnSearch);
        controlPanel.add(btnAdd);
        controlPanel.add(btnDelete);

        add(scrollPane, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
    }

    private JButton createButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.addActionListener(e -> action.run());
        return button;
    }

    private void loadTickets() {
        tableModel.setRowCount(0);
        List<Ve> veList = veController.findAll();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        veList.forEach(ve -> addVeToTable(ve, formatter));
    }

    private void addVeToTable(Ve ve, DateTimeFormatter formatter) {
        tableModel.addRow(new Object[]{
                ve.getMaVe(),
                ve.getMaSuatChieu(),
                ve.getMaPhong() != null ? ve.getMaPhong() : "Chưa đặt",
                ve.getMaHoaDon() != null ? ve.getMaHoaDon() : "Chưa thanh toán",
                ve.getSoGhe(),
                formatCurrency(ve.getGiaVe()),
                ve.getTrangThai().getValue(),
                ve.getNgayDat() != null ? ve.getNgayDat().format(formatter) : "Chưa đặt"
        });
    }

    private String formatCurrency(BigDecimal amount) {
        return String.format("%,.0f VND", amount);
    }

    private void searchTickets() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mã vé.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        tableModel.setRowCount(0);
        List<Ve> veList = Collections.singletonList(veController.findVeById(Integer.parseInt(keyword)));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        veList.forEach(ve -> addVeToTable(ve, formatter));
    }

    private void addTicket() {
        String soGhe = JOptionPane.showInputDialog(this, "Nhập số ghế:");
        String giaVeStr = JOptionPane.showInputDialog(this, "Nhập giá vé:");
        if (soGhe == null || giaVeStr == null || soGhe.trim().isEmpty() || giaVeStr.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            BigDecimal giaVe = new BigDecimal(giaVeStr);
            //Ve newVe = new Ve(soGhe, giaVe, TrangThaiVe.AVAILABLE, LocalDate.now());
            JOptionPane.showMessageDialog(this, "Thêm vé thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadTickets();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteTicket() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn vé cần xóa", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int maVe = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa vé mã " + maVe + "?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            boolean deleted = veController.deleteVe(maVe);
            if (deleted) {
                JOptionPane.showMessageDialog(this, "Xóa vé thành công", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadTickets();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa vé thất bại", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VeView().setVisible(true));
    }
}