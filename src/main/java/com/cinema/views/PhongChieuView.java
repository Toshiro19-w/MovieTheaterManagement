package com.cinema.views;

import com.cinema.controllers.PhongChieuController;
import com.cinema.models.PhongChieu;
import com.cinema.services.PhongChieuService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.util.List;

public class PhongChieuView extends JFrame {
    private Connection conn;
    private final PhongChieuController controller;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtMaPhong, txtSoLuongGhe, txtLoaiPhong;
    private JButton btnThem, btnSua, btnXoa, btnClear;

    public PhongChieuView() {
        this.controller = new PhongChieuController(new PhongChieuService(conn));
        initializeUI();
        loadDataToTable();
    }

    private void initializeUI() {
        setTitle("Quản lý Phòng Chiếu");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Table
        tableModel = new DefaultTableModel(new Object[]{"Mã Phòng", "Số Lượng Ghế", "Loại Phòng"}, 0);
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectRowToForm();
            }
        });
        JScrollPane scrollPane = new JScrollPane(table);

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin Phòng Chiếu"));

        formPanel.add(new JLabel("Mã Phòng:"));
        txtMaPhong = new JTextField();
        txtMaPhong.setEditable(false); // Không cho sửa mã phòng
        formPanel.add(txtMaPhong);

        formPanel.add(new JLabel("Số Lượng Ghế:"));
        txtSoLuongGhe = new JTextField();
        formPanel.add(txtSoLuongGhe);

        formPanel.add(new JLabel("Loại Phòng:"));
        txtLoaiPhong = new JTextField();
        formPanel.add(txtLoaiPhong);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnThem = new JButton("Thêm");
        btnSua = new JButton("Sửa");
        btnXoa = new JButton("Xóa");
        btnClear = new JButton("Clear");

        buttonPanel.add(btnThem);
        buttonPanel.add(btnSua);
        buttonPanel.add(btnXoa);
        buttonPanel.add(btnClear);

        btnThem.addActionListener(e -> themPhongChieu());
        btnSua.addActionListener(e -> suaPhongChieu());
        btnXoa.addActionListener(e -> xoaPhongChieu());
        btnClear.addActionListener(e -> clearForm());

        // Add components to main panel
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(formPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void loadDataToTable() {
        tableModel.setRowCount(0); // Clear table
        List<PhongChieu> danhSach = controller.findAll();
        if (danhSach != null) {
            for (PhongChieu pc : danhSach) {
                tableModel.addRow(new Object[]{
                        pc.getMaPhong(),
                        pc.getSoLuongGhe(),
                        pc.getLoaiPhong()
                });
            }
        }
    }

    private void selectRowToForm() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            txtMaPhong.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtSoLuongGhe.setText(tableModel.getValueAt(selectedRow, 1).toString());
            txtLoaiPhong.setText(tableModel.getValueAt(selectedRow, 2).toString());
        }
    }

    private void themPhongChieu() {
        try {
            int soLuongGhe = Integer.parseInt(txtSoLuongGhe.getText());
            String loaiPhong = txtLoaiPhong.getText();

            PhongChieu phongChieu = new PhongChieu(0, soLuongGhe, loaiPhong);
            PhongChieu result = controller.save(phongChieu);

            if (result != null) {
                JOptionPane.showMessageDialog(this, "Thêm phòng chiếu thành công!");
                loadDataToTable();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm phòng chiếu thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số lượng ghế phải là số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void suaPhongChieu() {
        try {
            if (txtMaPhong.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn phòng chiếu cần sửa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int maPhong = Integer.parseInt(txtMaPhong.getText());
            int soLuongGhe = Integer.parseInt(txtSoLuongGhe.getText());
            String loaiPhong = txtLoaiPhong.getText();

            PhongChieu phongChieu = new PhongChieu(maPhong, soLuongGhe, loaiPhong);
            PhongChieu result = controller.update(phongChieu);

            if (result != null) {
                JOptionPane.showMessageDialog(this, "Cập nhật phòng chiếu thành công!");
                loadDataToTable();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật phòng chiếu thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số lượng ghế phải là số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xoaPhongChieu() {
        if (txtMaPhong.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phòng chiếu cần xóa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int maPhong = Integer.parseInt(txtMaPhong.getText());
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn xóa phòng chiếu này?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean result = controller.delete(maPhong);
            if (result) {
                JOptionPane.showMessageDialog(this, "Xóa phòng chiếu thành công!");
                loadDataToTable();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa phòng chiếu thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        txtMaPhong.setText("");
        txtSoLuongGhe.setText("");
        txtLoaiPhong.setText("");
        table.clearSelection();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PhongChieuView view = new PhongChieuView();
            view.setVisible(true);
        });
    }
}