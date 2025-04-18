package com.cinema.views;

import com.cinema.controllers.HoaDonController;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;

public class HoaDonView extends JPanel {
    private JTextField txtSearchID, txtSearchIDKhachHang, txtSearchTenKhachHang;
    private JTextField txtTenNhanVien, txtNgayLap, txtTongTien;
    private JTable tableHoaDon, tableChiTietHoaDon;
    private DefaultTableModel modelHoaDon, modelChiTietHoaDon;

    public HoaDonView() throws IOException {
        // Thiết lập layout chính
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Phần tìm kiếm
        JPanel searchPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        searchPanel.setBorder(BorderFactory.createTitledBorder("TÌM KIẾM"));

        txtSearchID = new JTextField();
        txtSearchIDKhachHang = new JTextField();
        txtSearchTenKhachHang = new JTextField();

        searchPanel.add(new JLabel("ID:"));
        searchPanel.add(txtSearchID);
        searchPanel.add(new JLabel("ID Khách Hàng:"));
        searchPanel.add(txtSearchIDKhachHang);
        searchPanel.add(new JLabel("Tên Khách Hàng:"));
        searchPanel.add(txtSearchTenKhachHang);

        // Phần thông tin hóa đơn
        JPanel infoPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        infoPanel.setBorder(BorderFactory.createTitledBorder("THÔNG TIN HÓA ĐƠN"));

        txtTenNhanVien = new JTextField();
        txtTenNhanVien.setEditable(false);
        txtNgayLap = new JTextField();
        txtNgayLap.setEditable(false);
        txtTongTien = new JTextField();
        txtTongTien.setEditable(false);

        infoPanel.add(new JLabel("Tên Nhân Viên:"));
        infoPanel.add(txtTenNhanVien);
        infoPanel.add(new JLabel("Ngày Lập:"));
        infoPanel.add(txtNgayLap);
        infoPanel.add(new JLabel("Tổng Tiền:"));
        infoPanel.add(txtTongTien);

        // Phần danh sách hóa đơn
        String[] columnsHoaDon = {"ID", "Tên NV", "Tên KH", "Ngày", "Tổng Tiền"};
        modelHoaDon = new DefaultTableModel(columnsHoaDon, 0);
        tableHoaDon = new JTable(modelHoaDon);
        JScrollPane scrollHoaDon = new JScrollPane(tableHoaDon);
        scrollHoaDon.setBorder(BorderFactory.createTitledBorder("DANH SÁCH HÓA ĐƠN"));

        // Phần chi tiết hóa đơn
        String[] columnsChiTiet = {"Mã Vé", "Tên Phim", "Số Ghế", "Ngày Chiếu", "Giá Vé"};
        modelChiTietHoaDon = new DefaultTableModel(columnsChiTiet, 0);
        tableChiTietHoaDon = new JTable(modelChiTietHoaDon);
        JScrollPane scrollChiTiet = new JScrollPane(tableChiTietHoaDon);
        scrollChiTiet.setBorder(BorderFactory.createTitledBorder("CHI TIẾT HÓA ĐƠN"));

        // Sắp xếp layout
        JPanel topPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        topPanel.add(searchPanel);
        topPanel.add(infoPanel);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollHoaDon, scrollChiTiet);
        splitPane.setDividerLocation(400);

        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        // Khởi tạo controller
        new HoaDonController(this);
    }

    // Getter cho controller truy cập
    public JTextField getTxtSearchID() { return txtSearchID; }
    public JTextField getTxtSearchIDKhachHang() { return txtSearchIDKhachHang; }
    public JTextField getTxtSearchTenKhachHang() { return txtSearchTenKhachHang; }
    public JTextField getTxtTenNhanVien() { return txtTenNhanVien; }
    public JTextField getTxtNgayLap() { return txtNgayLap; }
    public JTextField getTxtTongTien() { return txtTongTien; }
    public JTable getTableHoaDon() { return tableHoaDon; }
    public JTable getTableChiTietHoaDon() { return tableChiTietHoaDon; }
    public DefaultTableModel getModelHoaDon() { return modelHoaDon; }
    public DefaultTableModel getModelChiTietHoaDon() { return modelChiTietHoaDon; }
}