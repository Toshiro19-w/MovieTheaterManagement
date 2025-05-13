package com.cinema.views.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
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

import com.cinema.controllers.KhachHangController;
import com.cinema.models.KhachHang;
import com.cinema.services.KhachHangService;
import com.cinema.utils.DatabaseConnection;

public class KhachHangView extends JPanel {
    private static final Color CINESTAR_BLUE = new Color(0, 51, 102);
    private static final Color CINESTAR_YELLOW = new Color(255, 204, 0);
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);

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
            // Khởi tạo controller với DatabaseConnection mới
            DatabaseConnection dbConnection = new DatabaseConnection();
            khachHangController = new KhachHangController(new KhachHangService(dbConnection));

            setLayout(new BorderLayout());
            setBackground(BACKGROUND_COLOR);
            setBorder(new EmptyBorder(10, 10, 10, 10));

            // Panel thông tin khách hàng
            JPanel infoPanel = createInfoPanel();

            // Bảng danh sách khách hàng
            JScrollPane scrollPane = createTablePanel();

            // Nút chức năng
            JPanel buttonPanel = createButtonPanel();

            add(infoPanel, BorderLayout.NORTH);
            add(scrollPane, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);

            // Khởi tạo dữ liệu
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
        JPanel infoPanel = new JPanel(new BorderLayout(10, 10));
        infoPanel.setBorder(BorderFactory.createTitledBorder("THÔNG TIN KHÁCH HÀNG"));
        infoPanel.setBackground(BACKGROUND_COLOR);

        JPanel fieldsPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        fieldsPanel.setBackground(BACKGROUND_COLOR);
        fieldsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        maKHLabel = new JLabel();
        hoTenField = new JTextField();
        emailField = new JTextField();
        sdtField = new JTextField();
        diemTichLuyField = new JTextField();
        searchField = new JTextField();

        addField(fieldsPanel, "Mã Khách Hàng:", maKHLabel);
        addField(fieldsPanel, "Họ Tên:", hoTenField);
        addField(fieldsPanel, "Email:", emailField);
        addField(fieldsPanel, "SĐT:", sdtField);
        addField(fieldsPanel, "Điểm Tích Lũy:", diemTichLuyField);
        addField(fieldsPanel, "Tìm Kiếm:", searchField);

        infoPanel.add(fieldsPanel, BorderLayout.CENTER);
        return infoPanel;
    }

    private JScrollPane createTablePanel() {
        String[] columns = {"Mã Khách Hàng", "Họ Tên", "Email", "SĐT", "Điểm Tích Lũy"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("DANH SÁCH KHÁCH HÀNG"));

        // Thêm sự kiện tìm kiếm
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

        return scrollPane;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addButton = new JButton("Thêm");
        editButton = new JButton("Sửa");
        deleteButton = new JButton("Xóa");
        clearButton = new JButton("Clear");

        // Style buttons
        for (JButton button : new JButton[]{addButton, editButton, deleteButton, clearButton}) {
            button.setBackground(CINESTAR_BLUE);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setBorder(new EmptyBorder(5, 10, 5, 10));
            button.setFont(new Font("Roboto", Font.BOLD, 12));
        }

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        return buttonPanel;
    }

    private void addField(JPanel panel, String labelText, JComponent component) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Roboto", Font.PLAIN, 14));
        panel.add(label);
        component.setFont(new Font("Roboto", Font.PLAIN, 14));
        panel.add(component);
    }

    private void loadData() {
        try {
            // Lấy danh sách khách hàng từ controller
            List<KhachHang> khachHangList = khachHangController.findAllKhachHang();
            
            // Xóa dữ liệu cũ trong bảng
            tableModel.setRowCount(0);
            
            // Thêm dữ liệu mới vào bảng
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
            
            // Cập nhật lại bảng
            tableModel.fireTableDataChanged();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi khi tải dữ liệu khách hàng: " + e.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // Getters for components
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
} 