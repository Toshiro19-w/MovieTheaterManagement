package com.cinema.views;

import com.cinema.controllers.PhimController;
import com.cinema.models.Phim;
import com.cinema.services.PhimService;
import com.cinema.utils.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class PhimView extends JPanel {
    private DatabaseConnection databaseConnection;
    private PhimController controller;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtMaPhim, txtTenPhim, txtTheLoai, txtThoiLuong,
            txtNgayKhoiChieu, txtNuocSanXuat, txtMoTa, txtDinhDang, txtDaoDien;
    private JButton btnThem, btnSua, btnXoa, btnClear;

    public PhimView() {
        try {
            databaseConnection = new DatabaseConnection();
            controller = new PhimController(new PhimService(databaseConnection));
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Không thể đọc file cấu hình cơ sở dữ liệu!");
            System.exit(1);
        }
        initializeUI();
        loadDataToTable();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tableModel = new DefaultTableModel(new Object[]{
                "Mã Phim", "Tên Phim", "Mã Thể Loại", "Thời Lượng", "Ngày Khởi Chiếu", "Nước Sản Xuất", "Định Dạng", "Mô Tả", "Đạo Diễn"
        }, 0);

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectRowToForm();
            }
        });
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel formPanel = new JPanel(new GridLayout(9, 2, 5, 5)); // 9 hàng cho 9 trường
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin Phim"));

        formPanel.add(new JLabel("Mã Phim:"));
        txtMaPhim = new JTextField();
        txtMaPhim.setEditable(false);
        formPanel.add(txtMaPhim);

        formPanel.add(new JLabel("Tên Phim:"));
        txtTenPhim = new JTextField();
        formPanel.add(txtTenPhim);

        formPanel.add(new JLabel("Mã Thể Loại:"));
        txtTheLoai = new JTextField();
        formPanel.add(txtTheLoai);

        formPanel.add(new JLabel("Thời Lượng:"));
        txtThoiLuong = new JTextField();
        formPanel.add(txtThoiLuong);

        formPanel.add(new JLabel("Ngày Khởi Chiếu:"));
        txtNgayKhoiChieu = new JTextField();
        formPanel.add(txtNgayKhoiChieu);

        formPanel.add(new JLabel("Nước Sản Xuất:"));
        txtNuocSanXuat = new JTextField();
        formPanel.add(txtNuocSanXuat);

        formPanel.add(new JLabel("Định Dạng:"));
        txtDinhDang = new JTextField();
        formPanel.add(txtDinhDang);

        formPanel.add(new JLabel("Mô Tả:"));
        txtMoTa = new JTextField();
        formPanel.add(txtMoTa);

        formPanel.add(new JLabel("Đạo Diễn:"));
        txtDaoDien = new JTextField();
        formPanel.add(txtDaoDien);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnThem = new JButton("Thêm");
        btnSua = new JButton("Sửa");
        btnXoa = new JButton("Xóa");
        btnClear = new JButton("Clear");

        buttonPanel.add(btnThem);
        buttonPanel.add(btnSua);
        buttonPanel.add(btnXoa);
        buttonPanel.add(btnClear);

        btnThem.addActionListener(e -> themPhim());
        btnSua.addActionListener(e -> suaPhim());
        btnXoa.addActionListener(e -> xoaPhim());
        btnClear.addActionListener(e -> clearForm());

        add(scrollPane, BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(formPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadDataToTable() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        tableModel.setRowCount(0);
        List<Phim> danhSach = controller.findAll();
        if (danhSach != null) {
            for (Phim phim : danhSach) {
                tableModel.addRow(new Object[]{
                        phim.getMaPhim(),
                        phim.getTenPhim(),
                        phim.getMaTheLoai(),
                        phim.getThoiLuong(),
                        phim.getNgayKhoiChieu() != null ? phim.getNgayKhoiChieu().format(formatter) : "Chưa có",
                        phim.getNuocSanXuat(),
                        phim.getDinhDang(),
                        phim.getMoTa(),
                        phim.getDaoDien()
                });
            }
        }
    }

    private void selectRowToForm() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            txtMaPhim.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtTenPhim.setText(tableModel.getValueAt(selectedRow, 1).toString());
            txtTheLoai.setText(tableModel.getValueAt(selectedRow, 2).toString());
            txtThoiLuong.setText(tableModel.getValueAt(selectedRow, 3).toString());
            txtNgayKhoiChieu.setText(tableModel.getValueAt(selectedRow, 4).toString());
            txtNuocSanXuat.setText(tableModel.getValueAt(selectedRow, 5).toString());
            txtDinhDang.setText(tableModel.getValueAt(selectedRow, 6).toString());
            txtMoTa.setText(tableModel.getValueAt(selectedRow, 7).toString());
            txtDaoDien.setText(tableModel.getValueAt(selectedRow, 8).toString());
        }
    }

    private void themPhim() {
        try {
            String tenPhim = txtTenPhim.getText();
            int maTheLoai = Integer.parseInt(txtTheLoai.getText());
            int thoiLuong = Integer.parseInt(txtThoiLuong.getText());
            String ngayKhoiChieuStr = txtNgayKhoiChieu.getText();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate ngayKhoiChieu = LocalDate.parse(ngayKhoiChieuStr, formatter);
            String nuocSanXuat = txtNuocSanXuat.getText();
            String dinhDang = txtDinhDang.getText();
            String moTa = txtMoTa.getText();
            String daoDien = txtDaoDien.getText();

            Phim phim = new Phim(0, tenPhim, maTheLoai, thoiLuong, ngayKhoiChieu, nuocSanXuat, dinhDang, moTa, daoDien);
            Phim result = controller.savePhim(phim);

            if (result != null) {
                JOptionPane.showMessageDialog(this, "Thêm phim thành công!");
                loadDataToTable();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm phim thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Mã thể loại và thời lượng phải là số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Ngày khởi chiếu không đúng định dạng (yyyy-MM-dd)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void suaPhim() {
        if (txtMaPhim.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phim cần sửa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String tenPhim = txtTenPhim.getText();
        int maTheLoai = Integer.parseInt(txtTheLoai.getText());
        int thoiLuong = Integer.parseInt(txtThoiLuong.getText());
        String ngayKhoiChieuStr = txtNgayKhoiChieu.getText();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate ngayKhoiChieu = LocalDate.parse(ngayKhoiChieuStr, formatter);
        String nuocSanXuat = txtNuocSanXuat.getText();
        String dinhDang = txtDinhDang.getText();
        String moTa = txtMoTa.getText();
        String daoDien = txtDaoDien.getText();

        Phim phim = new Phim(0, tenPhim, maTheLoai, thoiLuong, ngayKhoiChieu, nuocSanXuat, dinhDang, moTa, daoDien);
        Phim result = controller.updatePhim(phim);

        if (result != null) {
            JOptionPane.showMessageDialog(this, "Cập nhật phim thành công!");
            loadDataToTable();
        } else {
            JOptionPane.showMessageDialog(this, "Cập nhật phim thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xoaPhim() {
        if (txtMaPhim.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phim cần xóa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int maPhim = Integer.parseInt(txtMaPhim.getText());
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa phim này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean result = controller.deletePhim(maPhim);
            if (result) {
                JOptionPane.showMessageDialog(this, "Xóa phim thành công!");
                loadDataToTable();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa phim thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        txtMaPhim.setText("");
        txtTenPhim.setText("");
        txtTheLoai.setText("");
        txtThoiLuong.setText("");
        txtNgayKhoiChieu.setText("");
        txtNuocSanXuat.setText("");
        txtDinhDang.setText("");
        txtMoTa.setText("");
        txtDaoDien.setText("");
        table.clearSelection();
    }
}