package com.cinema.views.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import com.cinema.controllers.PhimController;
import com.cinema.services.PhimService;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.SimpleDocumentListener;
import com.cinema.utils.ValidationUtils;

public class PhimView extends JPanel {
    private JTextField txtMaPhim, txtTenPhim, txtThoiLuong, txtNgayKhoiChieu, txtNuocSanXuat, txtMoTa, txtDaoDien, txtSearch;
    private JComboBox<String> cbTenTheLoai, cbTrangThai, cbKieuPhim;
    private JLabel lblPoster, lblTenPhimError, lblThoiLuongError, lblNgayKhoiChieuError, lblNuocSanXuatError;
    private JButton btnThem, btnSua, btnXoa, btnClear, btnChonAnh;
    private DefaultTableModel tableModel;
    private JTable table;
    private DatabaseConnection dbConnection;
    private String selectedPosterPath;
    private ResourceBundle messages;
    private PhimService phimService;
    private PhimController controller;

    private static final Color PRIMARY_COLOR = new Color(59, 130, 246);
    private static final Color CINESTAR_BLUE = new Color(0, 51, 102);
    private static final Color CINESTAR_YELLOW = new Color(255, 204, 0);
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Color ROW_ALTERNATE_COLOR = new Color(240, 240, 240);
    private static final Font LABEL_FONT = new Font("Inter", Font.PLAIN, 14);
    private static final Font TITLE_FONT = new Font("Inter", Font.BOLD, 24);
    private static final Font BUTTON_FONT = new Font("Inter", Font.BOLD, 14);

    public PhimView() throws IOException {
        dbConnection = new DatabaseConnection();
        this.messages = ResourceBundle.getBundle("Messages");
        try {
            this.phimService = new PhimService(dbConnection);
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());

            // Khởi tạo giao diện
            initUI();

            // Khởi tạo controller
            this.controller = new PhimController(this);

            System.out.println("PhimView khởi tạo hoàn tất");
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối CSDL: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    messages.getString("dbConnectionError"),
                    messages.getString("error"),
                    JOptionPane.ERROR_MESSAGE);
        } catch (UnsupportedLookAndFeelException ex) {
            System.err.println("Lỗi khởi tạo giao diện: " + ex.getMessage());
        }
    }

    private void initUI() {
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        add(mainPanel);

        // Content Panel with shadow effect
        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.fillRoundRect(5, 5, getWidth() - 6, getHeight() - 6, 20, 20);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth() - 5, getHeight() - 5, 20, 20);
            }
        };
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        contentPanel.setLayout(new BorderLayout());
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Title Panel
        JLabel titleLabel = new JLabel("Quản lý phim", SwingConstants.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        contentPanel.add(titleLabel, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setPreferredSize(new Dimension(0, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 15, 5, 15);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Initialize components
        txtMaPhim = createStyledTextField();
        txtMaPhim.setEditable(false);
        txtTenPhim = createStyledTextField();
        txtTenPhim.setToolTipText("Nhập tên phim (không trùng lặp)");
        txtThoiLuong = createStyledTextField();
        txtThoiLuong.setToolTipText("Nhập thời lượng phim (số dương)");
        txtNgayKhoiChieu = createStyledTextField();
        txtNgayKhoiChieu.setToolTipText("Nhập ngày khởi chiếu (dd/MM/yyyy, từ hôm nay trở đi)");
        txtNuocSanXuat = createStyledTextField();
        txtNuocSanXuat.setToolTipText("Nhập nước sản xuất phim");
        txtMoTa = createStyledTextField();
        txtDaoDien = createStyledTextField();
        txtSearch = createStyledTextField();
        txtSearch.setToolTipText("Tìm kiếm theo tên phim");

        cbTenTheLoai = new JComboBox<>();
        cbTrangThai = new JComboBox<>();
        cbKieuPhim = new JComboBox<>();
        styleComboBox(cbTenTheLoai);
        styleComboBox(cbTrangThai);
        styleComboBox(cbKieuPhim);

        // Initialize poster label and button
        lblPoster = new JLabel("Không có ảnh");
        lblPoster.setPreferredSize(new Dimension(150, 200));
        lblPoster.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        btnChonAnh = createStyledButton("Chọn ảnh", PRIMARY_COLOR);
        btnChonAnh.setPreferredSize(new Dimension(150, 30));
        btnChonAnh.addActionListener(e -> chonAnh());

        // Initialize error labels
        lblTenPhimError = ValidationUtils.createErrorLabel();
        lblTenPhimError.setName("lblTenPhimError");
        lblThoiLuongError = ValidationUtils.createErrorLabel();
        lblThoiLuongError.setName("lblThoiLuongError");
        lblNgayKhoiChieuError = ValidationUtils.createErrorLabel();
        lblNgayKhoiChieuError.setName("lblNgayKhoiChieuError");
        lblNuocSanXuatError = ValidationUtils.createErrorLabel();
        lblNuocSanXuatError.setName("lblNuocSanXuatError");

        // Left column: Mã phim, Tên phim, Thể loại, Đạo diễn
        gbc.gridx = 0;
        gbc.gridy = 0;
        addFormField(formPanel, "Mã phim:", txtMaPhim, null, gbc);

        gbc.gridy++;
        addFormField(formPanel, "Tên phim:", txtTenPhim, lblTenPhimError, gbc);

        gbc.gridy++;
        addFormField(formPanel, "Thể loại:", cbTenTheLoai, null, gbc);

        gbc.gridy++;
        addFormField(formPanel, "Đạo diễn:", txtDaoDien, null, gbc);

        // Middle column: Thời lượng, Ngày khởi chiếu, Trạng thái, Định dạng
        gbc.gridx = 2;
        gbc.gridy = 0;
        addFormField(formPanel, "Thời lượng (phút):", txtThoiLuong, lblThoiLuongError, gbc);

        gbc.gridy++;
        addFormField(formPanel, "Ngày khởi chiếu:", txtNgayKhoiChieu, lblNgayKhoiChieuError, gbc);

        gbc.gridy++;
        addFormField(formPanel, "Trạng thái:", cbTrangThai, null, gbc);

        gbc.gridy++;
        addFormField(formPanel, "Định dạng:", cbKieuPhim, null, gbc);

        // Right column: Nước sản xuất, Mô tả, Poster
        gbc.gridx = 4;
        gbc.gridy = 0;
        addFormField(formPanel, "Nước sản xuất:", txtNuocSanXuat, lblNuocSanXuatError, gbc);

        gbc.gridy++;
        addFormField(formPanel, "Mô tả:", txtMoTa, null, gbc);

        gbc.gridy++;
        gbc.gridheight = 2;
        JLabel lblPosterTitle = new JLabel("Poster:", SwingConstants.RIGHT);
        lblPosterTitle.setFont(LABEL_FONT);
        gbc.gridwidth = 1;
        formPanel.add(lblPosterTitle, gbc);

        gbc.gridx++;
        JPanel posterPanel = new JPanel(new BorderLayout(0, 5));
        posterPanel.setOpaque(false);
        posterPanel.add(lblPoster, BorderLayout.CENTER);
        posterPanel.add(btnChonAnh, BorderLayout.SOUTH);
        formPanel.add(posterPanel, gbc);
        gbc.gridheight = 1;

        contentPanel.add(formPanel, BorderLayout.NORTH);

        // Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(false);
        tablePanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);
        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(LABEL_FONT);
        searchPanel.add(lblSearch);
        txtSearch.setPreferredSize(new Dimension(250, 30));
        searchPanel.add(txtSearch);
        tablePanel.add(searchPanel, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(
                new Object[]{"Mã phim", "Tên phim", "Thể loại", "Thời lượng", "Ngày KC", "Trạng thái"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (isRowSelected(row)) {
                    c.setBackground(new Color(255, 204, 0, 100));
                } else if (row % 2 == 0) {
                    c.setBackground(Color.WHITE);
                } else {
                    c.setBackground(ROW_ALTERNATE_COLOR);
                }
                return c;
            }
        };
        table.setFont(LABEL_FONT);
        table.getTableHeader().setBackground(CINESTAR_BLUE);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(BUTTON_FONT);
        table.setRowHeight(30);
        table.setGridColor(Color.LIGHT_GRAY);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setMinimumSize(new Dimension(800, 400));

        // Thêm renderer cho cột trạng thái
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if ("Đang chiếu".equals(value)) {
                    label.setIcon(new ImageIcon(getClass().getResource("/images/check.png")));
                    label.setText(" Đang chiếu");
                } else if ("Kết thúc".equals(value)) {
                    label.setIcon(new ImageIcon(getClass().getResource("/images/x.png")));
                    label.setText(" Kết thúc");
                } else {
                    label.setIcon(null);
                    label.setText(value != null ? value.toString() : "");
                }
                label.setIconTextGap(5);
                return label;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        contentPanel.add(tablePanel, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);
        btnThem = createStyledButton("Thêm", PRIMARY_COLOR);
        btnSua = createStyledButton("Sửa", PRIMARY_COLOR);
        btnXoa = createStyledButton("Xóa", PRIMARY_COLOR);
        btnClear = createStyledButton("Làm mới", PRIMARY_COLOR);
        buttonPanel.add(btnThem);
        buttonPanel.add(btnSua);
        buttonPanel.add(btnXoa);
        buttonPanel.add(btnClear);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add listeners
        addValidationListeners();
        setupButtonActions();

        // Log trạng thái của tableModel sau khi khởi tạo
        System.out.println("TableModel khởi tạo với " + tableModel.getRowCount() + " hàng và " + tableModel.getColumnCount() + " cột.");
    }

    private void addFormField(JPanel panel, String labelText, JComponent field, JLabel errorLabel, GridBagConstraints gbc) {
        JLabel label = new JLabel(labelText, SwingConstants.RIGHT);
        label.setFont(LABEL_FONT);
        gbc.gridwidth = 1;
        panel.add(label, gbc);

        JPanel fieldPanel = new JPanel(new BorderLayout(0, 2));
        fieldPanel.setOpaque(false);
        field.setPreferredSize(new Dimension(250, 30));
        fieldPanel.add(field, BorderLayout.NORTH);

        if (errorLabel != null) {
            errorLabel.setForeground(new Color(220, 53, 69));
            errorLabel.setFont(new Font(LABEL_FONT.getFamily(), Font.PLAIN, 12));
            errorLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 0, 0));
            errorLabel.setPreferredSize(new Dimension(250, 20));
            errorLabel.setVisible(false);
            fieldPanel.add(errorLabel, BorderLayout.CENTER);
            fieldPanel.revalidate();
            fieldPanel.repaint();
        }

        fieldPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        gbc.gridx++;
        gbc.gridwidth = 1;
        panel.add(fieldPanel, gbc);
        gbc.gridx--;
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(LABEL_FONT);
        comboBox.setPreferredSize(new Dimension(250, 30));
        comboBox.setBackground(Color.WHITE);
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(LABEL_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        return field;
    }

    private void chonAnh() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn poster phim");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                selectedPosterPath = selectedFile.getAbsolutePath();
                ImageIcon imageIcon = new ImageIcon(selectedPosterPath);
                Image scaledImage = imageIcon.getImage().getScaledInstance(150, 200, Image.SCALE_SMOOTH);
                lblPoster.setIcon(new ImageIcon(scaledImage));
                lblPoster.setText("");
            } catch (Exception ex) {
                lblPoster.setIcon(null);
                lblPoster.setText("Không thể tải ảnh");
                JOptionPane.showMessageDialog(this, "Lỗi khi tải ảnh: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addValidationListeners() {
        txtTenPhim.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            ValidationUtils.validateMovieTitleField(txtTenPhim, lblTenPhimError, messages, phimService, txtMaPhim.getText());
            updateButtonState();
        }));

        txtThoiLuong.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            ValidationUtils.validateDurationField(txtThoiLuong, lblThoiLuongError, messages);
            updateButtonState();
        }));

        txtNgayKhoiChieu.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            ValidationUtils.validateStartDateField(txtNgayKhoiChieu, lblNgayKhoiChieuError, messages);
            updateButtonState();
        }));

        txtNuocSanXuat.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            ValidationUtils.validateCountryField(txtNuocSanXuat, lblNuocSanXuatError, messages);
            updateButtonState();
        }));
    }

    private void updateButtonState() {
        boolean isValid = isFormValid();
        btnThem.setEnabled(isValid);
        btnSua.setEnabled(isValid && !txtMaPhim.getText().trim().isEmpty());
    }

    private boolean isFormValid() {
        return !lblTenPhimError.isVisible() &&
               !lblThoiLuongError.isVisible() &&
               !lblNgayKhoiChieuError.isVisible() &&
               !lblNuocSanXuatError.isVisible() &&
               !txtTenPhim.getText().trim().isEmpty() &&
               !txtThoiLuong.getText().trim().isEmpty() &&
               !txtNgayKhoiChieu.getText().trim().isEmpty() &&
               !txtNuocSanXuat.getText().trim().isEmpty();
    }

    private void setupButtonActions() {
        btnThem.addActionListener(unused -> {
            if (isFormValid()) {
                try {
                    controller.themPhim();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                            messages.getString("errorAddingMovie"),
                            messages.getString("error"),
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnSua.addActionListener(unused -> {
            if (isFormValid()) {
                try {
                    controller.suaPhim();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                            messages.getString("errorUpdatingMovie"),
                            messages.getString("error"),
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnXoa.addActionListener(unused -> {
            if (JOptionPane.showConfirmDialog(this,
                    messages.getString("confirmDeleteMovie"),
                    messages.getString("confirm"),
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    controller.xoaPhim();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                            messages.getString("errorDeletingMovie"),
                            messages.getString("error"),
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnClear.addActionListener(unused -> {
            clearForm();
            updateButtonState();
        });
    }

    public void clearForm() {
        txtMaPhim.setText("");
        txtTenPhim.setText("");
        txtThoiLuong.setText("");
        txtNgayKhoiChieu.setText("");
        txtNuocSanXuat.setText("");
        txtMoTa.setText("");
        txtDaoDien.setText("");
        cbTenTheLoai.setSelectedIndex(-1);
        cbTrangThai.setSelectedIndex(-1);
        cbKieuPhim.setSelectedIndex(-1);
        lblPoster.setIcon(null);
        lblPoster.setText("Không có ảnh");
        selectedPosterPath = null;

        ValidationUtils.hideError(lblTenPhimError);
        ValidationUtils.hideError(lblThoiLuongError);
        ValidationUtils.hideError(lblNgayKhoiChieuError);
        ValidationUtils.hideError(lblNuocSanXuatError);

        ValidationUtils.setNormalBorder(txtTenPhim);
        ValidationUtils.setNormalBorder(txtThoiLuong);
        ValidationUtils.setNormalBorder(txtNgayKhoiChieu);
        ValidationUtils.setNormalBorder(txtNuocSanXuat);

        table.clearSelection();
    }

    // Getters
    public JTextField getTxtMaPhim() { return txtMaPhim; }
    public JTextField getTxtTenPhim() { return txtTenPhim; }
    public JTextField getTxtThoiLuong() { return txtThoiLuong; }
    public JTextField getTxtNgayKhoiChieu() { return txtNgayKhoiChieu; }
    public JTextField getTxtNuocSanXuat() { return txtNuocSanXuat; }
    public JTextField getTxtMoTa() { return txtMoTa; }
    public JTextField getTxtDaoDien() { return txtDaoDien; }
    public JTextField getSearchField() { return txtSearch; }
    public String getSearchText() { return txtSearch.getText().trim(); }
    public JComboBox<String> getCbTenTheLoai() { return cbTenTheLoai; }
    public JComboBox<String> getCbTrangthai() { return cbTrangThai; }
    public JComboBox<String> getCbKieuPhim() { return cbKieuPhim; }
    public JLabel getPosterLabel() { return lblPoster; }
    public JButton getBtnThem() { return btnThem; }
    public JButton getBtnSua() { return btnSua; }
    public JButton getBtnXoa() { return btnXoa; }
    public JButton getBtnClear() { return btnClear; }
    public JTable getTable() { return table; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public DatabaseConnection getDatabaseConnection() { return dbConnection; }
    public String getSelectedPosterPath() { return selectedPosterPath; }
    public void setSelectedPosterPath(String path) { this.selectedPosterPath = path; }
    public void clearSelectedPosterPath() { this.selectedPosterPath = null; }
}