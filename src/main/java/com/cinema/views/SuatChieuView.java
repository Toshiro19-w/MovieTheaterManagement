package com.cinema.views;

import com.cinema.controllers.SuatChieuController;
import com.cinema.utils.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.sql.SQLException;

public class SuatChieuView extends JPanel {
    private DatabaseConnection databaseConnection;
    private JTextField ngayChieuField;
    private JTextField txtMaSuatChieu, txtNgayGioChieu;
    private JComboBox cbMaPhim, cbMaPhong;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnThem, btnSua, btnXoa, btnClear;

    public SuatChieuView() {
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
        JPanel searchPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        searchPanel.setBorder(BorderFactory.createTitledBorder("TÌM KIẾM"));
        searchPanel.add(new JLabel("Ngày Giờ Chiếu:"));
        ngayChieuField = new JTextField();
        setPlaceholder(ngayChieuField, "dd/MM/yyyy HH:mm:ss");
        searchPanel.add(ngayChieuField);

        // Phần thông tin suất chiếu
        JPanel infoPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        infoPanel.setBorder(BorderFactory.createTitledBorder("THÔNG TIN SUẤT CHIẾU"));

        infoPanel.add(new JLabel("Mã Suất Chiếu:"));
        txtMaSuatChieu = new JTextField();
        txtMaSuatChieu.setEditable(false);
        infoPanel.add(txtMaSuatChieu);

        infoPanel.add(new JLabel("Phim:"));
        cbMaPhim = new JComboBox<>();
        infoPanel.add(cbMaPhim);

        infoPanel.add(new JLabel("Phòng Chiếu:"));
        cbMaPhong = new JComboBox<>();
        infoPanel.add(cbMaPhong);

        infoPanel.add(new JLabel("Ngày Giờ Chiếu:"));
        txtNgayGioChieu = new JTextField();
        setPlaceholder(txtNgayGioChieu, "dd/MM/yyyy HH:mm:ss");
        infoPanel.add(txtNgayGioChieu);

        // Phần bảng danh sách suất chiếu
        String[] columns = {"Mã Suất Chiếu", "Tên Phim", "Phòng Chiếu", "Ngày Giờ Chiếu"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("DANH SÁCH SUẤT CHIẾU"));

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
        try {
            new SuatChieuController(this);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi khởi tạo SuatChieuController!");
        }
    }

    // Getters để controller truy cập
    public DatabaseConnection getDatabaseConnection() { return databaseConnection; }
    public JTextField getNgayChieuField() { return ngayChieuField; }
    public JTextField getTxtMaSuatChieu() { return txtMaSuatChieu; }
    public JTextField getTxtNgayGioChieu() { return txtNgayGioChieu; }
    public JComboBox getCbMaPhim() { return cbMaPhim; }
    public JComboBox getCbMaPhong() { return cbMaPhong; }
    public JTable getTable() { return table; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JButton getBtnThem() { return btnThem; }
    public JButton getBtnSua() { return btnSua; }
    public JButton getBtnXoa() { return btnXoa; }
    public JButton getBtnClear() { return btnClear; }

    private void setPlaceholder(JTextField field, String placeholder) {
        field.setText(placeholder);
        field.setForeground(Color.GRAY);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(Color.GRAY);
                    field.setText(placeholder);
                }
            }
        });
    }
}