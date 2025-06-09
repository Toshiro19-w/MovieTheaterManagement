package com.cinema.views.admin;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.cinema.components.ModernUIApplier;
import com.cinema.components.UIConstants;
import com.cinema.controllers.NhanVienController;
import com.cinema.utils.DatabaseConnection;

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
     */    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(UIConstants.CONTENT_BACKGROUND_COLOR);

        // Initialize panels
        JPanel topPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        topPanel.setOpaque(false);
        JPanel accountPanel = createAccountPanel();
        JPanel infoPanel = createInfoPanel();
        JPanel tablePanel = createTablePanel();
        JPanel buttonPanel = createButtonPanel();

        // Add panels side by side
        topPanel.add(accountPanel);
        topPanel.add(infoPanel);

        // Add components to main panel
        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Creates the account creation panel
     */    private JPanel createAccountPanel() {
        JPanel panel = ModernUIApplier.createTitledPanel("TẠO TÀI KHOẢN NHÂN VIÊN");
        panel.setLayout(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
            panel.getBorder(),
            new EmptyBorder(20, 10, 20, 10)));

        txtTenDangNhap = ModernUIApplier.createModernTextField("");
        txtMatKhau = new JPasswordField();
        txtMatKhau.setBorder(txtTenDangNhap.getBorder());
        txtMatKhau.setFont(UIConstants.BODY_FONT);

        cmbLoaiTaiKhoan = new JComboBox<>(new String[]{"Admin", "QuanLyPhim", "ThuNgan", "BanVe"});
        ModernUIApplier.applyModernComboBoxStyle(cmbLoaiTaiKhoan);

        panel.add(ModernUIApplier.createModernInfoLabel("Tên Đăng Nhập:"));
        panel.add(txtTenDangNhap);
        panel.add(ModernUIApplier.createModernInfoLabel("Mật Khẩu:"));
        panel.add(txtMatKhau);
        panel.add(ModernUIApplier.createModernInfoLabel("Loại Tài Khoản:"));
        panel.add(cmbLoaiTaiKhoan);
        panel.add(new JLabel(""));
        btnTaoTaiKhoan = ModernUIApplier.createPrimaryButton("TẠO TÀI KHOẢN");
        panel.add(btnTaoTaiKhoan);

        return panel;
    }

    /**
     * Creates the employee information panel
     */    private JPanel createInfoPanel() {
        JPanel panel = ModernUIApplier.createTitledPanel("THÔNG TIN NHÂN VIÊN");
        panel.setLayout(new GridLayout(7, 2, 10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
            panel.getBorder(),
            new EmptyBorder(20, 10, 20, 10)));

        txtMaND = ModernUIApplier.createModernInfoLabel("");
        txtHoTen = ModernUIApplier.createModernTextField("");
        txtSDT = ModernUIApplier.createModernTextField("");
        txtEmail = ModernUIApplier.createModernTextField("");
        txtLuong = ModernUIApplier.createModernTextField("");
        vaiTroCombo = new JComboBox<>(new String[]{"Admin", "QuanLyPhim", "ThuNgan", "BanVe"});
        ModernUIApplier.applyModernComboBoxStyle(vaiTroCombo);
        searchField = ModernUIApplier.createModernTextField("");

        panel.add(ModernUIApplier.createModernInfoLabel("Mã Nhân Viên:"));
        panel.add(txtMaND);
        panel.add(ModernUIApplier.createModernInfoLabel("Họ Tên:"));
        panel.add(txtHoTen);
        panel.add(ModernUIApplier.createModernInfoLabel("Số Điện Thoại:"));
        panel.add(txtSDT);
        panel.add(ModernUIApplier.createModernInfoLabel("Email:"));
        panel.add(txtEmail);
        panel.add(ModernUIApplier.createModernInfoLabel("Lương:"));
        panel.add(txtLuong);
        panel.add(ModernUIApplier.createModernInfoLabel("Vai Trò:"));
        panel.add(vaiTroCombo);
        panel.add(ModernUIApplier.createModernInfoLabel("Tìm Kiếm:"));
        panel.add(searchField);

        return panel;
    }

    /**
     * Creates the table panel for displaying employees
     */
    private JPanel createTablePanel() {
        String[] columns = {"Mã Nhân Viên", "Họ Tên", "SĐT", "Email", "Lương", "Vai Trò"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        ModernUIApplier.applyModernTableStyle(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

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
        JPanel panel = ModernUIApplier.createTitledPanel("DANH SÁCH NHÂN VIÊN");
        panel.setLayout(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Creates the button panel for CRUD operations
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setOpaque(false);

        btnThem = ModernUIApplier.createPrimaryButton("THÊM");
        btnSua = ModernUIApplier.createPrimaryButton("SỬA");
        btnXoa = ModernUIApplier.createPrimaryButton("XÓA");
        btnClear = ModernUIApplier.createSecondaryButton("CLEAR");

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