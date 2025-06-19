package com.cinema.views.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.cinema.components.UnderlineTextField;
import com.cinema.controllers.VeController;
import com.cinema.models.dto.CustomPaginationPanel;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.FormatUtils;
import com.cinema.utils.SnackbarUtil;

public class VeView extends JPanel {
    private DatabaseConnection databaseConnection;
    private JLabel txtMaVe;
    private JTextField txtSoGhe, txtNgayDat, txtGiaVeGoc, txtGiaVeSauGiam, txtTienGiam;
    private JTextField txtTenKhachHang, txtSoDienThoai, txtEmail, txtDiemTichLuy;
    private JComboBox<String> cbTrangThai, cbTenPhong, cbNgayGioChieu, cbTenPhim, cbKhuyenMai;
    private JTable tableVe;
    private DefaultTableModel tableVeModel;
    private JButton btnThem, btnSua, btnXoa, btnClear, btnRefresh, btnSuaGiaVe;
    private JTextField searchField;
    private JLabel soGheErrorLabel, tenPhongErrorLabel,
            ngayGioChieuErrorLabel, tenPhimErrorLabel,
            khuyenMaiErrorLabel, lblMessage;
    private CustomPaginationPanel paginationPanel;

    public VeView() throws SQLException {
        initializeDatabase();
        initializeUI();
        new VeController(this);
    }
    
    public javax.swing.JFrame getParentFrame() {
        return (javax.swing.JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
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
        setBackground(new Color(245, 247, 250));

        JPanel topPanel = createTopPanel();
        JPanel centerPanel = createCenterPanel();

        topPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(0, 0, 5, 0),
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1)
        ));

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBackground(new Color(245, 247, 250));

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        searchPanel.setBackground(new Color(245, 247, 250));
        
        JLabel searchLabel = new JLabel("Tìm kiếm:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        searchLabel.setForeground(new Color(50, 50, 50));
        
        searchField = new UnderlineTextField(15);
        searchField.setPreferredSize(new Dimension(200, 30));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ((UnderlineTextField)searchField).setPlaceholder("Nhập mã vé, số ghế, phim...");
        
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);

        // Info Panel
        JPanel infoPanel = new JPanel(new BorderLayout(5, 5));
        infoPanel.setBackground(new Color(245, 247, 250));
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(150, 150, 150), 1),
            "THÔNG TIN VÉ",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 13),
            new Color(50, 50, 50)
        );
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            titledBorder,
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        // Initialize components
        txtMaVe = new JLabel();
        txtMaVe.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbTrangThai = new JComboBox<>(new String[]{"BOOKED", "PAID", "CANCELLED", "PENDING"});
        cbTrangThai.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbTrangThai.setPreferredSize(new Dimension(150, 30));
        txtSoGhe = new UnderlineTextField(10);
        txtSoGhe.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        soGheErrorLabel = new JLabel("");
        soGheErrorLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        soGheErrorLabel.setForeground(new Color(200, 0, 0));
        txtNgayDat = new UnderlineTextField(10);
        txtNgayDat.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtNgayDat.setEditable(false);
        cbTenPhong = new JComboBox<>();
        cbTenPhong.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbTenPhong.setPreferredSize(new Dimension(150, 30));
        tenPhongErrorLabel = new JLabel("");
        tenPhongErrorLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        tenPhongErrorLabel.setForeground(new Color(200, 0, 0));
        cbNgayGioChieu = new JComboBox<>();
        cbNgayGioChieu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbNgayGioChieu.setPreferredSize(new Dimension(150, 30));
        ngayGioChieuErrorLabel = new JLabel("");
        ngayGioChieuErrorLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        ngayGioChieuErrorLabel.setForeground(new Color(200, 0, 0));
        cbTenPhim = new JComboBox<>();
        cbTenPhim.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbTenPhim.setPreferredSize(new Dimension(150, 30));
        tenPhimErrorLabel = new JLabel("");
        tenPhimErrorLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        tenPhimErrorLabel.setForeground(new Color(200, 0, 0));
        cbKhuyenMai = new JComboBox<>();
        cbKhuyenMai.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbKhuyenMai.setPreferredSize(new Dimension(150, 30));
        khuyenMaiErrorLabel = new JLabel("");
        khuyenMaiErrorLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        khuyenMaiErrorLabel.setForeground(new Color(200, 0, 0));
        txtGiaVeGoc = new UnderlineTextField(10);
        txtGiaVeGoc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtGiaVeGoc.setEditable(false);
        txtGiaVeSauGiam = new UnderlineTextField(10);
        txtGiaVeSauGiam.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtGiaVeSauGiam.setEditable(false);
        txtTienGiam = new UnderlineTextField(10);
        txtTienGiam.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtTienGiam.setEditable(false);
        
        // Khách hàng fields
        txtTenKhachHang = new UnderlineTextField(15);
        txtTenKhachHang.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtTenKhachHang.setEditable(false);
        txtSoDienThoai = new UnderlineTextField(15);
        txtSoDienThoai.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSoDienThoai.setEditable(false);
        txtEmail = new UnderlineTextField(15);
        txtEmail.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtEmail.setEditable(false);
        txtDiemTichLuy = new UnderlineTextField(15);
        txtDiemTichLuy.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtDiemTichLuy.setEditable(false);

        // Ticket Details Panel
        JPanel ticketDetailsPanel = new JPanel(new GridBagLayout());
        stylePanel(ticketDetailsPanel, "Chi tiết vé");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        addField(ticketDetailsPanel, gbc, 0, 0, "Mã vé:", txtMaVe);
        addField(ticketDetailsPanel, gbc, 0, 1, "Trạng thái:", cbTrangThai);
        addField(ticketDetailsPanel, gbc, 0, 2, "Số ghế:", txtSoGhe);
        gbc.gridx = 1; gbc.gridy = 5; ticketDetailsPanel.add(soGheErrorLabel, gbc);
        addField(ticketDetailsPanel, gbc, 0, 3, "Ngày đặt:", txtNgayDat);
        addField(ticketDetailsPanel, gbc, 2, 0, "Phòng chiếu:", cbTenPhong);
        gbc.gridx = 3; gbc.gridy = 1; ticketDetailsPanel.add(tenPhongErrorLabel, gbc);
        addField(ticketDetailsPanel, gbc, 2, 1, "Giá vé gốc:", txtGiaVeGoc);
        addField(ticketDetailsPanel, gbc, 2, 2, "Tiền giảm:", txtTienGiam);
        addField(ticketDetailsPanel, gbc, 2, 3, "Giá vé sau giảm:", txtGiaVeSauGiam);

        // Movie Details Panel
        JPanel movieDetailsPanel = new JPanel(new GridBagLayout());
        stylePanel(movieDetailsPanel, "Thông tin phim");
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        addField(movieDetailsPanel, gbc, 0, 0, "Tên phim:", cbTenPhim);
        gbc.gridx = 1; gbc.gridy = 1; movieDetailsPanel.add(tenPhimErrorLabel, gbc);
        addField(movieDetailsPanel, gbc, 0, 1, "Thời gian chiếu:", cbNgayGioChieu);
        gbc.gridx = 1; gbc.gridy = 3; movieDetailsPanel.add(ngayGioChieuErrorLabel, gbc);

        // Promotion Panel
        JPanel promotionPanel = new JPanel(new GridBagLayout());
        stylePanel(promotionPanel, "Khuyến mãi");
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        addField(promotionPanel, gbc, 0, 0, "Khuyến mãi:", cbKhuyenMai);
        gbc.gridx = 1; gbc.gridy = 1; promotionPanel.add(khuyenMaiErrorLabel, gbc);

        // Customer Info Panel
        JPanel customerInfoPanel = new JPanel(new GridBagLayout());
        stylePanel(customerInfoPanel, "Thông tin khách hàng");
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        addField(customerInfoPanel, gbc, 0, 0, "Tên khách hàng:", txtTenKhachHang);
        addField(customerInfoPanel, gbc, 0, 1, "Số điện thoại:", txtSoDienThoai);
        addField(customerInfoPanel, gbc, 2, 0, "Email:", txtEmail);
        addField(customerInfoPanel, gbc, 2, 1, "Điểm tích lũy:", txtDiemTichLuy);

        // Right Panel
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBackground(new Color(245, 247, 250));
        rightPanel.add(movieDetailsPanel, BorderLayout.NORTH);
        rightPanel.add(promotionPanel, BorderLayout.CENTER);
        rightPanel.add(customerInfoPanel, BorderLayout.SOUTH);

        infoPanel.add(ticketDetailsPanel, BorderLayout.CENTER);
        infoPanel.add(rightPanel, BorderLayout.EAST);

        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(infoPanel, BorderLayout.CENTER);

        return topPanel;
    }

    private JPanel createCenterPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBackground(new Color(245, 247, 250));
        
        // Button Panel
        JPanel buttonPanel = createButtonPanel();
        
        // Table Panel
        JPanel tablePanel = createTablePanel(
            new String[]{"Mã vé", "Trạng thái", "Số ghế", "Giá gốc", "Tiền giảm", "Giá sau giảm", "Ngày đặt", "Phòng chiếu", "Thời gian chiếu", "Tên phim", "Khuyến mãi"},
            "DANH SÁCH VÉ"
        );
        tableVe = (JTable) ((JScrollPane) tablePanel.getComponent(0)).getViewport().getView();
        tableVeModel = (DefaultTableModel) tableVe.getModel();
        
        // Pagination Panel
        paginationPanel = new CustomPaginationPanel();
        JPanel paginationContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        paginationContainer.setBackground(new Color(245, 247, 250));
        paginationContainer.add(paginationPanel);
        
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        mainPanel.add(paginationContainer, BorderLayout.SOUTH);
        
        return mainPanel;
    }

    private JPanel createTablePanel(String[] columns, String title) {
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(230, 232, 235));
        table.getTableHeader().setForeground(new Color(50, 50, 50));
        
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
                
                if (isSelected) {
                    c.setBackground(new Color(200, 220, 240));
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 248));
                }
                
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(150, 150, 150)),
            title,
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 13),
            new Color(50, 50, 50)
        ));

        return new JPanel(new BorderLayout()) {{
            add(scrollPane, BorderLayout.CENTER);
        }};
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new BorderLayout(5, 5));
        buttonPanel.setBackground(new Color(245, 247, 250));
        
        JPanel leftButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        leftButtonPanel.setBackground(new Color(245, 247, 250));
        
        JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        rightButtonPanel.setBackground(new Color(245, 247, 250));
        
        btnThem = createButton("THÊM", new Color(40, 167, 69));
        btnSua = createButton("SỬA", new Color(0, 123, 255));
        btnXoa = createButton("XÓA", new Color(220, 53, 69));
        btnClear = createButton("LÀM MỚI", new Color(108, 117, 125));
        btnRefresh = createButton("CẬP NHẬT", new Color(255, 193, 7));
        btnSuaGiaVe = createButton("QUẢN LÝ GIÁ VÉ", new Color(0, 123, 255));

        leftButtonPanel.add(btnThem);
        leftButtonPanel.add(btnSua);
        leftButtonPanel.add(btnXoa);
        leftButtonPanel.add(btnClear);
        leftButtonPanel.add(btnRefresh);
        
        rightButtonPanel.add(btnSuaGiaVe);

        buttonPanel.add(leftButtonPanel, BorderLayout.WEST);
        buttonPanel.add(rightButtonPanel, BorderLayout.EAST);

        return buttonPanel;
    }

    private JButton createButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setPreferredSize(new Dimension(100, 35));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(150, 150, 150)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        return button;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int x, int y, String labelText, JComponent component) {
        gbc.gridx = x * 2; gbc.gridy = y * 2;
        JLabel label = new JLabel(labelText, SwingConstants.RIGHT);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(50, 50, 50));
        panel.add(label, gbc);

        gbc.gridx = x * 2 + 1;
        if (component instanceof JTextField) {
            ((JTextField) component).setPreferredSize(new Dimension(150, 30));
        }
        panel.add(component, gbc);
    }

    private void stylePanel(JPanel panel, String title) {
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(150, 150, 150), 1),
            title,
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 13),
            new Color(50, 50, 50)
        );
        panel.setBorder(BorderFactory.createCompoundBorder(
            titledBorder,
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        panel.setBackground(new Color(250, 250, 252));
    }

    // Getters
    public DatabaseConnection getDatabaseConnection() { return databaseConnection; }
    public JLabel getTxtMaVe() { return txtMaVe; }
    public JComboBox<String> getCbTrangThai() { return cbTrangThai; }
    public JTextField getTxtSoGhe() { return txtSoGhe; }
    public JTextField getTxtNgayDat() { return txtNgayDat; }
    public JComboBox<String> getCbTenPhong() { return cbTenPhong; }
    public JComboBox<String> getCbNgayGioChieu() { return cbNgayGioChieu; }
    public JComboBox<String> getCbTenPhim() { return cbTenPhim; }
    public JTextField getSearchField() { return searchField; }
    public JTable getTable() { return tableVe; }
    public DefaultTableModel getTableModel() { return tableVeModel; }
    public JButton getBtnThem() { return btnThem; }
    public JButton getBtnSua() { return btnSua; }
    public JButton getBtnXoa() { return btnXoa; }
    public JButton getBtnClear() { return btnClear; }
    public JButton getBtnRefresh() { return btnRefresh; }
    public JButton getBtnSuaGiaVe() { return btnSuaGiaVe; }
    public JComboBox<String> getCbKhuyenMai() { return cbKhuyenMai; }
    public JLabel getSoGheErrorLabel() { return soGheErrorLabel; }
    public JLabel getTenPhongErrorLabel() { return tenPhongErrorLabel; }
    public JLabel getNgayGioChieuErrorLabel() { return ngayGioChieuErrorLabel; }
    public JLabel getTenPhimErrorLabel() { return tenPhimErrorLabel; }
    public JLabel getKhuyenMaiErrorLabel() { return khuyenMaiErrorLabel; }
    public JTextField getTxtGiaVeGoc() { return txtGiaVeGoc; }
    public JTextField getTxtGiaVeSauGiam() { return txtGiaVeSauGiam; }
    public JTextField getTxtTienGiam() { return txtTienGiam; }
    public JTextField getTxtTenKhachHang() { return txtTenKhachHang; }
    public JTextField getTxtSoDienThoai() { return txtSoDienThoai; }
    public JTextField getTxtEmail() { return txtEmail; }
    public JTextField getTxtDiemTichLuy() { return txtDiemTichLuy; }
    public BigDecimal getGiaVeGoc() { return FormatUtils.parseCurrency(txtGiaVeGoc.getText()); }
    public BigDecimal getTienGiam() { return FormatUtils.parseCurrency(txtTienGiam.getText()); }
    public BigDecimal getGiaVeSauGiam() { return FormatUtils.parseCurrency(txtGiaVeSauGiam.getText()); }
    public CustomPaginationPanel getPaginationPanel() { return paginationPanel; }
    public JLabel getLblMessage() {
        return lblMessage;
    }

    public void clearForm() {
        txtMaVe.setText("");
        cbTrangThai.setSelectedIndex(0);
        txtSoGhe.setText("");
        txtNgayDat.setText("");
        cbKhuyenMai.setSelectedIndex(-1);
        txtGiaVeGoc.setText(FormatUtils.formatCurrency(BigDecimal.ZERO));
        txtGiaVeSauGiam.setText(FormatUtils.formatCurrency(BigDecimal.ZERO));
        txtTienGiam.setText(FormatUtils.formatCurrency(BigDecimal.ZERO));
        cbTenPhong.setSelectedIndex(-1);
        cbTenPhim.setSelectedIndex(-1);
        cbNgayGioChieu.setSelectedIndex(-1);
        soGheErrorLabel.setText("");
        tenPhongErrorLabel.setText("");
        ngayGioChieuErrorLabel.setText("");
        tenPhimErrorLabel.setText("");
        khuyenMaiErrorLabel.setText("");
        txtTenKhachHang.setText("");
        txtSoDienThoai.setText("");
        txtEmail.setText("");
        txtDiemTichLuy.setText("");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    public void showMessage(String message, boolean isSuccess) {
        SnackbarUtil.showSnackbar(this, message, isSuccess);
    }
}