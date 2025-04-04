package com.cinema.views;

import com.cinema.controllers.SuatChieuController;
import com.cinema.models.Phim;
import com.cinema.models.SuatChieu;
import com.cinema.services.SuatChieuService;
import com.cinema.utils.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class SuatChieuView extends JPanel {
    private DatabaseConnection databaseConnection;
    private SuatChieuController controller;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtMaSuatChieu, txtMaPhim, txtMaPhong, txtNgayGioChieu;
    private JButton btnThem, btnSua, btnXoa, btnClear;

    public SuatChieuView() {
        try {
            databaseConnection = new DatabaseConnection();
            controller = new SuatChieuController(new SuatChieuService(databaseConnection));
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
                "Mã Suất Chiếu", "Mã Phim", "Mã Phòng", "Ngày Giờ Chiếu"
        }, 0);

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectRowToForm();
            }
        });
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin Phim"));

        formPanel.add(new JLabel("Mã Suất Chiếu:"));
        txtMaSuatChieu = new JTextField();
        txtMaSuatChieu.setEditable(false);
        formPanel.add(txtMaSuatChieu);

        formPanel.add(new JLabel("Mã Phim:"));
        txtMaPhim = new JTextField();
        formPanel.add(txtMaPhim);

        formPanel.add(new JLabel("Mã Phòng:"));
        txtMaPhong = new JTextField();
        formPanel.add(txtMaPhong);

        formPanel.add(new JLabel("Ngày giờ chiếu:"));
        txtNgayGioChieu = new JTextField();
        formPanel.add(txtNgayGioChieu);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnThem = new JButton("Thêm");
        btnSua = new JButton("Sửa");
        btnXoa = new JButton("Xóa");
        btnClear = new JButton("Clear");

        buttonPanel.add(btnThem);
        buttonPanel.add(btnSua);
        buttonPanel.add(btnXoa);
        buttonPanel.add(btnClear);

        btnThem.addActionListener(e -> themSuatChieu());
        btnSua.addActionListener(e -> suaSuatChieu());
        btnXoa.addActionListener(e -> xoaSuatChieu());
        btnClear.addActionListener(e -> clearForm());

        add(scrollPane, BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(formPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadDataToTable() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        tableModel.setRowCount(0);
        List<SuatChieu> danhSach = controller.findAll();
        if (danhSach != null) {
            for (SuatChieu suatChieu : danhSach) {
                String ngayGioChieuFormatted = "Chưa có";
                if (suatChieu.getNgayGioChieu() != null) {
                    ngayGioChieuFormatted = suatChieu.getNgayGioChieu().format(formatter);
                }
                tableModel.addRow(new Object[]{
                        suatChieu.getMaSuatChieu(),
                        suatChieu.getMaPhim(),
                        suatChieu.getMaPhong(),
                        ngayGioChieuFormatted,
                });
            }
        }
    }

    private void selectRowToForm() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            txtMaSuatChieu.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtMaPhim.setText(tableModel.getValueAt(selectedRow, 1).toString());
            txtMaPhong.setText(tableModel.getValueAt(selectedRow, 2).toString());
            txtNgayGioChieu.setText(tableModel.getValueAt(selectedRow, 3).toString());
        }
    }

    private void themSuatChieu() {
        try {
            int maPhim = Integer.parseInt(txtMaPhim.getText());
            int maPhong = Integer.parseInt(txtMaPhong.getText());
            String ngayGioChieuStr = txtNgayGioChieu.getText();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            LocalDateTime ngayGioChieu = LocalDateTime.parse(ngayGioChieuStr, formatter);

            SuatChieu suatChieu = new SuatChieu(0, maPhim, maPhong, ngayGioChieu);
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
            int maSuatChieu = Integer.parseInt(maSuatChieuStr);
            int maPhim = Integer.parseInt(txtMaPhim.getText());
            int maPhong = Integer.parseInt(txtMaPhong.getText());
            String ngayGioChieuStr = txtNgayGioChieu.getText();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            LocalDateTime ngayGioChieu = LocalDateTime.parse(ngayGioChieuStr, formatter);

            SuatChieu suatChieu = new SuatChieu(maSuatChieu, maPhim, maPhong, ngayGioChieu);
            SuatChieu result = controller.updateSuatChieu(suatChieu);

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
        if (txtMaPhim.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn suất chiếu cần xóa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int maSuatChieu = Integer.parseInt(txtMaPhim.getText());
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
        txtMaPhim.setText("");
        txtMaPhong.setText("");
        txtNgayGioChieu.setText("");
        table.clearSelection();
    }
}
