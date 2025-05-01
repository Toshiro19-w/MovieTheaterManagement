package com.cinema.views.admin;

import com.cinema.controllers.VeController;
import com.cinema.utils.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.sql.SQLException;

public class VeView extends JPanel {
    private DatabaseConnection databaseConnection;
    private JLabel txtMaVe, txtNgayDat;
    private JTextField txtGiaVe, txtSoGhe, txtTenPhong, txtTenPhim, txtNgayGioChieu, searchField;
    private JComboBox<String> cbTrangThai;
    private JTable tableVe, tableKhachHang;
    private DefaultTableModel tableVeModel, tableKhachHangModel;
    private JButton btnThem, btnSua, btnXoa, btnClear;
    private TableRowSorter<DefaultTableModel> sorter;

    public VeView() throws SQLException {
        initializeDatabase();
        initializeUI();
        new VeController(this);
    }

    private void initializeDatabase() {
        try {
            databaseConnection = new DatabaseConnection();
        } catch (IOException e) {
            showError("Không thể kết nối cơ sở dữ liệu: " + e.getMessage());
        }
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Initialize panels
        JPanel topPanel = createInfoPanel();
        JPanel centerPanel = createCenterPanel();
        JPanel buttonPanel = createButtonPanel();

        // Add components to main panel
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel(new BorderLayout(10, 10));
        infoPanel.setBorder(BorderFactory.createTitledBorder("THÔNG TIN VÉ"));

        JPanel fieldsPanel = new JPanel(new GridLayout(5, 4, 10, 10));
        initializeFields(fieldsPanel);

        infoPanel.add(fieldsPanel, BorderLayout.CENTER);
        return infoPanel;
    }

    private void initializeFields(JPanel fieldsPanel) {
        txtMaVe = new JLabel();
        cbTrangThai = new JComboBox<>(new String[]{"AVAILABLE", "BOOKED", "CANCELLED", "PAID"});
        txtGiaVe = new JTextField();
        txtSoGhe = new JTextField();
        txtNgayDat = new JLabel();
        txtTenPhong = new JTextField();
        txtNgayGioChieu = new JTextField();
        txtTenPhim = new JTextField();
        searchField = new JTextField();

        addField(fieldsPanel, "Mã Vé:", txtMaVe);
        addField(fieldsPanel, "Trạng Thái:", cbTrangThai);
        addField(fieldsPanel, "Giá Vé:", txtGiaVe);
        addField(fieldsPanel, "Số Ghế:", txtSoGhe);
        addField(fieldsPanel, "Ngày Đặt:", txtNgayDat);
        addField(fieldsPanel, "Tên Phòng:", txtTenPhong);
        addField(fieldsPanel, "Ngày Giờ Chiếu:", txtNgayGioChieu);
        addField(fieldsPanel, "Tên Phim:", txtTenPhim);
        setPlaceholder(txtNgayGioChieu, "dd/MM/yyyy HH:mm:ss");
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

    private void addField(JPanel panel, String labelText, JComponent component) {
        panel.add(new JLabel(labelText));
        panel.add(component);
    }

    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        // Ticket table
        JPanel vePanel = createVeTablePanel();
        // Customer table
        JPanel khachHangPanel = createKhachHangTablePanel();

        centerPanel.add(vePanel);
        centerPanel.add(khachHangPanel);
        return centerPanel;
    }

    private JPanel createVeTablePanel() {
        String[] columns = {"Mã Vé", "Trạng Thái", "Giá Vé", "Số Ghế", "Ngày Đặt", "Tên Phòng", "Ngày Giờ Chiếu", "Tên Phim"};
        tableVeModel = new DefaultTableModel(columns, 0);
        tableVe = new JTable(tableVeModel);
        tableVe.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        sorter = new TableRowSorter<>(tableVeModel);
        tableVe.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(tableVe);
        scrollPane.setBorder(BorderFactory.createTitledBorder("DANH SÁCH VÉ"));
        return new JPanel(new BorderLayout()) {{ add(scrollPane, BorderLayout.CENTER); }};
    }

    private JPanel createKhachHangTablePanel() {
        String[] columns = {"Tên Khách Hàng", "Số Điện Thoại", "Email"};
        tableKhachHangModel = new DefaultTableModel(columns, 0);
        tableKhachHang = new JTable(tableKhachHangModel);
        tableKhachHang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(tableKhachHang);
        scrollPane.setBorder(BorderFactory.createTitledBorder("THÔNG TIN KHÁCH HÀNG"));
        return new JPanel(new BorderLayout()) {{ add(scrollPane, BorderLayout.CENTER); }};
    }

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

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private void setPlaceholder(JTextField field, String placeholder) {
        field.setText(placeholder);
        field.setForeground(Color.GRAY);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(Color.GRAY);
                    field.setText(placeholder);
                }
            }
        });
    }

    // Getters for controller access
    public DatabaseConnection getDatabaseConnection() { return databaseConnection; }
    public JLabel getTxtMaVe() { return txtMaVe; }
    public JComboBox<String> getCbTrangThai() { return cbTrangThai; }
    public JTextField getTxtGiaVe() { return txtGiaVe; }
    public JTextField getTxtSoGhe() { return txtSoGhe; }
    public JLabel getTxtNgayDat() { return txtNgayDat; }
    public JTextField getTxtTenPhong() { return txtTenPhong; }
    public JTextField getTxtNgayGioChieu() { return txtNgayGioChieu; }
    public JTextField getTxtTenPhim() { return txtTenPhim; }
    public JTextField getSearchField() { return searchField; }
    public JTable getTable() { return tableVe; }
    public DefaultTableModel getTableModel() { return tableVeModel; }
    public JTable getTableKhachHang() { return tableKhachHang; }
    public DefaultTableModel getTableKhachHangModel() { return tableKhachHangModel; }
    public JButton getBtnThem() { return btnThem; }
    public JButton getBtnSua() { return btnSua; }
    public JButton getBtnXoa() { return btnXoa; }
    public JButton getBtnClear() { return btnClear; }
}