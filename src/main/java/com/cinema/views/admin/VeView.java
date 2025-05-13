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
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.cinema.components.UnderlineTextField;
import com.cinema.controllers.VeController;
import com.cinema.utils.DatabaseConnection;

public class VeView extends JPanel {
    private DatabaseConnection databaseConnection;
    private JLabel txtMaVe;
    private JTextField txtGiaVe, txtSoGhe, txtTenPhong, txtTenPhim, txtNgayGioChieu, searchField, txtNgayDat, txtKhuyenMai;
    private JComboBox<String> cbTrangThai;
    private JTable tableVe, tableKhachHang;
    private DefaultTableModel tableVeModel, tableKhachHangModel;
    private JButton btnThem, btnSua, btnXoa, btnClear, btnRefresh;
    private TableRowSorter<DefaultTableModel> sorter;
    private JLabel soGheErrorLabel, giaVeErrorLabel, tenPhongErrorLabel, ngayGioChieuErrorLabel, tenPhimErrorLabel;
    private JComboBox<String> cbTenPhim;
    private JComboBox<String> cbTenPhong;
    private JComboBox<String> cbNgayGioChieu;

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
        setBackground(new Color(245, 247, 250));

        JPanel topPanel = createTopPanel();
        JSplitPane centerPanel = createCenterPanel();
        JPanel buttonPanel = createButtonPanel();

        topPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(0, 0, 5, 0),
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1)
        ));

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
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
        ((UnderlineTextField)searchField).setPlaceholder("Nhập vào từ khoá...");
        
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
        cbTrangThai = new JComboBox<>(new String[]{"booked", "paid", "cancelled"});
        cbTrangThai.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbTrangThai.setPreferredSize(new Dimension(150, 30));
        txtGiaVe = new UnderlineTextField(10);
        txtGiaVe.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        giaVeErrorLabel = new JLabel("");
        giaVeErrorLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        giaVeErrorLabel.setForeground(new Color(200, 0, 0));
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
        txtKhuyenMai = new UnderlineTextField(10);
        txtKhuyenMai.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtKhuyenMai.setEditable(false);

        // Ticket Details Panel
        JPanel ticketDetailsPanel = new JPanel(new GridBagLayout());
        stylePanel(ticketDetailsPanel, "Chi tiết vé");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        addField(ticketDetailsPanel, gbc, 0, 0, "Mã vé:", txtMaVe);
        addField(ticketDetailsPanel, gbc, 0, 1, "Trạng thái:", cbTrangThai);
        addField(ticketDetailsPanel, gbc, 0, 2, "Giá vé:", txtGiaVe);
        gbc.gridx = 1; gbc.gridy = 3; ticketDetailsPanel.add(giaVeErrorLabel, gbc);
        addField(ticketDetailsPanel, gbc, 2, 0, "Số ghế:", txtSoGhe);
        gbc.gridx = 3; gbc.gridy = 1; ticketDetailsPanel.add(soGheErrorLabel, gbc);
        addField(ticketDetailsPanel, gbc, 2, 1, "Ngày đặt:", txtNgayDat);
        addField(ticketDetailsPanel, gbc, 2, 2, "Phòng chiếu:", cbTenPhong);
        gbc.gridx = 3; gbc.gridy = 3; ticketDetailsPanel.add(tenPhongErrorLabel, gbc);

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
        gbc.gridx = 1; gbc.gridy = 2; movieDetailsPanel.add(ngayGioChieuErrorLabel, gbc);

        // Promotion Panel
        JPanel promotionPanel = new JPanel(new GridBagLayout());
        stylePanel(promotionPanel, "Khuyến mãi");
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        addField(promotionPanel, gbc, 0, 0, "Khuyến mãi:", txtKhuyenMai);

        // Right Panel
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBackground(new Color(245, 247, 250));
        rightPanel.add(movieDetailsPanel, BorderLayout.CENTER);
        rightPanel.add(promotionPanel, BorderLayout.SOUTH);

        infoPanel.add(ticketDetailsPanel, BorderLayout.CENTER);
        infoPanel.add(rightPanel, BorderLayout.EAST);

        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(infoPanel, BorderLayout.CENTER);

        return topPanel;
    }

    private JSplitPane createCenterPanel() {
        JPanel vePanel = createTablePanel(
            new String[]{"Mã vé", "Trạng thái", "Giá vé", "Số ghế", "Ngày đặt", "Phòng chiếu", "Thời gian chiếu", "Tên phim", "Khuyến mãi"},
            "DANH SÁCH VÉ"
        );
        tableVe = (JTable) ((JScrollPane) vePanel.getComponent(0)).getViewport().getView();
        tableVeModel = (DefaultTableModel) tableVe.getModel();

        JPanel khachHangPanel = createTablePanel(
            new String[]{"Mã khách hàng", "Tên khách hàng", "Số điện thoại", "Email", "Điểm tích lũy"},
            "THÔNG TIN KHÁCH HÀNG"
        );
        tableKhachHang = (JTable) ((JScrollPane) khachHangPanel.getComponent(0)).getViewport().getView();
        tableKhachHangModel = (DefaultTableModel) tableKhachHang.getModel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, vePanel, khachHangPanel);
        splitPane.setResizeWeight(0.85); // Tăng tỷ lệ cho phần "Danh sách vé"
        return splitPane;
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
        table.setRowHeight(35); // Tăng chiều cao hàng để dễ đọc
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
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setBackground(new Color(245, 247, 250));
        
        btnThem = createButton("THÊM", new Color(40, 167, 69));
        btnSua = createButton("SỬA", new Color(0, 123, 255));
        btnXoa = createButton("XÓA", new Color(220, 53, 69));
        btnClear = createButton("LÀM MỚI", new Color(108, 117, 125));
        btnRefresh = createButton("CẬP NHẬT", new Color(255, 193, 7));

        buttonPanel.add(btnThem);
        buttonPanel.add(btnSua);
        buttonPanel.add(btnXoa);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnRefresh);

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
    public JTextField getTxtGiaVe() { return txtGiaVe; }
    public JTextField getTxtSoGhe() { return txtSoGhe; }
    public JTextField getTxtNgayDat() { return txtNgayDat; }
    public JComboBox<String> getCbTenPhong() { return cbTenPhong; }
    public JComboBox<String> getCbNgayGioChieu() { return cbNgayGioChieu; }
    public JComboBox<String> getCbTenPhim() { return cbTenPhim; }
    public JTextField getSearchField() { return searchField; }
    public JTable getTable() { return tableVe; }
    public DefaultTableModel getTableModel() { return tableVeModel; }
    public JTable getTableKhachHang() { return tableKhachHang; }
    public DefaultTableModel getTableKhachHangModel() { return tableKhachHangModel; }
    public JButton getBtnThem() { return btnThem; }
    public JButton getBtnSua() { return btnSua; }
    public JButton getBtnXoa() { return btnXoa; }
    public JButton getBtnClear() { return btnClear; }
    public JButton getBtnRefresh() { return btnRefresh; }
    public JTextField getTxtKhuyenMai() { return txtKhuyenMai; }
    public JLabel getSoGheErrorLabel() { return soGheErrorLabel; }
    public JLabel getGiaVeErrorLabel() { return giaVeErrorLabel; }
    public JLabel getTenPhongErrorLabel() { return tenPhongErrorLabel; }
    public JLabel getNgayGioChieuErrorLabel() { return ngayGioChieuErrorLabel; }
    public JLabel getTenPhimErrorLabel() { return tenPhimErrorLabel; }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}