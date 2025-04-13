package com.cinema.views;

import com.cinema.controllers.*;
import com.cinema.models.*;
import com.cinema.services.*;
import com.cinema.utils.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class NhanVienView extends JPanel {
    private DatabaseConnection databaseConnection;
    private NhanVienController controller;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtMaND, txtHoTen, txtSDT, txtEmail, txtChucVu, txtLuong, tenNhanVienField;
    private JComboBox<String> vaiTroCombo;
    private JButton btnThem, btnSua, btnXoa, btnClear;

    public NhanVienView() {
        try {
            databaseConnection = new DatabaseConnection();
            controller = new NhanVienController(new NhanVienService(databaseConnection));
            setLayout(new BorderLayout(10, 10));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Không thể đọc file cấu hình cơ sở dữ liệu!");
            System.exit(1);
        }
        initializeUI();
        loadDataToTable();
    }

    private void initializeUI() {
        // Sidebar tìm kiếm
        JPanel searchPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Tìm kiếm nhân viên"));
        searchPanel.add(new JLabel("Tên nhân viên:"));
        tenNhanVienField = new JTextField();
        searchPanel.add(tenNhanVienField);

        JButton searchButton = new JButton("Tìm kiếm");
        searchButton.addActionListener(e -> searchNhanVien());
        searchPanel.add(searchButton);

        add(searchPanel, BorderLayout.WEST);

        tableModel = new DefaultTableModel(new Object[]{
                "Mã ND", "Họ tên", "SĐT", "Email", "Chức vụ", "Lương", "Vai trò"
        }, 0);

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectRowToForm();
            }
        });
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel formPanel = new JPanel(new GridLayout(7, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin Nhân viên"));

        formPanel.add(new JLabel("Mã ND:"));
        txtMaND = new JTextField();
        txtMaND.setEditable(false);
        formPanel.add(txtMaND);

        formPanel.add(new JLabel("Họ tên:"));
        txtHoTen = new JTextField();
        formPanel.add(txtHoTen);

        formPanel.add(new JLabel("SĐT:"));
        txtSDT = new JTextField();
        formPanel.add(txtSDT);

        formPanel.add(new JLabel("Email:"));
        txtEmail = new JTextField();
        formPanel.add(txtEmail);

        formPanel.add(new JLabel("Chức vụ:"));
        txtChucVu = new JTextField();
        formPanel.add(txtChucVu);

        formPanel.add(new JLabel("Lương:"));
        txtLuong = new JTextField();
        formPanel.add(txtLuong);

        formPanel.add(new JLabel("Vai trò:"));
        vaiTroCombo = new JComboBox<>(new String[]{"Admin", "QuanLy", "ThuNgan", "BanVe"});
        formPanel.add(vaiTroCombo);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnThem = new JButton("Thêm");
        btnSua = new JButton("Sửa");
        btnXoa = new JButton("Xóa");
        btnClear = new JButton("Clear");

        buttonPanel.add(btnThem);
        buttonPanel.add(btnSua);
        buttonPanel.add(btnXoa);
        buttonPanel.add(btnClear);

        btnThem.addActionListener(e -> themNhanVien());
        btnSua.addActionListener(e -> suaNhanVien());
        btnXoa.addActionListener(e -> xoaNhanVien());
        btnClear.addActionListener(e -> clearForm());

        add(scrollPane, BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(formPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadDataToTable() {
        tableModel.setRowCount(0);
        List<NhanVien> nhanVienList = controller.findAll();
        for (NhanVien nv : nhanVienList) {
            tableModel.addRow(new Object[]{
                    nv.getMaNguoiDung(),
                    nv.getHoTen(),
                    nv.getSoDienThoai(),
                    nv.getEmail(),
                    nv.getChucVu(),
                    formatCurrency(nv.getLuong()),
                    nv.getVaiTro().getValue()
            });
        }
    }

    private String formatCurrency(BigDecimal amount) {
        return String.format("%,.0f VND", amount);
    }

    private void selectRowToForm() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            txtMaND.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtHoTen.setText(tableModel.getValueAt(selectedRow, 1).toString());
            txtSDT.setText(tableModel.getValueAt(selectedRow, 2).toString());
            txtEmail.setText(tableModel.getValueAt(selectedRow, 3).toString());
            txtChucVu.setText(tableModel.getValueAt(selectedRow, 4).toString());
            txtLuong.setText(tableModel.getValueAt(selectedRow, 5).toString().replace(" VND", "").replace(",", ""));
            vaiTroCombo.setSelectedItem(tableModel.getValueAt(selectedRow, 6).toString());
        }
    }

    private void themNhanVien() {
        String hoTen = txtHoTen.getText();
        String sdt = txtSDT.getText();
        String email = txtEmail.getText();
        String chucVu = txtChucVu.getText();
        BigDecimal luong = new BigDecimal(txtLuong.getText());
        VaiTro vaiTro = VaiTro.fromString(vaiTroCombo.getSelectedItem().toString());

        if (!ValidationUtils.isValidString(hoTen))
            throw new IllegalArgumentException("Tên nhân viên không được để trống");
        if (!ValidationUtils.isValidEmail(email))
            throw new IllegalArgumentException("Email không hợp lệ");
        if (!ValidationUtils.isValidPhoneNumber(sdt))
            throw new IllegalArgumentException("Số điện thoại không hợp lệ (phải có 10 chữ số, bắt đầu từ 0)");
        if (!ValidationUtils.isPositiveBigDecimal(luong))
            throw new IllegalArgumentException("Lương phải là số dương!");

        NhanVien nhanVien = new NhanVien(0, hoTen, sdt, email, LoaiNguoiDung.NHANVIEN, chucVu, luong, vaiTro);
        controller.save(nhanVien);

        JOptionPane.showMessageDialog(this, "Thêm nhân viên thành công!");
        loadDataToTable();
        clearForm();
    }

    private void suaNhanVien() {
        if (txtMaND.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần sửa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int maND = Integer.parseInt(txtMaND.getText());
        String hoTen = txtHoTen.getText();
        String sdt = txtSDT.getText();
        String email = txtEmail.getText();
        String chucVu = txtChucVu.getText();
        BigDecimal luong = new BigDecimal(txtLuong.getText());
        VaiTro vaiTro = VaiTro.fromString(vaiTroCombo.getSelectedItem().toString());

        if (!ValidationUtils.isValidString(hoTen))
            throw new IllegalArgumentException("Tên nhân viên không được để trống");
        if (!ValidationUtils.isValidEmail(email))
            throw new IllegalArgumentException("Email không hợp lệ");
        if (!ValidationUtils.isValidPhoneNumber(sdt))
            throw new IllegalArgumentException("Số điện thoại không hợp lệ (phải có 10 chữ số, bắt đầu từ 0)");
        if (!ValidationUtils.isPositiveBigDecimal(luong))
            throw new IllegalArgumentException("Lương phải là số dương!");

        NhanVien nhanVien = new NhanVien(maND, hoTen, sdt, email, LoaiNguoiDung.NHANVIEN, chucVu, luong, vaiTro);
        controller.update(nhanVien);

        JOptionPane.showMessageDialog(this, "Cập nhật nhân viên thành công!");
        loadDataToTable();
    }

    private void xoaNhanVien() {
        if (txtMaND.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần xóa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int maND = Integer.parseInt(txtMaND.getText());
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa nhân viên này?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            controller.delete(maND);
            JOptionPane.showMessageDialog(this, "Xóa nhân viên thành công!");
            loadDataToTable();
            clearForm();
        }
    }

    private void searchNhanVien() {
        String tenNhanVien = tenNhanVienField.getText();
        if (!ValidationUtils.isValidString(tenNhanVien)) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên nhân viên!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<NhanVien> nhanVienList = controller.searchNhanVienByTen(tenNhanVien);
        tableModel.setRowCount(0);
        for (NhanVien nv : nhanVienList) {
            tableModel.addRow(new Object[]{
                    nv.getMaNguoiDung(),
                    nv.getHoTen(),
                    nv.getSoDienThoai(),
                    nv.getEmail(),
                    nv.getChucVu(),
                    formatCurrency(nv.getLuong()),
                    nv.getVaiTro().getValue()
            });
        }
    }

    private void clearForm() {
        txtMaND.setText("");
        txtHoTen.setText("");
        txtSDT.setText("");
        txtEmail.setText("");
        txtChucVu.setText("");
        txtLuong.setText("");
        vaiTroCombo.setSelectedIndex(0);
        table.clearSelection();
    }
}