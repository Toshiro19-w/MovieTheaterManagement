package com.cinema.views;

import com.cinema.controllers.PhongChieuController;
import com.cinema.utils.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;

public class PhongChieuView extends JPanel {
    private DatabaseConnection databaseConnection;
    private JTable table;
    private JTextField txtSearchTenPhong;
    private DefaultTableModel tableModel;
    private JTextField txtMaPhong, txtTenPhong, txtSoLuongGhe, txtLoaiPhong;
    private JButton btnThem, btnClear;

    public PhongChieuView() {
        try {
            databaseConnection = new DatabaseConnection();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Không thể đọc file cấu hình cơ sở dữ liệu!");
            System.exit(1);
            return;
        }
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initializeUI();
        new PhongChieuController(this); // Khởi tạo controller
    }

    private void initializeUI() {
        // Panel tìm kiếm
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Tìm kiếm phòng chiếu"));
        searchPanel.setPreferredSize(new Dimension(200, 100));

        searchPanel.add(new JLabel("Tên phòng:"));
        txtSearchTenPhong = new JTextField();
        txtSearchTenPhong.setMaximumSize(new Dimension(200, 25));
        setPlaceholder(txtSearchTenPhong, "Nhập tên phòng...");
        searchPanel.add(txtSearchTenPhong);

        JButton searchButton = new JButton("Tìm kiếm");
        searchButton.setMaximumSize(new Dimension(100, 25));
        searchPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        searchPanel.add(searchButton);

        add(searchPanel, BorderLayout.WEST);

        // Bảng dữ liệu
        tableModel = new DefaultTableModel(new Object[]{
                "Mã Phòng", "Tên Phòng", "Số Lượng Ghế", "Loại Phòng"
        }, 0);
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Form nhập liệu
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin phòng chiếu"));

        formPanel.add(new JLabel("Mã Phòng:"));
        txtMaPhong = new JTextField();
        txtMaPhong.setEditable(false);
        formPanel.add(txtMaPhong);

        formPanel.add(new JLabel("Tên Phòng:"));
        txtTenPhong = new JTextField();
        formPanel.add(txtTenPhong);

        formPanel.add(new JLabel("Số Lượng Ghế:"));
        txtSoLuongGhe = new JTextField();
        formPanel.add(txtSoLuongGhe);

        formPanel.add(new JLabel("Loại Phòng:"));
        txtLoaiPhong = new JTextField();
        formPanel.add(txtLoaiPhong);

        // Panel nút chức năng
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnThem = new JButton("Thêm");
        btnClear = new JButton("Clear");

        buttonPanel.add(btnThem);
        buttonPanel.add(btnClear);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(formPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

        // Gắn sự kiện tìm kiếm
        searchButton.addActionListener(_ -> txtSearchTenPhong.postActionEvent());
    }

    // Getters để controller truy cập
    public DatabaseConnection getDatabaseConnection() {
        return databaseConnection;
    }

    public JTable getTable() {
        return table;
    }

    public JTextField getTxtTenPhong() {
        return txtTenPhong;
    }

    public JTextField getTxtSearchTenPhong() {
        return txtSearchTenPhong;
    }

    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    public JTextField getTxtMaPhong() {
        return txtMaPhong;
    }

    public JTextField getTxtSoLuongGhe() {
        return txtSoLuongGhe;
    }

    public JTextField getTxtLoaiPhong() {
        return txtLoaiPhong;
    }

    public JButton getBtnThem() {
        return btnThem;
    }

    public JButton getBtnClear() {
        return btnClear;
    }

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