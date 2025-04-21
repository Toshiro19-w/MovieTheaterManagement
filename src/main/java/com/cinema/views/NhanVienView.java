package com.cinema.views;

import com.cinema.controllers.NhanVienController;
import com.cinema.models.LoaiNguoiDung;
import com.cinema.utils.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;

public class NhanVienView extends JPanel {
    private DatabaseConnection databaseConnection;
    private JTextField txtSearchMaND, txtSearchHoTen, txtSearchChucVu;
    private JTextField txtMaND, txtHoTen, txtSDT, txtEmail, txtChucVu, txtLuong;
    private JComboBox<String> vaiTroCombo;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnThem, btnSua, btnXoa, btnClear;

    public NhanVienView() {
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
        JPanel searchPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        searchPanel.setBorder(BorderFactory.createTitledBorder("TÌM KIẾM"));
        searchPanel.add(new JLabel("Mã ND:"));
        txtSearchMaND = new JTextField();
        searchPanel.add(txtSearchMaND);
        searchPanel.add(new JLabel("Họ Tên:"));
        txtSearchHoTen = new JTextField();
        searchPanel.add(txtSearchHoTen);
        searchPanel.add(new JLabel("Chức Vụ:"));
        txtSearchChucVu = new JTextField();
        searchPanel.add(txtSearchChucVu);

        // Phần thông tin nhân viên
        JPanel infoPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        infoPanel.setBorder(BorderFactory.createTitledBorder("THÔNG TIN NHÂN VIÊN"));

        infoPanel.add(new JLabel("Mã ND:"));
        txtMaND = new JTextField();
        txtMaND.setEditable(false);
        infoPanel.add(txtMaND);

        infoPanel.add(new JLabel("Họ Tên:"));
        txtHoTen = new JTextField();
        infoPanel.add(txtHoTen);

        infoPanel.add(new JLabel("SĐT:"));
        txtSDT = new JTextField();
        infoPanel.add(txtSDT);

        infoPanel.add(new JLabel("Email:"));
        txtEmail = new JTextField();
        infoPanel.add(txtEmail);

        infoPanel.add(new JLabel("Chức Vụ:"));
        txtChucVu = new JTextField();
        infoPanel.add(txtChucVu);

        infoPanel.add(new JLabel("Lương:"));
        txtLuong = new JTextField();
        infoPanel.add(txtLuong);

        infoPanel.add(new JLabel("Vai Trò:"));
        vaiTroCombo = new JComboBox<>(new String[]{"Admin", "QuanLy", "ThuNgan", "BanVe"});
        infoPanel.add(vaiTroCombo);

        // Phần bảng danh sách nhân viên
        String[] columns = {"Mã ND", "Họ Tên", "SĐT", "Email", "Chức Vụ", "Lương", "Vai Trò"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("DANH SÁCH NHÂN VIÊN"));

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
        new NhanVienController(this);
    }

    // Getter cho controller truy cập
    public DatabaseConnection getDatabaseConnection() { return databaseConnection; }
    public JTextField getTxtSearchMaND() { return txtSearchMaND; }
    public JTextField getTxtSearchHoTen() { return txtSearchHoTen; }
    public JTextField getTxtSearchChucVu() { return txtSearchChucVu; }
    public JTextField getTxtMaND() { return txtMaND; }
    public JTextField getTxtHoTen() { return txtHoTen; }
    public JTextField getTxtSDT() { return txtSDT; }
    public JTextField getTxtEmail() { return txtEmail; }
    public JTextField getTxtChucVu() { return txtChucVu; }
    public JTextField getTxtLuong() { return txtLuong; }
    public JComboBox<String> getVaiTroCombo() { return vaiTroCombo; }
    public JTable getTable() { return table; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JButton getBtnThem() { return btnThem; }
    public JButton getBtnSua() { return btnSua; }
    public JButton getBtnXoa() { return btnXoa; }
    public JButton getBtnClear() { return btnClear; }
}