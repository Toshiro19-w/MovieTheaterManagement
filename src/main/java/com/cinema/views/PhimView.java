package com.cinema.views;

import com.cinema.controllers.PhimController;
import com.cinema.models.Phim;
import com.cinema.services.PhimService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class PhimView extends JFrame {
    private Connection conn;
    private final PhimController controller;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtMaPhim, txtTenPhim, txtTheLoai, txtThoiLuong,
            txtNgayKhoiChieu, txtNuocSanXuat, txtMoTa, txtDinhDang, txtDaoDien;
    private JButton btnThem, btnSua, btnXoa, btnClear;

    public PhimView() {
        this.controller = new PhimController(new PhimService(conn));
        initializeUI();
        loadDataToTable();
    }

    private void initializeUI() {
        setTitle("Quản lý Phim");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

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

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin Phim"));

        formPanel.add(new JLabel("Mã Phim:"));
        txtMaPhim = new JTextField();
        txtMaPhim.setEditable(false);
        formPanel.add(txtMaPhim);

        formPanel.add(new JLabel("Tên Phim:"));
        txtTenPhim = new JTextField();
        formPanel.add(txtTenPhim);

        formPanel.add(new JLabel("Thể Loại:"));
        txtTheLoai = new JTextField();
        formPanel.add(txtTheLoai);

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

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(formPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void loadDataToTable() {
        tableModel.setRowCount(0);
        List<Phim> danhSach = controller.findAll();
        if (danhSach != null) {
            for (Phim phim : danhSach) {
                tableModel.addRow(new Object[]{
                        phim.getMaPhim(),
                        phim.getTenPhim(),
                        phim.getMaTheLoai(),
                        phim.getThoiLuong(),
                        phim.getNgayKhoiChieu(),
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
        }
    }

    private void themPhim() {
        try {
            String tenPhim = txtTenPhim.getText();
            int maTheLoai = Integer.parseInt(txtTheLoai.getText());
            int thoiLuong = Integer.parseInt(txtThoiLuong.getText());
            LocalDate ngayKhoiChieu = LocalDate.parse(txtNgayKhoiChieu.getText());
            String nuocSanXuat = txtNuocSanXuat.getText();
            String dinhDang = txtDinhDang.getText();
            String moTa = txtMoTa.getText();
            String daoDien = txtDaoDien.getText();

            Phim phim = new Phim(0, tenPhim, maTheLoai, thoiLuong, ngayKhoiChieu, nuocSanXuat, dinhDang, moTa, daoDien);
            Phim result = controller.save(phim);

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
        LocalDate ngayKhoiChieu = LocalDate.parse(txtNgayKhoiChieu.getText());
        String nuocSanXuat = txtNuocSanXuat.getText();
        String dinhDang = txtDinhDang.getText();
        String moTa = txtMoTa.getText();
        String daoDien = txtDaoDien.getText();

        Phim phim = new Phim(0, tenPhim, maTheLoai, thoiLuong, ngayKhoiChieu, nuocSanXuat, dinhDang, moTa, daoDien);
        Phim result = controller.update(phim);

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
            boolean result = controller.delete(maPhim);
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
        table.clearSelection();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PhimView view = new PhimView();
            view.setVisible(true);
        });
    }
}