package com.cinema.views.admin;

import com.cinema.controllers.SuatChieuController;
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

public class SuatChieuView extends JPanel {
    private DatabaseConnection databaseConnection;
    private JTextField txtNgayGioChieu, searchField;
    private JLabel txtMaSuatChieu;
    private JComboBox cbMaPhim, cbMaPhong;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnThem, btnSua, btnXoa, btnClear;
    private TableRowSorter<DefaultTableModel> sorter;
    private Integer selectedMaSuatChieu;

    public SuatChieuView() throws SQLException {
        initializeDatabase();
        initializeUI();
        new SuatChieuController(this);
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
        infoPanel.setBorder(BorderFactory.createTitledBorder("THÔNG TIN SUẤT CHIẾU"));

        JPanel fieldsPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        initializeFields(fieldsPanel);

        infoPanel.add(fieldsPanel, BorderLayout.CENTER);
        return infoPanel;
    }

    private void initializeFields(JPanel fieldsPanel) {
        txtMaSuatChieu = new JLabel();
        cbMaPhim = new JComboBox<>();
        cbMaPhong = new JComboBox<>();
        txtNgayGioChieu = new JTextField();
        searchField = new JTextField();

        addField(fieldsPanel, "Mã Suất Chiếu:", txtMaSuatChieu);
        addField(fieldsPanel, "Phim:", cbMaPhim);
        addField(fieldsPanel, "Phòng chiếu:", cbMaPhong);
        addField(fieldsPanel, "Ngày giờ chiếu:", txtNgayGioChieu);
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

    private JPanel createTablePanel() {
        String[] columns = {"Mã Suất Chiếu", "Tên Phim", "Phòng Chiếu", "Ngày Giờ Chiếu"};
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
                    selectedMaSuatChieu = (Integer) tableModel.getValueAt(selectedRow, 0);
                } else {
                    selectedMaSuatChieu = null;
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("DANH SÁCH SUẤT CHIẾU"));
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

    // Getters để controller truy cập
    public DatabaseConnection getDatabaseConnection() { return databaseConnection; }
    public JTextField getSearchField() { return searchField; }
    public JLabel getTxtMaSuatChieu() { return txtMaSuatChieu; }
    public JTextField getTxtNgayGioChieu() { return txtNgayGioChieu; }
    public JComboBox getCbMaPhim() { return cbMaPhim; }
    public JComboBox getCbMaPhong() { return cbMaPhong; }
    public JTable getTable() { return table; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JButton getBtnThem() { return btnThem; }
    public JButton getBtnSua() { return btnSua; }
    public JButton getBtnXoa() { return btnXoa; }
    public JButton getBtnClear() { return btnClear; }
    public String getSeacrhText() { return searchField.getText(); }
}