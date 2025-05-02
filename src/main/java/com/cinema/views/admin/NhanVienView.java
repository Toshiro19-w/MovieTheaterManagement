package com.cinema.views.admin;

import com.cinema.controllers.NhanVienController;
import com.cinema.utils.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.sql.SQLException;

/**
 * NhanVienView is a JPanel that provides a GUI for managing employee information
 * and creating accounts for employees.
 */
public class NhanVienView extends JPanel {
    private DatabaseConnection databaseConnection;
    private JTextField searchField, txtTenDangNhap, txtHoTen, txtSDT, txtEmail, txtLuong;
    private JComboBox<String> vaiTroCombo, cmbLoaiTaiKhoan;
    private JLabel txtMaND;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnThem, btnSua, btnXoa, btnClear, btnTaoTaiKhoan;
    private JPasswordField txtMatKhau;
    private Integer selectedMaNV;
    private TableRowSorter<DefaultTableModel> sorter;

    public NhanVienView() throws SQLException {
        initializeDatabase();
        initializeUI();
        new NhanVienController(this);
    }

    /**
     * Initializes the database connection
     */
    private void initializeDatabase() {
        try {
            databaseConnection = new DatabaseConnection();
        } catch (IOException e) {
            showError("Không thể kết nối cơ sở dữ liệu: " + e.getMessage());
        }
    }

    /**
     * Initializes the user interface components
     */
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Initialize panels
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        JPanel accountPanel = createAccountPanel();
        JPanel infoPanel = createInfoPanel();
        JPanel tablePanel = createTablePanel();
        JPanel buttonPanel = createButtonPanel();

        // Combine info and account panels
        topPanel.add(accountPanel, BorderLayout.NORTH);
        topPanel.add(infoPanel, BorderLayout.CENTER);

        // Add components to main panel
        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Creates the account creation panel
     */
    private JPanel createAccountPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("TẠO TÀI KHOẢN NHÂN VIÊN"));

        panel.add(new JLabel("Tên Đăng Nhập:"));
        txtTenDangNhap = new JTextField();
        panel.add(txtTenDangNhap);

        panel.add(new JLabel("Mật Khẩu:"));
        txtMatKhau = new JPasswordField();
        panel.add(txtMatKhau);

        panel.add(new JLabel("Loại Tài Khoản:"));
        cmbLoaiTaiKhoan = new JComboBox<>(new String[]{"Admin", "QuanLyPhim", "ThuNgan", "BanVe"});
        panel.add(cmbLoaiTaiKhoan);

        panel.add(new JLabel(""));
        btnTaoTaiKhoan = new JButton("TẠO TÀI KHOẢN");
        panel.add(btnTaoTaiKhoan);

        return panel;
    }

    /**
     * Creates the employee information panel
     */
    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel(new BorderLayout(10, 10));
        infoPanel.setBorder(BorderFactory.createTitledBorder("THÔNG TIN NHÂN VIÊN"));

        JPanel fieldsPanel = new JPanel(new GridLayout(7, 2, 10, 10));
        initializeFields(fieldsPanel);

        infoPanel.add(fieldsPanel, BorderLayout.CENTER);
        return infoPanel;
    }

    /**
     * Initializes input fields for employee information
     */
    private void initializeFields(JPanel fieldsPanel) {
        txtMaND = new JLabel();
        txtHoTen = new JTextField();
        txtSDT = new JTextField();
        txtEmail = new JTextField();
        txtLuong = new JTextField();
        vaiTroCombo = new JComboBox<>(new String[]{"Admin", "QuanLyPhim", "ThuNgan", "BanVe"});
        searchField = new JTextField();

        addField(fieldsPanel, "Mã Nhân Viên:", txtMaND);
        addField(fieldsPanel, "Họ Tên:", txtHoTen);
        addField(fieldsPanel, "Số Điện Thoại:", txtSDT);
        addField(fieldsPanel, "Email:", txtEmail);
        addField(fieldsPanel, "Lương:", txtLuong);
        addField(fieldsPanel, "Vai Trò:", vaiTroCombo);
        addField(fieldsPanel, "Tìm Kiếm:", searchField);

        // Add search functionality
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String searchText = searchField.getText();
                if (searchText.trim().isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
                }
            }
        });
    }

    /**
     * Adds a label and component to the fields panel
     */
    private void addField(JPanel panel, String labelText, JComponent component) {
        panel.add(new JLabel(labelText));
        panel.add(component);
    }

    /**
     * Creates the table panel for displaying employees
     */
    private JPanel createTablePanel() {
        String[] columns = {"Mã Nhân Viên", "Họ Tên", "SĐT", "Email", "Lương", "Vai Trò"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // Handle table selection
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    selectedMaNV = (Integer) tableModel.getValueAt(selectedRow, 0);
                } else {
                    selectedMaNV = null;
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("DANH SÁCH NHÂN VIÊN"));
        return new JPanel(new BorderLayout()) {{ add(scrollPane, BorderLayout.CENTER); }};
    }

    /**
     * Creates the button panel for CRUD operations
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnThem = new JButton("THÊM");
        btnSua = new JButton("SỬA");
        btnXoa = new JButton("XÓA");
        btnClear = new JButton("CLEAR");

        buttonPanel.add(btnThem);
        buttonPanel.add(btnSua);
        buttonPanel.add(btnXoa);
        buttonPanel.add(btnClear);
        return buttonPanel;
    }

    /**
     * Clears the employee and account forms
     */
    public void clearForms() {
        txtMaND.setText("");
        txtHoTen.setText("");
        txtSDT.setText("");
        txtEmail.setText("");
        txtLuong.setText("");
        vaiTroCombo.setSelectedIndex(0);
        txtTenDangNhap.setText("");
        txtMatKhau.setText("");
        cmbLoaiTaiKhoan.setSelectedIndex(0);
        searchField.setText("");
        selectedMaNV = null;
        table.clearSelection();
    }

    /**
     * Shows an error message dialog
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    // Getters
    public DatabaseConnection getDatabaseConnection() { return databaseConnection; }
    public JLabel getTxtMaND() { return txtMaND; }
    public JTextField getTxtHoTen() { return txtHoTen; }
    public JTextField getTxtSDT() { return txtSDT; }
    public JTextField getTxtEmail() { return txtEmail; }
    public JTextField getTxtLuong() { return txtLuong; }
    public JComboBox<String> getVaiTroCombo() { return vaiTroCombo; }
    public JTable getTable() { return table; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JButton getBtnThem() { return btnThem; }
    public JButton getBtnSua() { return btnSua; }
    public JButton getBtnXoa() { return btnXoa; }
    public JButton getBtnClear() { return btnClear; }
    public JTextField getTxtTenDangNhap() { return txtTenDangNhap; }
    public JPasswordField getTxtMatKhau() { return txtMatKhau; }
    public JComboBox<String> getCmbLoaiTaiKhoan() { return cmbLoaiTaiKhoan; }
    public JButton getBtnTaoTaiKhoan() { return btnTaoTaiKhoan; }
    public Integer getSelectedMaNV() { return selectedMaNV; }
    public JTextField getSearchField() { return searchField; }
    public String getSearchText() { return searchField.getText(); }
}