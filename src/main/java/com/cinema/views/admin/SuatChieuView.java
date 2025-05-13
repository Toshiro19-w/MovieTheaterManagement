package com.cinema.views.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.cinema.controllers.PhongChieuController;
import com.cinema.controllers.SuatChieuController;
import com.cinema.utils.DatabaseConnection;

public class SuatChieuView extends JPanel {
    private DatabaseConnection databaseConnection;

    // SuatChieu components
    private JTextField txtNgayGioChieu, suatChieuSearchField;
    private JLabel txtMaSuatChieu;
    private JComboBox cbMaPhim, cbMaPhong;
    private JTable suatChieuTable;
    private DefaultTableModel suatChieuTableModel;
    private JButton btnThemSuat, btnSuaSuat, btnXoaSuat, btnClearSuat;
    private TableRowSorter<DefaultTableModel> suatChieuSorter;
    private Integer selectedMaSuatChieu;

    // PhongChieu components
    private JTable phongChieuTable;
    private DefaultTableModel phongChieuTableModel;
    private Integer selectedMaPhong;

    public SuatChieuView() throws SQLException {
        initializeDatabase();
        initializeUI();
        new SuatChieuController(this);
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

        // Create split pane for side-by-side layout
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(0.5); // Split evenly
        splitPane.setResizeWeight(0.5); // Maintain proportional resizing

        // SuatChieu panel (left)
        JPanel suatChieuPanel = createSuatChieuPanel();
        // PhongChieu panel (right)
        JPanel phongChieuPanel = createPhongChieuPanel();

        splitPane.setLeftComponent(suatChieuPanel);
        splitPane.setRightComponent(phongChieuPanel);

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createSuatChieuPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Info panel
        JPanel infoPanel = new JPanel(new BorderLayout(10, 10));
        infoPanel.setBorder(BorderFactory.createTitledBorder("THÔNG TIN SUẤT CHIẾU"));
        JPanel fieldsPanel = new JPanel(new GridLayout(6, 2, 10, 10));

        initializeSuatChieuFields(fieldsPanel);
        infoPanel.add(fieldsPanel, BorderLayout.CENTER);

        // Table panel
        JPanel tablePanel = createSuatChieuTablePanel();

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnThemSuat = new JButton("THÊM");
        btnSuaSuat = new JButton("SỬA");
        btnXoaSuat = new JButton("XÓA");
        btnClearSuat = new JButton("CLEAR");
        buttonPanel.add(btnThemSuat);
        buttonPanel.add(btnSuaSuat);
        buttonPanel.add(btnXoaSuat);
        buttonPanel.add(btnClearSuat);

        panel.add(infoPanel, BorderLayout.NORTH);
        panel.add(tablePanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void initializeSuatChieuFields(JPanel fieldsPanel) {
        txtMaSuatChieu = new JLabel();
        cbMaPhim = new JComboBox<>();
        cbMaPhong = new JComboBox<>();
        txtNgayGioChieu = new JTextField();
        suatChieuSearchField = new JTextField();

        addField(fieldsPanel, "Mã Suất Chiếu:", txtMaSuatChieu);
        addField(fieldsPanel, "Phim:", cbMaPhim);
        addField(fieldsPanel, "Phòng chiếu:", cbMaPhong);
        addField(fieldsPanel, "Ngày giờ chiếu:", txtNgayGioChieu);
        setPlaceholder(txtNgayGioChieu);
        addField(fieldsPanel, "Tìm Kiếm:", suatChieuSearchField);

        // Add search functionality
        suatChieuSearchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String searchText = suatChieuSearchField.getText();
                if (searchText.trim().isEmpty()) {
                    suatChieuSorter.setRowFilter(null);
                } else {
                    suatChieuSorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
                }
            }
        });
    }

    private JPanel createSuatChieuTablePanel() {
        String[] columns = {"Mã Suất Chiếu", "Tên Phim", "Phòng Chiếu", "Ngày Giờ Chiếu"};
        suatChieuTableModel = new DefaultTableModel(columns, 0);
        suatChieuTable = new JTable(suatChieuTableModel);
        suatChieuTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        suatChieuSorter = new TableRowSorter<>(suatChieuTableModel);
        suatChieuTable.setRowSorter(suatChieuSorter);

        suatChieuTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = suatChieuTable.getSelectedRow();
                if (selectedRow >= 0) {
                    selectedMaSuatChieu = (Integer) suatChieuTableModel.getValueAt(selectedRow, 0);
                } else {
                    selectedMaSuatChieu = null;
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(suatChieuTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("DANH SÁCH SUẤT CHIẾU"));
        return new JPanel(new BorderLayout()) {{ add(scrollPane, BorderLayout.CENTER); }};
    }

    private JPanel createPhongChieuPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        // Table panel
        JPanel tablePanel = createPhongChieuTablePanel();
        panel.add(tablePanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPhongChieuTablePanel() {
        String[] columns = {"Mã Phòng", "Tên Phòng", "Số Lượng Ghế", "Loại Phòng"};
        phongChieuTableModel = new DefaultTableModel(columns, 0);
        phongChieuTable = new JTable(phongChieuTableModel);
        phongChieuTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        TableRowSorter<DefaultTableModel> phongChieuSorter = new TableRowSorter<>(phongChieuTableModel);
        phongChieuTable.setRowSorter(phongChieuSorter);

        phongChieuTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = phongChieuTable.getSelectedRow();
                if (selectedRow >= 0) {
                    selectedMaPhong = (Integer) phongChieuTableModel.getValueAt(selectedRow, 0);
                } else {
                    selectedMaPhong = null;
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(phongChieuTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("DANH SÁCH PHÒNG CHIẾU"));
        return new JPanel(new BorderLayout()) {{ add(scrollPane, BorderLayout.CENTER); }};
    }

    private void addField(JPanel panel, String labelText, JComponent component) {
        panel.add(new JLabel(labelText));
        panel.add(component);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private void setPlaceholder(JTextField field) {
        field.setText("dd/MM/yyyy HH:mm:ss");
        field.setForeground(Color.GRAY);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals("dd/MM/yyyy HH:mm:ss")) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(Color.GRAY);
                    field.setText("dd/MM/yyyy HH:mm:ss");
                }
            }
        });
    }

    // Getters for SuatChieuController
    public DatabaseConnection getDatabaseConnection() { return databaseConnection; }
    public JTextField getSuatChieuSearchField() { return suatChieuSearchField; }
    public JLabel getTxtMaSuatChieu() { return txtMaSuatChieu; }
    public JTextField getTxtNgayGioChieu() { return txtNgayGioChieu; }
    public JComboBox getCbMaPhim() { return cbMaPhim; }
    public JComboBox getCbMaPhong() { return cbMaPhong; }
    public JTable getSuatChieuTable() { return suatChieuTable; }
    public DefaultTableModel getSuatChieuTableModel() { return suatChieuTableModel; }
    public JButton getBtnThemSuat() { return btnThemSuat; }
    public JButton getBtnSuaSuat() { return btnSuaSuat; }
    public JButton getBtnXoaSuat() { return btnXoaSuat; }
    public JButton getBtnClearSuat() { return btnClearSuat; }
    public String getSuatChieuSearchText() { return suatChieuSearchField.getText(); }
    public Integer getSelectedMaSuatChieu() { return selectedMaSuatChieu; }

    // Getters for PhongChieuController
    public JTable getPhongChieuTable() { return phongChieuTable; }
    public DefaultTableModel getPhongChieuTableModel() { return phongChieuTableModel; }
    public Integer getSelectedMaPhong() { return selectedMaPhong; }
}