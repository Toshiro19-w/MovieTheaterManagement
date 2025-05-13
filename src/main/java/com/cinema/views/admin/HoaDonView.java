package com.cinema.views.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.cinema.components.UnderlineTextField;
import com.cinema.controllers.HoaDonController;

public class HoaDonView extends JPanel {
    private JTextField searchField;
    private UnderlineTextField txtMaHoaDon, txtNgayLap, txtTongTien;
    private JTable tableHoaDon, tableChiTietHoaDon;
    private DefaultTableModel modelHoaDon, modelChiTietHoaDon;
    private TableRowSorter<DefaultTableModel> sorter;
    private JButton btnThem, btnSua, btnXoa, btnInHoaDon, btnLamMoi;
    private String username;
    private HoaDonController controller;

    public HoaDonView(String username) throws IOException, SQLException {
        this.username = username;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 245, 245));

        // Panel chính chứa tất cả các thành phần
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setOpaque(false);

        // Panel phía trên chứa search và info
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);
        
        // Thêm các panel con
        topPanel.add(createSearchPanel(), BorderLayout.NORTH);
        topPanel.add(createInfoPanel(), BorderLayout.CENTER);
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        // Panel chứa bảng và nút
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);
        
        // Thêm panel nút chức năng
        centerPanel.add(createButtonPanel(), BorderLayout.NORTH);
        
        // Panel chứa bảng
        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        tablesPanel.setOpaque(false);
        
        // Tạo và thêm các bảng
        tablesPanel.add(createHoaDonTablePanel());
        tablesPanel.add(createChiTietTablePanel());
        
        centerPanel.add(tablesPanel, BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Thêm panel chính vào view
        add(mainPanel);

        // Khởi tạo controller
        controller = new HoaDonController(this, username);
    }

    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100)),
                "TÌM KIẾM HÓA ĐƠN"
        ));
        searchPanel.setBackground(new Color(255, 255, 255));

        searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
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

        searchPanel.add(new JLabel("Tìm kiếm:"));
        searchPanel.add(searchField);

        return searchPanel;
    }

    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100)),
                "THÔNG TIN HÓA ĐƠN"
        ));
        infoPanel.setBackground(new Color(255, 255, 255));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Khởi tạo các trường với UnderlineTextField
        txtMaHoaDon = new UnderlineTextField(15);
        txtMaHoaDon.setEditable(false);
        txtMaHoaDon.setPlaceholder("Mã hóa đơn");
        txtMaHoaDon.setReadonlyColor(new Color(100, 100, 100));

        txtNgayLap = new UnderlineTextField(15);
        txtNgayLap.setEditable(false);
        txtNgayLap.setPlaceholder("Ngày lập");
        txtNgayLap.setReadonlyColor(new Color(100, 100, 100));

        txtTongTien = new UnderlineTextField(15);
        txtTongTien.setEditable(false);
        txtTongTien.setPlaceholder("Tổng tiền");
        txtTongTien.setReadonlyColor(new Color(100, 100, 100));

        // Tạo labels với font và màu sắc mới
        Font labelFont = new Font("Segoe UI", Font.BOLD, 14);
        Color labelColor = new Color(70, 70, 70);

        JLabel lblMaHoaDon = new JLabel("Mã Hóa Đơn:");
        lblMaHoaDon.setFont(labelFont);
        lblMaHoaDon.setForeground(labelColor);

        JLabel lblNgayLap = new JLabel("Ngày Lập:");
        lblNgayLap.setFont(labelFont);
        lblNgayLap.setForeground(labelColor);

        JLabel lblTongTien = new JLabel("Tổng Tiền:");
        lblTongTien.setFont(labelFont);
        lblTongTien.setForeground(labelColor);

        // Thêm components vào panel với layout mới
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        infoPanel.add(lblMaHoaDon, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        infoPanel.add(txtMaHoaDon, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        infoPanel.add(lblNgayLap, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        infoPanel.add(txtNgayLap, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        infoPanel.add(lblTongTien, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        infoPanel.add(txtTongTien, gbc);

        // Thêm padding cho panel
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "THÔNG TIN HÓA ĐƠN",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(70, 70, 70)
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        return infoPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(new Color(255, 255, 255));
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        btnThem = createStyledButton("Thêm", new Color(46, 204, 113));
        btnSua = createStyledButton("Sửa", new Color(52, 152, 219));
        btnXoa = createStyledButton("Xóa", new Color(231, 76, 60));
        btnInHoaDon = createStyledButton("In Hóa Đơn", new Color(155, 89, 182));
        btnLamMoi = createStyledButton("Làm Mới", new Color(149, 165, 166));

        buttonPanel.add(btnThem);
        buttonPanel.add(btnSua);
        buttonPanel.add(btnXoa);
        buttonPanel.add(btnInHoaDon);
        buttonPanel.add(btnLamMoi);

        // Thêm nút chọn khách hàng
        JButton btnChonKhachHang = new JButton("Chọn Khách Hàng");
        btnChonKhachHang.addActionListener(e -> {
            int selectedRow = tableHoaDon.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn!");
                return;
            }
            int maHoaDon = (int) tableHoaDon.getValueAt(selectedRow, 0);
            JDialog dialog = createChonKhachHangDialog();
            dialog.setVisible(true);
        });
        buttonPanel.add(btnChonKhachHang);

        // Thêm nút thêm vé
        JButton btnThemVe = new JButton("Thêm Vé");
        btnThemVe.addActionListener(e -> {
            int selectedRow = tableHoaDon.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn!");
                return;
            }
            int maHoaDon = (int) tableHoaDon.getValueAt(selectedRow, 0);
            JDialog dialog = createThemChiTietDialog(maHoaDon);
            dialog.setVisible(true);
        });
        buttonPanel.add(btnThemVe);

        return buttonPanel;
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(backgroundColor);
        button.setPreferredSize(new Dimension(120, 35));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        
        // Thêm hiệu ứng hover
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor);
            }
        });

        return button;
    }

    private JPanel createHoaDonTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "DANH SÁCH HÓA ĐƠN",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));

        String[] columnsHoaDon = {"ID", "Tên NV", "Tên KH", "Ngày", "Tổng Tiền"};
        modelHoaDon = new DefaultTableModel(columnsHoaDon, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableHoaDon = new JTable(modelHoaDon);
        setupTable(tableHoaDon);
        
        JScrollPane scrollPane = new JScrollPane(tableHoaDon);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createChiTietTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "CHI TIẾT HÓA ĐƠN",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));

        String[] columnsChiTiet = {"Mã Vé", "Số Ghế", "Loại Ghế", "Giá Gốc", "Khuyến Mãi", "Giá Sau Giảm"};
        modelChiTietHoaDon = new DefaultTableModel(columnsChiTiet, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableChiTietHoaDon = new JTable(modelChiTietHoaDon);
        setupTable(tableChiTietHoaDon);
        
        // Căn giữa cột giá
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        tableChiTietHoaDon.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        tableChiTietHoaDon.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
        
        // Format số tiền
        tableChiTietHoaDon.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof Number) {
                    value = String.format("%,.0f VNĐ", ((Number) value).doubleValue());
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });
        
        tableChiTietHoaDon.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof Number) {
                    value = String.format("%,.0f VNĐ", ((Number) value).doubleValue());
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(tableChiTietHoaDon);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private void setupTable(JTable table) {
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(240, 240, 240));
        table.setSelectionBackground(new Color(232, 241, 249));
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(new Color(230, 230, 230));
        
        // Căn giữa header
        ((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer())
            .setHorizontalAlignment(JLabel.CENTER);
    }

    private JDialog createThemChiTietDialog(int maHoaDon) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thêm Vé Vào Hóa Đơn", true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Panel tìm kiếm
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField txtSearch = new JTextField(20);
        JButton btnSearch = new JButton("Tìm kiếm");
        searchPanel.add(new JLabel("Tìm kiếm:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        
        // Bảng vé
        String[] columns = {"Mã Vé", "Tên Phim", "Số Ghế", "Loại Ghế", "Ngày Giờ Chiếu", "Giá Vé", "Trạng Thái"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable tableVe = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(tableVe);
        
        // Panel nút
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnThem = new JButton("Thêm");
        JButton btnHuy = new JButton("Hủy");
        buttonPanel.add(btnThem);
        buttonPanel.add(btnHuy);
        
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        
        // Xử lý sự kiện
        btnSearch.addActionListener(e -> {
            String searchText = txtSearch.getText().trim();
            // TODO: Implement search functionality
        });
        
        btnThem.addActionListener(e -> {
            int selectedRow = tableVe.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng chọn vé!");
                return;
            }
            int maVe = (int) tableVe.getValueAt(selectedRow, 0);
            if (controller.themVeVaoHoaDon(maHoaDon, maVe)) {
                dialog.dispose();
                refreshTable();
            }
        });
        
        btnHuy.addActionListener(e -> dialog.dispose());
        
        // Load dữ liệu
        controller.hienThiVeCoTheThem(tableVe);
        
        return dialog;
    }

    private JDialog createChonKhachHangDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chọn Khách Hàng", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Panel tìm kiếm
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField txtSearch = new JTextField(20);
        JButton btnSearch = new JButton("Tìm kiếm");
        searchPanel.add(new JLabel("Tìm kiếm:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        
        // Bảng khách hàng
        String[] columns = {"Mã KH", "Họ Tên", "Số Điện Thoại", "Email"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable tableKhachHang = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(tableKhachHang);
        
        // Panel nút
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnChon = new JButton("Chọn");
        JButton btnHuy = new JButton("Hủy");
        buttonPanel.add(btnChon);
        buttonPanel.add(btnHuy);
        
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        
        // Xử lý sự kiện
        btnSearch.addActionListener(e -> {
            String searchText = txtSearch.getText().trim();
            // TODO: Implement search functionality
        });
        
        btnChon.addActionListener(e -> {
            int selectedRow = tableKhachHang.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng chọn khách hàng!");
                return;
            }
            int maKhachHang = (int) tableKhachHang.getValueAt(selectedRow, 0);
            int maHoaDon = (int) tableHoaDon.getValueAt(tableHoaDon.getSelectedRow(), 0);
            if (controller.capNhatKhachHang(maHoaDon, maKhachHang)) {
                dialog.dispose();
                refreshTable();
            }
        });
        
        btnHuy.addActionListener(e -> dialog.dispose());
        
        // Load dữ liệu
        controller.hienThiDanhSachKhachHang(tableKhachHang);
        
        return dialog;
    }

    // Getter cho controller truy cập
    public JTextField getSearchField() { return searchField; }
    public String getSearchText() { return searchField.getText(); }
    public UnderlineTextField getTxtMaHoaDon() { return txtMaHoaDon; }
    public UnderlineTextField getTxtNgayLap() { return txtNgayLap; }
    public UnderlineTextField getTxtTongTien() { return txtTongTien; }
    public JTable getTableHoaDon() { return tableHoaDon; }
    public DefaultTableModel getModelHoaDon() { return modelHoaDon; }
    public DefaultTableModel getModelChiTietHoaDon() { return modelChiTietHoaDon; }
    
    // Getter cho các nút
    public JButton getBtnThem() { return btnThem; }
    public JButton getBtnSua() { return btnSua; }
    public JButton getBtnXoa() { return btnXoa; }
    public JButton getBtnInHoaDon() { return btnInHoaDon; }
    public JButton getBtnLamMoi() { return btnLamMoi; }

    public String getUsername() {
        return username;
    }

    // Getter cho các component mới
    public JDialog getThemChiTietDialog(int maHoaDon) {
        return createThemChiTietDialog(maHoaDon);
    }

    public JDialog getChonKhachHangDialog() {
        return createChonKhachHangDialog();
    }

    private void refreshTable() {
        // Implement refresh logic here
    }
}