package com.cinema.views;

import com.cinema.controllers.PhimController;
import com.cinema.utils.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;

public class PhimView extends JPanel {
    private DatabaseConnection databaseConnection;
    private JTextField txtSearchTenPhim;
    private JTextField txtMaPhim, txtTenPhim, txtTenTheLoai, txtThoiLuong,
            txtNgayKhoiChieu, txtNuocSanXuat, txtDinhDang, txtMoTa,
            txtDaoDien, txtSearchTenTheLoai, txtSearchNuocSanXuat, txtSearchDaoDien;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnThem, btnSua, btnXoa, btnClear;

    public PhimView() throws SQLException {
        try {
            databaseConnection = new DatabaseConnection();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Không thể đọc file cấu hình cơ sở dữ liệu!");
            return;
        }

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Phần tìm kiếm
        JPanel searchPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        searchPanel.setBorder(BorderFactory.createTitledBorder("TÌM KIẾM"));
        searchPanel.add(new JLabel("Tên Phim:"));
        txtSearchTenPhim = new JTextField();
        searchPanel.add(txtSearchTenPhim);
        searchPanel.add(new JLabel("Thể Loại:"));
        txtSearchTenTheLoai = new JTextField();
        searchPanel.add(txtSearchTenTheLoai);
        searchPanel.add(new JLabel("Nước Sản Xuất:"));
        txtSearchNuocSanXuat = new JTextField();
        searchPanel.add(txtSearchNuocSanXuat);
        searchPanel.add(new JLabel("Đạo diễn:"));
        txtSearchDaoDien = new JTextField();
        searchPanel.add(txtSearchDaoDien);

        // Phần thông tin phim
        JPanel infoPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        infoPanel.setBorder(BorderFactory.createTitledBorder("THÔNG TIN PHIM"));

        infoPanel.add(new JLabel("Mã Phim:"));
        txtMaPhim = new JTextField();
        txtMaPhim.setEditable(false);
        infoPanel.add(txtMaPhim);

        infoPanel.add(new JLabel("Tên Phim:"));
        txtTenPhim = new JTextField();
        infoPanel.add(txtTenPhim);

        infoPanel.add(new JLabel("Thể Loại:"));
        txtTenTheLoai = new JTextField();
        infoPanel.add(txtTenTheLoai);

        infoPanel.add(new JLabel("Thời Lượng:"));
        txtThoiLuong = new JTextField();
        infoPanel.add(txtThoiLuong);

        infoPanel.add(new JLabel("Ngày Khởi Chiếu:"));
        txtNgayKhoiChieu = new JTextField();
        infoPanel.add(txtNgayKhoiChieu);

        infoPanel.add(new JLabel("Nước Sản Xuất:"));
        txtNuocSanXuat = new JTextField();
        infoPanel.add(txtNuocSanXuat);

        infoPanel.add(new JLabel("Định Dạng:"));
        txtDinhDang = new JTextField();
        infoPanel.add(txtDinhDang);

        infoPanel.add(new JLabel("Mô Tả:"));
        txtMoTa = new JTextField();
        infoPanel.add(txtMoTa);

        infoPanel.add(new JLabel("Đạo Diễn:"));
        txtDaoDien = new JTextField();
        infoPanel.add(txtDaoDien);

        // Phần bảng danh sách phim
        String[] columns = {"Mã Phim", "Tên Phim", "Thể Loại", "Thời Lượng", "Ngày Khởi Chiếu", "Nước Sản Xuất", "Định Dạng", "Mô Tả", "Đạo Diễn"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("DANH SÁCH PHIM"));

        // Phần nút thao tác
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnThem = new JButton("THÊM");
        btnSua = new JButton("SỬA");
        btnXoa = new JButton("XÓA");
        btnClear = new JButton("CLEAR");
        buttonPanel.add(btnThem);
        buttonPanel.add(btnSua);
        buttonPanel.add(btnXoa);
        buttonPanel.add(btnClear);

        // Sắp xếp layout
        JPanel topPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        topPanel.add(searchPanel);
        topPanel.add(infoPanel);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Khởi tạo controller
        new PhimController(this);
    }

    // Getter cho controller truy cập
    public DatabaseConnection getDatabaseConnection() { return databaseConnection; }
    public JTextField getTxtSearchTenPhim() { return txtSearchTenPhim; }
    public JTextField getTxtMaPhim() { return txtMaPhim; }
    public JTextField getTxtTenPhim() { return txtTenPhim; }
    public JTextField getTxtTenTheLoai() { return txtTenTheLoai; }
    public JTextField getTxtThoiLuong() { return txtThoiLuong; }
    public JTextField getTxtNgayKhoiChieu() { return txtNgayKhoiChieu; }
    public JTextField getTxtNuocSanXuat() { return txtNuocSanXuat; }
    public JTextField getTxtDinhDang() { return txtDinhDang; }
    public JTextField getTxtMoTa() { return txtMoTa; }
    public JTextField getTxtDaoDien() { return txtDaoDien; }
    public JTable getTable() { return table; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JButton getBtnThem() { return btnThem; }
    public JButton getBtnSua() { return btnSua; }
    public JButton getBtnXoa() { return btnXoa; }
    public JButton getBtnClear() { return btnClear; }
    public JTextField getTxtSearchTenTheLoai() { return txtSearchTenTheLoai; }
    public JTextField getTxtSearchNuocSanXuat() {
        return txtSearchNuocSanXuat;
    }
    public JTextField getTxtSearchDaoDien() {
        return txtSearchDaoDien;
    }
}