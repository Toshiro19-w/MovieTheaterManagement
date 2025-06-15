package com.cinema.views.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.cinema.components.ModernUIApplier;
import com.cinema.controllers.KhachHangController;
import com.cinema.models.KhachHang;
import com.cinema.services.KhachHangService;
import com.cinema.utils.DatabaseConnection;
import com.cinema.views.common.ResizableView;

public class KhachHangView extends JPanel implements ResizableView {
    private JLabel maKHLabel;
    private JTextField hoTenField;
    private JTextField emailField;
    private JTextField sdtField;
    private JTextField diemTichLuyField;
    private JTextField searchField;
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton clearButton;
    private KhachHangController khachHangController;

    public KhachHangView() throws SQLException {
        try {
            DatabaseConnection dbConnection = new DatabaseConnection();
            khachHangController = new KhachHangController(new KhachHangService(dbConnection));

            setLayout(new BorderLayout(10, 10));
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(10, 10, 10, 10));

            JPanel infoPanel = createInfoPanel();
            JPanel tablePanel = createTablePanel();
            JPanel buttonPanel = createButtonPanel();

            add(infoPanel, BorderLayout.NORTH);
            add(tablePanel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);

            loadData();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi khi kết nối database: " + e.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
            throw new SQLException("Không thể kết nối database", e);
        }
    }

    private JPanel createInfoPanel() {
        JPanel panel = ModernUIApplier.createTitledPanel("THÔNG TIN KHÁCH HÀNG");
        panel.setLayout(new GridLayout(6, 2, 10, 10));

        maKHLabel = new JLabel();
        hoTenField = ModernUIApplier.createModernTextField("");
        emailField = ModernUIApplier.createModernTextField("");
        sdtField = ModernUIApplier.createModernTextField("");
        diemTichLuyField = ModernUIApplier.createModernTextField("");
        searchField = ModernUIApplier.createModernTextField("");

        panel.add(ModernUIApplier.createModernInfoLabel("Mã Khách Hàng:"));
        panel.add(maKHLabel);
        panel.add(ModernUIApplier.createModernInfoLabel("Họ Tên:"));
        panel.add(hoTenField);
        panel.add(ModernUIApplier.createModernInfoLabel("Email:"));
        panel.add(emailField);
        panel.add(ModernUIApplier.createModernInfoLabel("SĐT:"));
        panel.add(sdtField);
        panel.add(ModernUIApplier.createModernInfoLabel("Điểm Tích Lũy:"));
        panel.add(diemTichLuyField);
        panel.add(ModernUIApplier.createModernInfoLabel("Tìm Kiếm:"));
        panel.add(searchField);

        return panel;
    }

    private JPanel createTablePanel() {
        String[] columns = {"Mã Khách Hàng", "Họ Tên", "Email", "SĐT", "Điểm Tích Lũy"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        ModernUIApplier.applyModernTableStyle(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                String searchText = searchField.getText();
                if (searchText.trim().isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        JPanel panel = ModernUIApplier.createTitledPanel("DANH SÁCH KHÁCH HÀNG");
        panel.setLayout(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setOpaque(false);

        addButton = ModernUIApplier.createPrimaryButton("THÊM");
        editButton = ModernUIApplier.createPrimaryButton("SỬA");
        deleteButton = ModernUIApplier.createPrimaryButton("XÓA");
        clearButton = ModernUIApplier.createSecondaryButton("CLEAR");

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        return buttonPanel;
    }

    private void loadData() {
        try {
            List<KhachHang> khachHangList = khachHangController.findAllKhachHang();
            tableModel.setRowCount(0);
            for (KhachHang kh : khachHangList) {
                Object[] row = {
                    kh.getMaNguoiDung(),
                    kh.getHoTen(),
                    kh.getEmail(),
                    kh.getSoDienThoai(),
                    kh.getDiemTichLuy()
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi khi tải dữ liệu khách hàng: " + e.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // Getters
    public JLabel getMaKHLabel() { return maKHLabel; }
    public JTextField getHoTenField() { return hoTenField; }
    public JTextField getEmailField() { return emailField; }
    public JTextField getSdtField() { return sdtField; }
    public JTextField getDiemTichLuyField() { return diemTichLuyField; }
    public JTextField getSearchField() { return searchField; }
    public JTable getTable() { return table; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JButton getAddButton() { return addButton; }
    public JButton getEditButton() { return editButton; }
    public JButton getDeleteButton() { return deleteButton; }
    public JButton getClearButton() { return clearButton; }

    @Override
    public Dimension getPreferredViewSize() {
        return new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    @Override
    public Dimension getMinimumViewSize() {
        return new Dimension(MIN_WIDTH, MIN_HEIGHT);
    }

    @Override
    public boolean needsScrolling() {
        // Cần scroll vì có bảng danh sách khách hàng
        return true;
    }
}