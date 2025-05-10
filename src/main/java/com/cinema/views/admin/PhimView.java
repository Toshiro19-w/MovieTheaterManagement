package com.cinema.views.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.cinema.controllers.PhimController;
import com.cinema.utils.DatabaseConnection;

/**
 * PhimView is a JPanel that provides a GUI for managing movie information.
 * It includes input fields for movie details, a table for displaying movies,
 * and buttons for CRUD operations.
 */
public class PhimView extends JPanel {
    private DatabaseConnection databaseConnection;
    private JTextField searchField;
    private JTextField txtTenPhim, txtTenTheLoai, txtThoiLuong,
            txtNgayKhoiChieu, txtNuocSanXuat, txtDinhDang, txtMoTa,
            txtDaoDien;
    private JLabel txtMaPhim, posterLabel;
    private String selectedPosterPath;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnThem, btnSua, btnXoa, btnClear;
    private TableRowSorter<DefaultTableModel> sorter;

    public PhimView() throws SQLException {
        initializeDatabase();
        initializeUI();
        new PhimController(this);
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
        setBorder((((new EmptyBorder(10, 10, 10, 10)))));

        // Initialize info panel
        JPanel infoPanel = createInfoPanel();

        // Initialize table
        JPanel tablePanel = createTablePanel();

        // Initialize button panel
        JPanel buttonPanel = createButtonPanel();

        // Combine panels
        add(infoPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Creates the information input panel
     */
    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel(new BorderLayout(10, 10));
        infoPanel.setBorder(BorderFactory.createTitledBorder("THÔNG TIN PHIM"));

        JPanel fieldsPanel = new JPanel(new GridLayout(6, 2, 10, 10));

        // Initialize fields
        initializeFields(fieldsPanel);

        // Initialize poster
        posterLabel = new JLabel();
        posterLabel.setPreferredSize(new Dimension(150, 200));
        posterLabel.setHorizontalAlignment(SwingConstants.CENTER);
        posterLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        infoPanel.add(fieldsPanel, BorderLayout.CENTER);
        infoPanel.add(posterLabel, BorderLayout.EAST);
        return infoPanel;
    }

    /**
     * Initializes input fields and their labels
     */
    private void initializeFields(JPanel fieldsPanel) {
        txtMaPhim = new JLabel();
        txtTenPhim = new JTextField();
        txtTenTheLoai = new JTextField();
        txtThoiLuong = new JTextField();
        txtNgayKhoiChieu = new JTextField();
        txtNuocSanXuat = new JTextField();
        txtDinhDang = new JTextField();
        txtMoTa = new JTextField();
        txtDaoDien = new JTextField();
        searchField = new JTextField();

        // Add components to panel
        addField(fieldsPanel, "Mã Phim:", txtMaPhim);
        addField(fieldsPanel, "Tên Phim:", txtTenPhim);
        addField(fieldsPanel, "Thể Loại:", txtTenTheLoai);
        addField(fieldsPanel, "Thời Lượng:", txtThoiLuong);
        addField(fieldsPanel, "Ngày Khởi Chiếu:", txtNgayKhoiChieu);
        addField(fieldsPanel, "Nước Sản Xuất:", txtNuocSanXuat);
        addField(fieldsPanel, "Định Dạng:", txtDinhDang);
        addField(fieldsPanel, "Mô Tả:", txtMoTa);
        addField(fieldsPanel, "Đạo Diễn:", txtDaoDien);
        addField(fieldsPanel, "Tìm Kiếm:", searchField);

        // Add image selection button
        JPanel chonAnhPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnChonAnh = new JButton("Chọn Ảnh");
        btnChonAnh.addActionListener(_ -> chonAnh());
        chonAnhPanel.add(btnChonAnh);
        fieldsPanel.add(new JLabel("Poster:"));
        fieldsPanel.add(chonAnhPanel);

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
     * Creates the table panel
     */
    private JPanel createTablePanel() {
        String[] columns = {"Mã Phim", "Tên Phim", "Thể Loại", "Thời Lượng",
                "Ngày Khởi Chiếu", "Nước Sản Xuất", "Định Dạng",
                "Mô Tả", "Đạo Diễn"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("DANH SÁCH PHIM"));
        return new JPanel(new BorderLayout()) {{ add(scrollPane, BorderLayout.CENTER); }};
    }

    /**
     * Creates the button panel
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
     * Handles image selection and display
     */
    private void chonAnh() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn ảnh phim");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Image files", "jpg", "png", "jpeg", "gif"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File selectedFile = fileChooser.getSelectedFile();
                selectedPosterPath = selectedFile.getName();

                // Copy file to resources
                Path source = selectedFile.toPath();
                Path target = Paths.get("src/main/resources/images/posters/" + selectedPosterPath);
                Files.createDirectories(target.getParent());
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);

                // Display image
                displayImage(selectedFile);
            } catch (IOException e) {
                showError("Không thể xử lý ảnh: " + e.getMessage());
            }
        }
    }

    /**
     * Displays the selected image in the poster label
     */
    private void displayImage(File file) {
        try {
            ImageIcon posterIcon = new ImageIcon(file.getPath());
            Image scaledImage = posterIcon.getImage().getScaledInstance(150, 200, Image.SCALE_SMOOTH);
            posterLabel.setIcon(new ImageIcon(scaledImage));
            posterLabel.setText("");
        } catch (Exception e) {
            posterLabel.setIcon(null);
            posterLabel.setText("Không thể hiển thị ảnh");
        }
    }

    /**
     * Shows an error message dialog
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    // Getters
    public DatabaseConnection getDatabaseConnection() { return databaseConnection; }
    public JTextField getSearchField() { return searchField; }
    public JLabel getTxtMaPhim() { return txtMaPhim; }
    public JTextField getTxtTenPhim() { return txtTenPhim; }
    public JTextField getTxtTenTheLoai() { return txtTenTheLoai; }
    public JTextField getTxtThoiLuong() { return txtThoiLuong; }
    public JTextField getTxtNgayKhoiChieu() { return txtNgayKhoiChieu; }
    public JTextField getTxtNuocSanXuat() { return txtNuocSanXuat; }
    public JTextField getTxtDinhDang() { return txtDinhDang; }
    public JTextField getTxtMoTa() { return txtMoTa; }
    public JTextField getTxtDaoDien() { return txtDaoDien; }
    public JLabel getPosterLabel() { return posterLabel; }
    public String getSelectedPosterPath() { return selectedPosterPath; }
    public void clearSelectedPosterPath() { selectedPosterPath = null; }
    public JTable getTable() { return table; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JButton getBtnThem() { return btnThem; }
    public JButton getBtnSua() { return btnSua; }
    public JButton getBtnXoa() { return btnXoa; }
    public JButton getBtnClear() { return btnClear; }
    public String getSearchText() { return searchField.getText(); }
}