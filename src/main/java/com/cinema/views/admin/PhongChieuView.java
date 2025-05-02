package com.cinema.views.admin;

import com.cinema.controllers.PhongChieuController;
import com.cinema.utils.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class PhongChieuView extends JPanel {
    private DatabaseConnection databaseConnection;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel txtMaPhong;
    private JTextField txtTenPhong, txtSoLuongGhe, txtLoaiPhong, searchField;
    private JButton btnThem, btnClear;
    private TableRowSorter<DefaultTableModel> sorter;
    private Integer selectedMaPhong;

    public PhongChieuView() {
        initializeDatabase();
        initializeUI();
        new PhongChieuController(this);
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
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        JPanel infoPanel = createInfoPanel();
        JPanel tablePanel = createTablePanel();
        JPanel buttonPanel = createButtonPanel();

        // Combine info and account panels
        topPanel.add(infoPanel, BorderLayout.CENTER);

        // Add components to main panel
        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel(new BorderLayout(10, 10));
        infoPanel.setBorder(BorderFactory.createTitledBorder("THÔNG TIN PHÒNG CHIẾU"));

        JPanel fieldsPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        initializeFields(fieldsPanel);

        infoPanel.add(fieldsPanel, BorderLayout.CENTER);
        return infoPanel;
    }

    private void initializeFields(JPanel fieldsPanel) {
        txtMaPhong = new JLabel();
        txtTenPhong = new JTextField();
        txtSoLuongGhe = new JTextField();
        txtLoaiPhong = new JTextField();
        searchField = new JTextField();

        addField(fieldsPanel, "Mã Phòng:", txtMaPhong);
        addField(fieldsPanel, "Tên Phòng:", txtTenPhong);
        addField(fieldsPanel, "Số Lượng Ghế:", txtSoLuongGhe);
        addField(fieldsPanel, "Loại Phòng:", txtLoaiPhong);
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

    private JPanel createTablePanel() {
        String[] columns = {"Mã Phòng", "Tên Phòng", "Số Lượng Ghế", "Loại Phòng"};
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
                    selectedMaPhong = (Integer) tableModel.getValueAt(selectedRow, 0);
                } else {
                    selectedMaPhong = null;
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("DANH SÁCH PHÒNG CHIẾU"));
        return new JPanel(new BorderLayout()) {{ add(scrollPane, BorderLayout.CENTER); }};
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnThem = new JButton("THÊM");
        btnClear = new JButton("CLEAR");

        buttonPanel.add(btnThem);
        buttonPanel.add(btnClear);
        return buttonPanel;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    // Getters để controller truy cập
    public DatabaseConnection getDatabaseConnection() {
        return databaseConnection;
    }
    public JTable getTable() {
        return table;
    }
    public JTextField getTxtTenPhong() {
        return txtTenPhong;
    }
    public DefaultTableModel getTableModel() {
        return tableModel;
    }
    public JLabel getTxtMaPhong() {
        return txtMaPhong;
    }
    public JTextField getTxtSoLuongGhe() {
        return txtSoLuongGhe;
    }
    public JTextField getTxtLoaiPhong() {
        return txtLoaiPhong;
    }
    public JButton getBtnThem() {
        return btnThem;
    }
    public JButton getBtnClear() {
        return btnClear;
    }
    public String getSeacrhText() { return searchField.getText(); }
    public JTextField getSearchField() {
        return searchField;
    }
    public Integer getSelectedMaPhong() {
        return selectedMaPhong;
    }
}