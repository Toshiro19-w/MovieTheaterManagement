package com.cinema.views.admin;

import com.cinema.controllers.HoaDonController;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class HoaDonView extends JPanel {
    private JTextField searchField;
    private JTextField txtTenNhanVien, txtNgayLap, txtTongTien;
    private JTable tableHoaDon, tableChiTietHoaDon;
    private DefaultTableModel modelHoaDon, modelChiTietHoaDon;
    private TableRowSorter<DefaultTableModel> sorter;

    public HoaDonView() throws IOException {
        // Thiết lập layout chính
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 245, 245));

        // Panel tìm kiếm
        JPanel searchPanel = createSearchPanel();

        // Panel thông tin hóa đơn
        JPanel infoPanel = createInfoPanel();

        // Panel chứa cả search và info
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);
        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(infoPanel, BorderLayout.CENTER);

        // Danh sách hóa đơn
        JPanel tablePanel = createTablePanel();

        // Thêm các thành phần vào panel chính
        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);

        // Khởi tạo controller
        new HoaDonController(this);
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
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Initialize text fields
        txtTenNhanVien = new JTextField(15);
        txtTenNhanVien.setEditable(false);
        txtNgayLap = new JTextField(15);
        txtNgayLap.setEditable(false);
        txtTongTien = new JTextField(15);
        txtTongTien.setEditable(false);

        // Styling text fields
        Font fieldFont = new Font("Arial", Font.PLAIN, 14);
        txtTenNhanVien.setFont(fieldFont);
        txtNgayLap.setFont(fieldFont);
        txtTongTien.setFont(fieldFont);

        // Add components
        gbc.gridx = 0;
        gbc.gridy = 0;
        infoPanel.add(new JLabel("Tên Nhân Viên:"), gbc);
        gbc.gridx = 1;
        infoPanel.add(txtTenNhanVien, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        infoPanel.add(new JLabel("Ngày Lập:"), gbc);
        gbc.gridx = 1;
        infoPanel.add(txtNgayLap, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        infoPanel.add(new JLabel("Tổng Tiền:"), gbc);
        gbc.gridx = 1;
        infoPanel.add(txtTongTien, gbc);

        return infoPanel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new GridLayout(1, 2, 10, 10));
        tablePanel.setOpaque(false);

        // Danh sách hóa đơn
        String[] columnsHoaDon = {"ID", "Tên NV", "Tên KH", "Ngày", "Tổng Tiền"};
        modelHoaDon = new DefaultTableModel(columnsHoaDon, 0);
        tableHoaDon = new JTable(modelHoaDon);
        tableHoaDon.setRowHeight(25);
        tableHoaDon.setFont(new Font("Arial", Font.PLAIN, 13));
        tableHoaDon.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        sorter = new TableRowSorter<>(modelHoaDon);
        tableHoaDon.setRowSorter(sorter);

        JScrollPane scrollHoaDon = new JScrollPane(tableHoaDon);
        scrollHoaDon.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100)),
                "DANH SÁCH HÓA ĐƠN"
        ));

        // Chi tiết hóa đơn
        String[] columnsChiTiet = {"Mã Vé", "Tên Phim", "Số Ghế", "Ngày Chiếu", "Giá Vé"};
        modelChiTietHoaDon = new DefaultTableModel(columnsChiTiet, 0);
        tableChiTietHoaDon = new JTable(modelChiTietHoaDon);
        tableChiTietHoaDon.setRowHeight(25);
        tableChiTietHoaDon.setFont(new Font("Arial", Font.PLAIN, 13));
        tableChiTietHoaDon.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));

        JScrollPane scrollChiTiet = new JScrollPane(tableChiTietHoaDon);
        scrollChiTiet.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100)),
                "CHI TIẾT HÓA ĐƠN"
        ));

        tablePanel.add(scrollHoaDon);
        tablePanel.add(scrollChiTiet);

        return tablePanel;
    }

    // Getter cho controller truy cập
    public JTextField getSearchField() { return searchField; }
    public String getSearchText() { return searchField.getText(); }
    public JTextField getTxtTenNhanVien() { return txtTenNhanVien; }
    public JTextField getTxtNgayLap() { return txtNgayLap; }
    public JTextField getTxtTongTien() { return txtTongTien; }
    public JTable getTableHoaDon() { return tableHoaDon; }
    public DefaultTableModel getModelHoaDon() { return modelHoaDon; }
    public DefaultTableModel getModelChiTietHoaDon() { return modelChiTietHoaDon; }
}