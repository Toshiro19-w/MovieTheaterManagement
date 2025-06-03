package com.cinema.views.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import com.cinema.components.CountryComboBox;
import com.cinema.components.ModernUIApplier;
import com.cinema.components.MultiSelectComboBox;
import com.cinema.components.UIConstants;
import com.cinema.components.UnderlineTextField;
import com.cinema.controllers.PhimController;
import com.cinema.models.dto.CustomPaginationPanel;
import com.cinema.models.repositories.SuatChieuRepository;
import com.cinema.models.repositories.VeRepository;
import com.cinema.services.PhimService;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.SimpleDocumentListener;
import com.cinema.utils.TimeFormatter;
import com.cinema.utils.ValidationUtils;

public class PhimView extends JPanel {
    private UnderlineTextField txtMaPhim, txtTenPhim, txtThoiLuong, txtNgayKhoiChieu, txtMoTa, txtDaoDien, txtSearch, txtTrangThai;
    private CountryComboBox cbNuocSanXuat;
    private MultiSelectComboBox cbTenTheLoai;
    private JComboBox<String> cbKieuPhim;
    private JLabel lblPoster, lblTenPhimError, lblThoiLuongError, lblNgayKhoiChieuError, lblNuocSanXuatError;
    private JButton btnThem, btnSua, btnXoa, btnClear, btnChonAnh;
    private DefaultTableModel tableModel;
    private JTable table;
    private TableRowSorter<DefaultTableModel> tableSorter;
    private DatabaseConnection dbConnection;
    private String selectedPosterPath;
    private ResourceBundle messages;
    private PhimService phimService;
    private PhimController controller;
    private JButton btnQuanLyTheLoai;
    private TheLoaiSidebar theLoaiSidebar;
    private JPanel sidebarContainer;
    private boolean isSidebarVisible = false;

    // Sử dụng màu sắc từ ModernUIComponents
    private static final Color ROW_ALTERNATE_COLOR = new Color(240, 240, 240);

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
        setBackground(UIConstants.BACKGROUND_COLOR);
        setPreferredSize(new Dimension(1024, 900));

        // Tạo JScrollPane để cho phép cuộn khi nội dung quá dài
        JScrollPane mainScrollPane = new JScrollPane();
        mainScrollPane.setBorder(BorderFactory.createEmptyBorder());
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // Content Panel chính với hiệu ứng đổ bóng
        JPanel contentPanel = ModernUIApplier.createModernPanel();
        contentPanel.setBackground(UIConstants.CARD_BACKGROUND);
        contentPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        
        // Tiêu đề với icon phim
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = ModernUIApplier.createModernHeaderLabel("QUẢN LÝ PHIM");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(UIConstants.PRIMARY_COLOR);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        titlePanel.add(titleLabel);
        
        contentPanel.add(titlePanel);
        contentPanel.add(Box.createVerticalStrut(1));

        // Khởi tạo các thành phần
        initializeComponents();

        // Panel form nhập liệu
        JPanel formWrapper = createFormPanel();
        formWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(formWrapper);
        contentPanel.add(Box.createVerticalStrut(1));
        
        // Panel chứa các nút chức năng
        JPanel buttonPanel = createButtonPanel();
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(buttonPanel);
        contentPanel.add(Box.createVerticalStrut(1));

        // Panel hiển thị bảng dữ liệu
        JPanel tablePanel = createTablePanel();
        tablePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(tablePanel);

        // Thêm contentPanel vào ScrollPane
        mainScrollPane.setViewportView(contentPanel);
        add(mainScrollPane, BorderLayout.CENTER);

        // Thêm các listener
        addValidationListeners();
        setupButtonActions();
        
        // Thêm listener cho bảng để hiển thị chi tiết khi click
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                try {
                    controller.hienThiChiTietPhim(table.getSelectedRow());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                            "Lỗi khi hiển thị chi tiết phim: " + ex.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void initializeComponents() {
        // Text fields với thiết kế hiện đại
        txtMaPhim = createStyledUnderlineTextField();
        txtMaPhim.setEditable(false);
        txtMaPhim.setPlaceholder("Mã phim tự động");
        txtMaPhim.setBackground(new Color(245, 245, 245));
        
        txtTenPhim = createStyledUnderlineTextField();
        txtTenPhim.setPlaceholder("Nhập tên phim");
        txtTenPhim.setToolTipText("Nhập tên phim (không trùng lặp)");
        
        txtThoiLuong = createStyledUnderlineTextField();
        txtThoiLuong.setPlaceholder("Nhập thời lượng (phút)");
        txtThoiLuong.setToolTipText("Nhập thời lượng phim (số dương)");
        
        txtNgayKhoiChieu = createStyledUnderlineTextField();
        txtNgayKhoiChieu.setPlaceholder("dd/MM/yyyy");
        txtNgayKhoiChieu.setToolTipText("Nhập ngày khởi chiếu (dd/MM/yyyy, từ hôm nay trở đi)");
        
        // ComboBox quốc gia
        cbNuocSanXuat = new CountryComboBox();
        cbNuocSanXuat.setFont(UIConstants.BODY_FONT);
        cbNuocSanXuat.setPreferredSize(new Dimension(250, 35));
        
        txtMoTa = createStyledUnderlineTextField();
        txtMoTa.setPlaceholder("Nhập mô tả phim");
        
        txtDaoDien = createStyledUnderlineTextField();
        txtDaoDien.setPlaceholder("Nhập tên đạo diễn");
        
        // Trường tìm kiếm với icon
        txtSearch = createStyledUnderlineTextField();
        txtSearch.setPlaceholder("Tìm kiếm phim...");
        txtSearch.setToolTipText("Tìm kiếm theo tên phim");
        txtSearch.setPreferredSize(new Dimension(250, 35));

        // Combo boxes
        cbTenTheLoai = new MultiSelectComboBox();
        cbTenTheLoai.setPreferredSize(new Dimension(250, 35));
        cbKieuPhim = new JComboBox<>();
        cbKieuPhim.setPreferredSize(new Dimension(250, 35));
        styleMultiComboBox(cbTenTheLoai);
        styleComboBox(cbKieuPhim);
        
        // Trạng thái TextField
        txtTrangThai = createStyledUnderlineTextField();
        txtTrangThai.setEditable(false);
        txtTrangThai.setPlaceholder("Trạng thái phim");
        txtTrangThai.setBackground(new Color(245, 245, 245));

        // Poster label với thiết kế hiện đại
        lblPoster = new JLabel("Không có ảnh", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Vẽ nền với góc bo tròn
                g2d.setColor(new Color(245, 245, 245));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                super.paintComponent(g2d);
                g2d.dispose();
            }
        };
        // Sử dụng kích thước tỷ lệ hợp lý hơn để tránh làm giãn form
        lblPoster.setPreferredSize(new Dimension(100, 150));
        lblPoster.setBorder(BorderFactory.createEmptyBorder());
        lblPoster.setOpaque(false);
        
        // Nút chọn ảnh với thiết kế hiện đại
        btnChonAnh = ModernUIApplier.createModernButton("Chọn ảnh", UIConstants.PRIMARY_COLOR, Color.WHITE);
        btnChonAnh.setPreferredSize(new Dimension(150, 35));
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

        // Action buttons với thiết kế hiện đại
        btnThem = ModernUIApplier.createModernButton("Thêm", UIConstants.PRIMARY_COLOR, Color.WHITE);
        btnSua = ModernUIApplier.createModernButton("Sửa", UIConstants.PRIMARY_COLOR, Color.WHITE);
        btnXoa = ModernUIApplier.createModernButton("Xóa", UIConstants.ERROR_COLOR, Color.WHITE);
        btnClear = ModernUIApplier.createModernButton("Làm mới", UIConstants.SECONDARY_COLOR, Color.WHITE);
        btnQuanLyTheLoai = ModernUIApplier.createModernButton("Quản lý thể loại", UIConstants.SECONDARY_COLOR, Color.WHITE);
    }

    private JPanel createFormPanel() {
        // Panel chính
        JPanel formPanel = ModernUIApplier.createModernPanel();
        formPanel.setBackground(UIConstants.CARD_BACKGROUND);
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        // Sử dụng GridLayout đơn giản hơn với 3 cột
        formPanel.setLayout(new GridLayout(1, 3, 20, 0));
        
        // Cột 1: Thông tin cơ bản
        JPanel col1 = new JPanel();
        col1.setOpaque(false);
        col1.setLayout(new BoxLayout(col1, BoxLayout.Y_AXIS));
        addFormFieldSimple(col1, "Mã phim:", txtMaPhim, null);
        addFormFieldSimple(col1, "Tên phim:", txtTenPhim, lblTenPhimError);
        addFormFieldSimple(col1, "Thể loại:", cbTenTheLoai, null);
        addFormFieldSimple(col1, "Đạo diễn:", txtDaoDien, null);
        addFormFieldSimple(col1, "Thời lượng (phút):", txtThoiLuong, lblThoiLuongError);
        
        // Cột 2: Thông tin bổ sung
        JPanel col2 = new JPanel();
        col2.setOpaque(false);
        col2.setLayout(new BoxLayout(col2, BoxLayout.Y_AXIS));
        addFormFieldSimple(col2, "Ngày khởi chiếu:", txtNgayKhoiChieu, lblNgayKhoiChieuError);
        addFormFieldSimple(col2, "Trạng thái:", txtTrangThai, null);
        addFormFieldSimple(col2, "Kiểu phim:", cbKieuPhim, null);
        addFormFieldSimple(col2, "Nước sản xuất:", cbNuocSanXuat, lblNuocSanXuatError);
        addFormFieldSimple(col2, "Mô tả:", txtMoTa, null);
        
        // Cột 3: Poster
        JPanel col3 = new JPanel();
        col3.setOpaque(false);
        col3.setLayout(new BoxLayout(col3, BoxLayout.Y_AXIS));
        
        JLabel lblPosterTitle = new JLabel("Poster:");
        lblPosterTitle.setFont(UIConstants.LABEL_FONT);
        lblPosterTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        col3.add(lblPosterTitle);
        col3.add(Box.createVerticalStrut(10));
        
        // Đặt kích thước cố định cho lblPoster
        lblPoster.setPreferredSize(new Dimension(100, 150));
        lblPoster.setMaximumSize(new Dimension(100, 150));
        lblPoster.setAlignmentX(Component.CENTER_ALIGNMENT);
        col3.add(lblPoster);
        col3.add(Box.createVerticalStrut(10));
        
        btnChonAnh.setAlignmentX(Component.CENTER_ALIGNMENT);
        col3.add(btnChonAnh);
        
        // Thêm các cột vào panel chính
        formPanel.add(col1);
        formPanel.add(col2);
        formPanel.add(col3);
        
        // Đặt trong panel wrapper để căn giữa
        JPanel wrapperPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrapperPanel.setOpaque(false);
        wrapperPanel.add(formPanel);
        
        return wrapperPanel;
    }

    // Thêm style cho JComboBox
    private void styleComboBox(JComboBox<String> comboBox) {
        ModernUIApplier.applyModernComboBoxStyle(comboBox);
        comboBox.setPreferredSize(new Dimension(120, 35));
    }

    private void styleMultiComboBox(MultiSelectComboBox comboBox) {
        ModernUIApplier.applyModernComboBoxStyle(comboBox);
        comboBox.setPreferredSize(new Dimension(120, 35));
    }

    // Helper method đơn giản hơn để thêm field với label cùng hàng và error message đầy đủ
    private void addFormFieldSimple(JPanel panel, String labelText, Component field, Component errorLabel) {
        JPanel fieldPanel = new JPanel();
        fieldPanel.setOpaque(false);
        fieldPanel.setLayout(new BorderLayout());
        
        // Panel chứa label và field cùng hàng
        JPanel inputRow = new JPanel(new BorderLayout(10, 0));
        inputRow.setOpaque(false);
        
        JLabel label = new JLabel(labelText);
        label.setFont(UIConstants.LABEL_FONT);
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        label.setPreferredSize(new Dimension(120, 30));
        inputRow.add(label, BorderLayout.WEST);
        
        // Đặt field vào center
        inputRow.add(field, BorderLayout.CENTER);
        
        fieldPanel.add(inputRow, BorderLayout.NORTH);
        
        // Thêm error label vào panel riêng bên dưới với khả năng xuống dòng
        if (errorLabel != null) {
            JPanel errorPanel = new JPanel(new BorderLayout());
            errorPanel.setOpaque(false);
            
            // Thêm padding bên trái để căn chỉnh với field
            errorPanel.setBorder(BorderFactory.createEmptyBorder(2, 130, 0, 0));
            
            // Đảm bảo error label có thể xuống dòng
            if (errorLabel instanceof JLabel) {
                JLabel errLabel = (JLabel) errorLabel;
                errLabel.setPreferredSize(new Dimension(150, 30));
                // Cho phép text xuống dòng
                errLabel.setText("<html><div style='width:150px;'>" + errLabel.getText() + "</div></html>");
            }
            
            errorPanel.add(errorLabel, BorderLayout.CENTER);
            fieldPanel.add(errorPanel, BorderLayout.CENTER);
        }
        
        panel.add(fieldPanel);
        panel.add(Box.createVerticalStrut(10));
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = ModernUIApplier.createModernPanel();
        tablePanel.setBackground(UIConstants.BACKGROUND_COLOR);
        tablePanel.setLayout(new BorderLayout(0, 2)); // Giảm khoảng cách dọc xuống 5px
        tablePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Giảm padding
        tablePanel.setMaximumSize(new Dimension(1250, 2000));
        
        // Panel tiêu đề và tìm kiếm
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setOpaque(false);
        
        // Tiêu đề bảng
        JLabel tableTitle = new JLabel("Danh sách phim");
        tableTitle.setFont(UIConstants.SUBHEADER_FONT);
        tableTitle.setForeground(UIConstants.PRIMARY_COLOR);
        headerPanel.add(tableTitle, BorderLayout.WEST);
        
        // Panel tìm kiếm với thiết kế hiện đại
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setOpaque(false);
        
        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(UIConstants.BODY_FONT);
        searchPanel.add(lblSearch);
        
        // Tạo panel chứa txtSearch với icon tìm kiếm
        JPanel searchFieldPanel = new JPanel(new BorderLayout());
        searchFieldPanel.setOpaque(false);
        searchFieldPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        searchFieldPanel.add(txtSearch, BorderLayout.CENTER);
        
        // Thêm listener cho tìm kiếm sử dụng TableRowSorter
        txtSearch.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            String text = txtSearch.getText().trim();
            if (text.isEmpty()) {
                tableSorter.setRowFilter(null);
            } else {
                // Tìm kiếm không phân biệt chữ hoa/thường trong cột tên phim (cột 1)
                tableSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 1));
            }
        }));
        
        searchPanel.add(searchFieldPanel);
        headerPanel.add(searchPanel, BorderLayout.EAST);
        
        tablePanel.add(headerPanel, BorderLayout.NORTH);

        // Tạo bảng với thiết kế hiện đại
        tableModel = new DefaultTableModel(
                new Object[]{"Poster", "Tên phim", "Thể loại", "Thời lượng", "Ngày khởi chiếu", "Trạng thái"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return ImageIcon.class;
                return super.getColumnClass(columnIndex);
            }
        };

        table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                
                // Hiệu ứng hover và chọn dòng
                if (isRowSelected(row)) {
                    c.setBackground(new Color(UIConstants.PRIMARY_COLOR.getRed(), 
                                             UIConstants.PRIMARY_COLOR.getGreen(), 
                                             UIConstants.PRIMARY_COLOR.getBlue(), 40));
                    c.setForeground(UIConstants.PRIMARY_COLOR);
                } else if (row % 2 == 0) {
                    c.setBackground(UIConstants.CARD_BACKGROUND);
                    c.setForeground(UIConstants.TEXT_COLOR);
                } else {
                    c.setBackground(ROW_ALTERNATE_COLOR);
                    c.setForeground(UIConstants.TEXT_COLOR);
                }
                
                // Thêm padding cho các ô
                if (c instanceof JLabel) {
                    ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                }
                
                return c;
            }
        };
        
        // Áp dụng style hiện đại cho bảng
        ModernUIApplier.applyModernTableStyle(table);
        table.setRowHeight(120); // Giảm chiều cao dòng để hiển thị nhiều phim hơn
        table.setIntercellSpacing(new Dimension(10, 5)); // Khoảng cách giữa các ô
        table.setShowGrid(false); // Ẩn lưới để giao diện sạch hơn
        table.setFillsViewportHeight(true);
        
        // Thiết lập TableRowSorter cho bảng
        tableSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(tableSorter);
        
        // Thiết lập kích thước các cột
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(0).setMaxWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        
        // Renderer cho cột trạng thái với thiết kế badge hiện đại
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = new JLabel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2d = (Graphics2D) g.create();
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        
                        // Vẽ nền với góc bo tròn
                        g2d.setColor(getBackground());
                        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                        
                        super.paintComponent(g2d);
                        g2d.dispose();
                    }
                };
                
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setFont(UIConstants.SMALL_FONT.deriveFont(Font.BOLD));
                label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                
                if ("active".equals(value)) {
                    label.setText("ĐANG CHIẾU");
                    label.setBackground(new Color(46, 204, 113, 40)); // Màu xanh lá
                    label.setForeground(new Color(46, 204, 113));
                } else if ("deleted".equals(value)) {
                    label.setText("KẾT THÚC");
                    label.setBackground(new Color(231, 76, 60, 40)); // Màu đỏ đậm hơn
                    label.setForeground(new Color(231, 76, 60));
                } else if ("upcoming".equals(value)) {
                    label.setText("SẮP CHIẾU");
                    label.setBackground(new Color(52, 152, 219, 40)); // Màu xanh dương
                    label.setForeground(new Color(52, 152, 219));
                } else {
                    label.setText(value != null ? value.toString() : "");
                }
                
                if (isSelected) {
                    label.setBackground(new Color(UIConstants.PRIMARY_COLOR.getRed(), 
                                                UIConstants.PRIMARY_COLOR.getGreen(), 
                                                UIConstants.PRIMARY_COLOR.getBlue(), 40));
                }
                
                // Đặt opaque thành true để hiển thị màu nền
                label.setOpaque(true);
                return label;
            }
        });

        // Thêm renderer cho cột thời lượng
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof Integer) {
                    label.setText(TimeFormatter.formatMinutesToHoursAndMinutes((Integer) value));
                }
                return label;
            }
        });
        
        // Renderer cho cột trạng thái với thiết kế badge hiện đại
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = new JLabel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2d = (Graphics2D) g.create();
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        
                        // Vẽ nền với góc bo tròn
                        g2d.setColor(getBackground());
                        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                        
                        super.paintComponent(g2d);
                        g2d.dispose();
                    }
                };
                
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setFont(UIConstants.SMALL_FONT.deriveFont(Font.BOLD));
                label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                
                if ("active".equals(value)) {
                    label.setText("ĐANG CHIẾU");
                    label.setBackground(new Color(46, 204, 113, 40)); // Màu xanh lá
                    label.setForeground(new Color(46, 204, 113));
                } else if ("deleted".equals(value)) {
                    label.setText("KẾT THÚC");
                    label.setBackground(new Color(231, 76, 60, 40)); // Màu đỏ đậm hơn
                    label.setForeground(new Color(231, 76, 60));
                } else if ("upcoming".equals(value)) {
                    label.setText("SẮP CHIẾU");
                    label.setBackground(new Color(52, 152, 219, 40)); // Màu xanh dương
                    label.setForeground(new Color(52, 152, 219));
                } else {
                    label.setText(value != null ? value.toString() : "");
                }
                
                if (isSelected) {
                    label.setBackground(new Color(UIConstants.PRIMARY_COLOR.getRed(), 
                                                UIConstants.PRIMARY_COLOR.getGreen(), 
                                                UIConstants.PRIMARY_COLOR.getBlue(), 40));
                }
                
                label.setOpaque(true);
                return label;
            }
        });

        // Tạo container cho bảng với hiệu ứng đổ bóng
        JPanel tableContainer = ModernUIApplier.createModernPanel();
        tableContainer.setLayout(new BorderLayout());
        tableContainer.setBackground(UIConstants.CARD_BACKGROUND);
        
        // Sử dụng JScrollPane với thanh cuộn hiện đại
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(UIConstants.CARD_BACKGROUND);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // Thiết lập kích thước cho scrollPane
        scrollPane.setPreferredSize(new Dimension(1000, 450));
        
        tableContainer.add(scrollPane, BorderLayout.CENTER);
        tablePanel.add(tableContainer, BorderLayout.CENTER);
        
        // Thêm phân trang với thiết kế hiện đại
        CustomPaginationPanel paginationPanel = new CustomPaginationPanel();
        paginationPanel.setBackground(UIConstants.CARD_BACKGROUND);
        paginationPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        paginationPanel.setPageChangeListener(page -> {
            try {
                controller.loadPhimPaginated(page, 10);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Lỗi khi tải dữ liệu phim: " + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        
        tablePanel.add(paginationPanel, BorderLayout.SOUTH);

        return tablePanel;
    }

    private JPanel createButtonPanel() {
        // Tạo panel với hiệu ứng đổ bóng
        JPanel buttonPanel = ModernUIApplier.createModernPanel();
        buttonPanel.setBackground(UIConstants.CARD_BACKGROUND);
        
        // Đặt kích thước cố định
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Giảm padding
        
        // Tùy chỉnh các nút
        btnThem.setPreferredSize(new Dimension(120, 40));
        btnSua.setPreferredSize(new Dimension(120, 40));
        btnXoa.setPreferredSize(new Dimension(120, 40));
        btnClear.setPreferredSize(new Dimension(120, 40));
        btnQuanLyTheLoai.setPreferredSize(new Dimension(150, 40));
        
        // Thêm các nút trực tiếp vào panel
        buttonPanel.add(btnThem);
        buttonPanel.add(btnSua);
        buttonPanel.add(btnXoa);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnQuanLyTheLoai);
        
        // Đặt border nhỏ hơn để giảm khoảng cách
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        
        return buttonPanel;
    }

    private UnderlineTextField createStyledUnderlineTextField() {
        UnderlineTextField field = new UnderlineTextField(20);
        field.setFont(UIConstants.LABEL_FONT);
        field.setUnderlineColor(new Color(200, 200, 200));
        field.setFocusColor(UIConstants.PRIMARY_COLOR);
        field.setErrorColor(UIConstants.ERROR_COLOR);
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
                Image scaledImage = imageIcon.getImage().getScaledInstance(150, 450, Image.SCALE_SMOOTH);
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

        btnQuanLyTheLoai.addActionListener(_ -> toggleTheLoaiSidebar());
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
        txtTrangThai.setText("");
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

    // TheLoaiSidebar
    private void toggleTheLoaiSidebar() {
        if (sidebarContainer == null) {
            initSidebarContainer();
        }
        
        if (isSidebarVisible) {
            hideSidebar();
        } else {
            showSidebar();
        }
    }

    private void initSidebarContainer() {
        sidebarContainer = new JPanel(new BorderLayout());
        sidebarContainer.setPreferredSize(new Dimension(350, getHeight()));
        sidebarContainer.setVisible(false);
        
        theLoaiSidebar = new TheLoaiSidebar(dbConnection, e -> hideSidebar());
        sidebarContainer.add(theLoaiSidebar, BorderLayout.CENTER);
        
        add(sidebarContainer, BorderLayout.EAST);
        revalidate();
    }

    private void showSidebar() {
        if (theLoaiSidebar != null) {
            theLoaiSidebar.refreshData();
        }
        sidebarContainer.setVisible(true);
        isSidebarVisible = true;
        revalidate();
        repaint();
    }

    private void hideSidebar() {
        sidebarContainer.setVisible(false);
        isSidebarVisible = false;
        revalidate();
        repaint();
        
        // Refresh thể loại list in combobox after sidebar is closed
        try {
            controller.loadTheLoaiList();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Lỗi khi tải danh sách thể loại: " + e.getMessage(), 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
        }
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
    public MultiSelectComboBox getCbTenTheLoai() { return cbTenTheLoai; }
    public UnderlineTextField getTxtTrangThai() { return txtTrangThai; }
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