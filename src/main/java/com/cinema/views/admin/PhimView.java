package com.cinema.views.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import com.cinema.components.CountryComboBox;
import com.cinema.components.RoundedPanel;
import com.cinema.components.StyledButton;
import com.cinema.components.UnderlineTextField;
import com.cinema.controllers.PhimController;
import com.cinema.models.repositories.SuatChieuRepository;
import com.cinema.models.repositories.VeRepository;
import com.cinema.services.PhimService;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.SimpleDocumentListener;
import com.cinema.utils.ValidationUtils;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class PhimView extends JPanel {
    private UnderlineTextField txtMaPhim, txtTenPhim, txtThoiLuong, txtNgayKhoiChieu, txtMoTa, txtDaoDien, txtSearch;
    private CountryComboBox cbNuocSanXuat;
    private JComboBox<String> cbTenTheLoai, cbTrangThai, cbKieuPhim;
    private JLabel lblPoster, lblTenPhimError, lblThoiLuongError, lblNgayKhoiChieuError, lblNuocSanXuatError;
    private StyledButton btnThem, btnSua, btnXoa, btnClear, btnChonAnh;
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
        setBackground(BACKGROUND_COLOR);

        // Content Panel with shadow effect
        RoundedPanel contentPanel = new RoundedPanel(20, true, new Color(0, 0, 0, 20));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        contentPanel.setLayout(new BorderLayout());
        add(contentPanel, BorderLayout.CENTER);

        // Title Panel
        JLabel titleLabel = new JLabel("Quản lý phim", SwingConstants.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        contentPanel.add(titleLabel, BorderLayout.NORTH);

        // Initialize components
        initializeComponents();

        // Form Panel using JGoodies FormLayout
        RoundedPanel formPanel = createFormPanel();
        contentPanel.add(formPanel, BorderLayout.NORTH);

        // Table Panel
        RoundedPanel tablePanel = createTablePanel();
        contentPanel.add(tablePanel, BorderLayout.CENTER);

        // Button Panel
        RoundedPanel buttonPanel = createButtonPanel();
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add listeners
        addValidationListeners();
        setupButtonActions();

        // Log trạng thái của tableModel sau khi khởi tạo
        System.out.println("TableModel khởi tạo với " + tableModel.getRowCount() + " hàng và " + tableModel.getColumnCount() + " cột.");
    }

    private void initializeComponents() {
        // Text fields
        txtMaPhim = createStyledUnderlineTextField();
        txtMaPhim.setEditable(false);
        txtMaPhim.setPlaceholder("Mã phim tự động");
        
        txtTenPhim = createStyledUnderlineTextField();
        txtTenPhim.setPlaceholder("Nhập tên phim");
        txtTenPhim.setToolTipText("Nhập tên phim (không trùng lặp)");
        
        txtThoiLuong = createStyledUnderlineTextField();
        txtThoiLuong.setPlaceholder("Nhập thời lượng (phút)");
        txtThoiLuong.setToolTipText("Nhập thời lượng phim (số dương)");
        
        txtNgayKhoiChieu = createStyledUnderlineTextField();
        txtNgayKhoiChieu.setPlaceholder("dd/MM/yyyy");
        txtNgayKhoiChieu.setToolTipText("Nhập ngày khởi chiếu (dd/MM/yyyy, từ hôm nay trở đi)");
        
        cbNuocSanXuat = new CountryComboBox();
        cbNuocSanXuat.setFont(LABEL_FONT);
        cbNuocSanXuat.setPreferredSize(new Dimension(250, 30));
        
        txtMoTa = createStyledUnderlineTextField();
        txtMoTa.setPlaceholder("Nhập mô tả phim");
        
        txtDaoDien = createStyledUnderlineTextField();
        txtDaoDien.setPlaceholder("Nhập tên đạo diễn");
        
        txtSearch = createStyledUnderlineTextField();
        txtSearch.setPlaceholder("Tìm kiếm phim...");
        txtSearch.setToolTipText("Tìm kiếm theo tên phim");

        // Combo boxes
        cbTenTheLoai = new JComboBox<>();
        cbTrangThai = new JComboBox<>();
        cbKieuPhim = new JComboBox<>();
        styleComboBox(cbTenTheLoai);
        styleComboBox(cbTrangThai);
        styleComboBox(cbKieuPhim);

        // Poster label and button
        lblPoster = new JLabel("Không có ảnh", SwingConstants.CENTER);
        lblPoster.setPreferredSize(new Dimension(150, 200));
        lblPoster.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        btnChonAnh = new StyledButton("Chọn ảnh", PRIMARY_COLOR);
        btnChonAnh.setPreferredSize(new Dimension(150, 30));
        btnChonAnh.addActionListener(_ -> chonAnh());

        // Error labels
        lblTenPhimError = ValidationUtils.createErrorLabel();
        lblTenPhimError.setName("lblTenPhimError");
        lblThoiLuongError = ValidationUtils.createErrorLabel();
        lblThoiLuongError.setName("lblThoiLuongError");
        lblNgayKhoiChieuError = ValidationUtils.createErrorLabel();
        lblNgayKhoiChieuError.setName("lblNgayKhoiChieuError");
        lblNuocSanXuatError = ValidationUtils.createErrorLabel();
        lblNuocSanXuatError.setName("lblNuocSanXuatError");

        // Action buttons
        btnThem = new StyledButton("Thêm", PRIMARY_COLOR);
        btnSua = new StyledButton("Sửa", PRIMARY_COLOR);
        btnXoa = new StyledButton("Xóa", PRIMARY_COLOR);
        btnClear = new StyledButton("Làm mới", PRIMARY_COLOR);
    }

    private RoundedPanel createFormPanel() {
        RoundedPanel formPanel = new RoundedPanel(15);
        formPanel.setBackground(new Color(250, 250, 250));
        
        // Sử dụng JGoodies FormLayout
        FormLayout layout = new FormLayout(
            "right:pref, 8dlu, 200dlu, 15dlu, right:pref, 8dlu, 200dlu, 15dlu, right:pref, 8dlu, 200dlu",
            "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p");
        
        PanelBuilder builder = new PanelBuilder(layout, formPanel);
        builder.border(Borders.DIALOG);
        CellConstraints cc = new CellConstraints();
        
        // Cột 1: Mã phim, Tên phim, Thể loại, Đạo diễn
        builder.addLabel("Mã phim:", cc.xy(1, 1)).setFont(LABEL_FONT);
        builder.add(txtMaPhim, cc.xy(3, 1));
        
        builder.addLabel("Tên phim:", cc.xy(1, 3)).setFont(LABEL_FONT);
        JPanel tenPhimPanel = new JPanel(new BorderLayout());
        tenPhimPanel.setOpaque(false);
        tenPhimPanel.add(txtTenPhim, BorderLayout.CENTER);
        tenPhimPanel.add(lblTenPhimError, BorderLayout.SOUTH);
        builder.add(tenPhimPanel, cc.xy(3, 3));
        
        builder.addLabel("Thể loại:", cc.xy(1, 5)).setFont(LABEL_FONT);
        builder.add(cbTenTheLoai, cc.xy(3, 5));
        
        builder.addLabel("Đạo diễn:", cc.xy(1, 7)).setFont(LABEL_FONT);
        builder.add(txtDaoDien, cc.xy(3, 7));
        
        // Cột 2: Thời lượng, Ngày khởi chiếu, Trạng thái, Kiểu phim
        builder.addLabel("Thời lượng (phút):", cc.xy(5, 1)).setFont(LABEL_FONT);
        JPanel thoiLuongPanel = new JPanel(new BorderLayout());
        thoiLuongPanel.setOpaque(false);
        thoiLuongPanel.add(txtThoiLuong, BorderLayout.CENTER);
        thoiLuongPanel.add(lblThoiLuongError, BorderLayout.SOUTH);
        builder.add(thoiLuongPanel, cc.xy(7, 1));
        
        builder.addLabel("Ngày khởi chiếu:", cc.xy(5, 3)).setFont(LABEL_FONT);
        JPanel ngayKCPanel = new JPanel(new BorderLayout());
        ngayKCPanel.setOpaque(false);
        ngayKCPanel.add(txtNgayKhoiChieu, BorderLayout.CENTER);
        ngayKCPanel.add(lblNgayKhoiChieuError, BorderLayout.SOUTH);
        builder.add(ngayKCPanel, cc.xy(7, 3));
        
        builder.addLabel("Trạng thái:", cc.xy(5, 5)).setFont(LABEL_FONT);
        builder.add(cbTrangThai, cc.xy(7, 5));
        
        builder.addLabel("Kiểu phim:", cc.xy(5, 7)).setFont(LABEL_FONT);
        builder.add(cbKieuPhim, cc.xy(7, 7));
        
        // Cột 3: Nước sản xuất, Mô tả, Poster
        builder.addLabel("Nước sản xuất:", cc.xy(9, 1)).setFont(LABEL_FONT);
        JPanel nuocSXPanel = new JPanel(new BorderLayout());
        nuocSXPanel.setOpaque(false);
        nuocSXPanel.add(cbNuocSanXuat, BorderLayout.CENTER);
        nuocSXPanel.add(lblNuocSanXuatError, BorderLayout.SOUTH);
        builder.add(nuocSXPanel, cc.xy(11, 1));
        
        builder.addLabel("Mô tả:", cc.xy(9, 3)).setFont(LABEL_FONT);
        builder.add(txtMoTa, cc.xy(11, 3));
        
        builder.addLabel("Poster:", cc.xy(9, 5)).setFont(LABEL_FONT);
        
        JPanel posterPanel = new RoundedPanel(10);
        posterPanel.setBackground(Color.WHITE);
        posterPanel.setLayout(new BorderLayout(0, 5));
        posterPanel.add(lblPoster, BorderLayout.CENTER);
        posterPanel.add(btnChonAnh, BorderLayout.SOUTH);
        builder.add(posterPanel, cc.xywh(11, 5, 1, 3));
        
        return formPanel;
    }

    private RoundedPanel createTablePanel() {
        RoundedPanel tablePanel = new RoundedPanel(15);
        tablePanel.setBackground(new Color(250, 250, 250));
        tablePanel.setLayout(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Search Panel
        FormLayout searchLayout = new FormLayout(
            "pref, 8dlu, 250dlu:grow", 
            "p");
        PanelBuilder searchBuilder = new PanelBuilder(searchLayout);
        searchBuilder.border(Borders.DIALOG);
        
        RoundedPanel searchPanel = new RoundedPanel(10);
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setLayout(new BorderLayout());
        
        JLabel lblSearch = new JLabel("Tìm kiếm:", SwingConstants.RIGHT);
        lblSearch.setFont(LABEL_FONT);
        searchBuilder.add(lblSearch, CC.xy(1, 1));
        searchBuilder.add(txtSearch, CC.xy(3, 1));
        
        searchPanel.add(searchBuilder.getPanel(), BorderLayout.CENTER);
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

        return tablePanel;
    }

    private RoundedPanel createButtonPanel() {
        RoundedPanel buttonPanel = new RoundedPanel(15);
        buttonPanel.setBackground(new Color(250, 250, 250));
        
        FormLayout buttonLayout = new FormLayout(
            "center:pref:grow", 
            "p");
        PanelBuilder buttonBuilder = new PanelBuilder(buttonLayout, buttonPanel);
        buttonBuilder.border(Borders.DIALOG);
        
        JPanel buttonsFlow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonsFlow.setOpaque(false);
        buttonsFlow.add(btnThem);
        buttonsFlow.add(btnSua);
        buttonsFlow.add(btnXoa);
        buttonsFlow.add(btnClear);
        
        buttonBuilder.add(buttonsFlow, CC.xy(1, 1));
        
        return buttonPanel;
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(LABEL_FONT);
        comboBox.setPreferredSize(new Dimension(250, 30));
        comboBox.setBackground(Color.WHITE);
    }

    private UnderlineTextField createStyledUnderlineTextField() {
        UnderlineTextField field = new UnderlineTextField(20);
        field.setFont(LABEL_FONT);
        field.setUnderlineColor(new Color(200, 200, 200));
        field.setFocusColor(PRIMARY_COLOR);
        field.setErrorColor(new Color(220, 53, 69));
        field.setReadonlyColor(new Color(180, 180, 180));
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
            txtTenPhim.setError(lblTenPhimError.isVisible());
            updateButtonState();
        }));

        txtThoiLuong.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            ValidationUtils.validateDurationField(txtThoiLuong, lblThoiLuongError, messages);
            txtThoiLuong.setError(lblThoiLuongError.isVisible());
            updateButtonState();
        }));

        txtNgayKhoiChieu.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            ValidationUtils.validateStartDateField(
                txtMaPhim,
                txtNgayKhoiChieu, 
                lblNgayKhoiChieuError, 
                messages,
                new VeRepository(dbConnection),
                new SuatChieuRepository(dbConnection)
            );
            txtNgayKhoiChieu.setError(lblNgayKhoiChieuError.isVisible());
            updateButtonState();
        }));

        cbNuocSanXuat.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            ValidationUtils.validateCountryField(cbNuocSanXuat, lblNuocSanXuatError, messages);
            cbNuocSanXuat.setError(lblNuocSanXuatError.isVisible());
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
               !cbNuocSanXuat.getText().trim().isEmpty();
    }

    private void setupButtonActions() {
        btnThem.addActionListener(_ -> {
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

        btnSua.addActionListener(_ -> {
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

        btnXoa.addActionListener(_ -> {
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

        btnClear.addActionListener(_ -> {
            clearForm();
            updateButtonState();
        });
    }

    public void clearForm() {
        txtMaPhim.setText("");
        txtTenPhim.setText("");
        txtThoiLuong.setText("");
        txtNgayKhoiChieu.setText("");
        cbNuocSanXuat.setSelectedIndex(-1);
        txtMoTa.setText("");
        txtDaoDien.setText("");
        cbTenTheLoai.setSelectedIndex(-1);
        cbTrangThai.setSelectedIndex(-1);
        cbKieuPhim.setSelectedIndex(-1);
        lblPoster.setIcon(null);
        lblPoster.setText("Không có ảnh");
        selectedPosterPath = null;

        txtTenPhim.setError(false);
        txtThoiLuong.setError(false);
        txtNgayKhoiChieu.setError(false);

        ValidationUtils.hideError(lblTenPhimError);
        ValidationUtils.hideError(lblThoiLuongError);
        ValidationUtils.hideError(lblNgayKhoiChieuError);
        ValidationUtils.hideError(lblNuocSanXuatError);

        table.clearSelection();
    }

    // Getters
    public UnderlineTextField getTxtMaPhim() { return txtMaPhim; }
    public UnderlineTextField getTxtTenPhim() { return txtTenPhim; }
    public UnderlineTextField getTxtThoiLuong() { return txtThoiLuong; }
    public UnderlineTextField getTxtNgayKhoiChieu() { return txtNgayKhoiChieu; }
    public CountryComboBox getCbNuocSanXuat() { return cbNuocSanXuat; }
    public UnderlineTextField getTxtMoTa() { return txtMoTa; }
    public UnderlineTextField getTxtDaoDien() { return txtDaoDien; }
    public UnderlineTextField getSearchField() { return txtSearch; }
    public String getSearchText() { return txtSearch.getText().trim(); }
    public JComboBox<String> getCbTenTheLoai() { return cbTenTheLoai; }
    public JComboBox<String> getCbTrangthai() { return cbTrangThai; }
    public JComboBox<String> getCbKieuPhim() { return cbKieuPhim; }
    public JLabel getPosterLabel() { return lblPoster; }
    public StyledButton getBtnThem() { return btnThem; }
    public StyledButton getBtnSua() { return btnSua; }
    public StyledButton getBtnXoa() { return btnXoa; }
    public StyledButton getBtnClear() { return btnClear; }
    public JTable getTable() { return table; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public DatabaseConnection getDatabaseConnection() { return dbConnection; }
    public String getSelectedPosterPath() { return selectedPosterPath; }
    public void setSelectedPosterPath(String path) { this.selectedPosterPath = path; }
    public void clearSelectedPosterPath() { this.selectedPosterPath = null; }
}