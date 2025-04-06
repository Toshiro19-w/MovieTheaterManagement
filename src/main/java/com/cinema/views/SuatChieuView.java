package com.cinema.views;

import com.cinema.controllers.SuatChieuController;
import com.cinema.models.SuatChieu;
import com.cinema.services.SuatChieuService;
import com.cinema.utils.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class SuatChieuView extends JPanel {
    private SuatChieuController controller;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtMaSuatChieu, txtTenPhim, txtTenPhong,
            txtNgayGioChieu, txtThoiLuong, txtDinhDang;

    public SuatChieuView() {
        try {
            DatabaseConnection databaseConnection = new DatabaseConnection();
            controller = new SuatChieuController(new SuatChieuService(databaseConnection));
        } catch (IOException e) {
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
                "Mã Suất Chiếu", "Tên Phim", "Tên Phòng", "Ngày Giờ Chiếu", "Thời Lượng", "Định Dạng"
        }, 0);

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectRowToForm();
            }
        });
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin Phim"));

        formPanel.add(new JLabel("Mã Suất Chiếu:"));
        txtMaSuatChieu = new JTextField();
        txtMaSuatChieu.setEditable(false);
        formPanel.add(txtMaSuatChieu);

        formPanel.add(new JLabel("Tên Phim:"));
        txtTenPhim = new JTextField();
        formPanel.add(txtTenPhim);

        formPanel.add(new JLabel("Tên Phòng:"));
        txtTenPhong = new JTextField();
        formPanel.add(txtTenPhong);

        formPanel.add(new JLabel("Ngày Giờ Chiếu:"));
        txtNgayGioChieu = new JTextField();
        formPanel.add(txtNgayGioChieu);

        formPanel.add(new JLabel("Thời Lượng:"));
        txtThoiLuong = new JTextField();
        formPanel.add(txtThoiLuong);

        formPanel.add(new JLabel("Định dạng:"));
        txtDinhDang = new JTextField();
        formPanel.add(txtDinhDang);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnThem = new JButton("Thêm");
        JButton btnSua = new JButton("Sửa");
        JButton btnXoa = new JButton("Xóa");
        JButton btnClear = new JButton("Clear");

        buttonPanel.add(btnThem);
        buttonPanel.add(btnSua);
        buttonPanel.add(btnXoa);
        buttonPanel.add(btnClear);

        btnThem.addActionListener(_ -> themSuatChieu());
        btnSua.addActionListener(_ -> suaSuatChieu());
        btnXoa.addActionListener(_ -> xoaSuatChieu());
        btnClear.addActionListener(_ -> clearForm());

        add(scrollPane, BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(formPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadDataToTable() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        tableModel.setRowCount(0);
        List<SuatChieu> danhSach = controller.findAllDetail();
        if (danhSach != null) {
            for (SuatChieu suatChieu : danhSach) {
                String ngayGioChieuFormatted = "Chưa có";
                if (suatChieu.getNgayGioChieu() != null) {
                    ngayGioChieuFormatted = suatChieu.getNgayGioChieu().format(formatter);
                }
                tableModel.addRow(new Object[]{
                        suatChieu.getMaSuatChieu(),
                        suatChieu.getTenPhim(),
                        suatChieu.getTenPhong(),
                        ngayGioChieuFormatted,
                        suatChieu.getThoiLuongPhim(),
                        suatChieu.getThoiLuongPhim()
                });
            }
        }
    }

    private void selectRowToForm() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            txtMaSuatChieu.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtTenPhim.setText(tableModel.getValueAt(selectedRow, 1).toString());
            txtTenPhong.setText(tableModel.getValueAt(selectedRow, 2).toString());
            txtNgayGioChieu.setText(tableModel.getValueAt(selectedRow, 3).toString());
            txtThoiLuong.setText(tableModel.getValueAt(selectedRow, 4).toString());
            txtDinhDang.setText(tableModel.getValueAt(selectedRow, 5).toString());
        }
    }

    private void themSuatChieu() {
        try {
            String tenPhim = txtTenPhim.getText();
            String tenPhong = txtTenPhong.getText();
            String ngayGioChieuStr = txtNgayGioChieu.getText();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            LocalDateTime ngayGioChieu = LocalDateTime.parse(ngayGioChieuStr, formatter);
            int thoiLuong = Integer.parseInt(txtThoiLuong.getText());
            String dinhDang = txtDinhDang.getText();

            SuatChieu suatChieu = new SuatChieu(0, tenPhim, tenPhong, ngayGioChieu, thoiLuong, dinhDang);
            SuatChieu result = controller.saveSuatChieu(suatChieu);

            if (result != null) {
                JOptionPane.showMessageDialog(this, "Thêm suất chiếu thành công!");
                loadDataToTable();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm suất chiếu thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Mã thể loại và mã phòng là số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Ngày giờ chiếu không đúng định dạng (dd/MM/yyyy HH:mm:sss)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void suaSuatChieu() {
        String maSuatChieuStr = txtMaSuatChieu.getText();
        if (maSuatChieuStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn suất chiếu cần sửa (Mã suất chiếu không được trống)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String tenPhim = txtTenPhim.getText();
            String tenPhong = txtTenPhong.getText();
            String ngayGioChieuStr = txtNgayGioChieu.getText();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            LocalDateTime ngayGioChieu = LocalDateTime.parse(ngayGioChieuStr, formatter);
            int thoiLuong = Integer.parseInt(txtThoiLuong.getText());
            String dinhDang = txtDinhDang.getText();

            SuatChieu suatChieu = new SuatChieu(0, tenPhim, tenPhong, ngayGioChieu, thoiLuong, dinhDang);
            SuatChieu result = controller.saveSuatChieu(suatChieu);

            if (result != null) {
                JOptionPane.showMessageDialog(this, "Cập nhật suất chiếu thành công!");
                loadDataToTable();
                clearForm(); // Thường nên clear form sau khi sửa thành công
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật suất chiếu thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Mã suất chiếu, mã phim và mã phòng phải là số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Ngày giờ chiếu không đúng định dạng (dd/MM/yyyy HH:mm:ss)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xoaSuatChieu() {
        if (txtTenPhim.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn suất chiếu cần xóa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int maSuatChieu = Integer.parseInt(txtTenPhim.getText());
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa suất chiếu này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean result = controller.deleteSuatChieu(maSuatChieu);
            if (result) {
                JOptionPane.showMessageDialog(this, "Xóa suất chiếu thành công!");
                loadDataToTable();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa suất chiếu thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        txtMaSuatChieu.setText("");
        txtTenPhim.setText("");
        txtTenPhong.setText("");
        txtNgayGioChieu.setText("");
        table.clearSelection();
    }
}
