package com.cinema.views;

import com.cinema.controllers.PhimController;
import com.cinema.controllers.VeController;
import com.cinema.models.TrangThaiVe;
import com.cinema.models.Ve;
import com.cinema.services.PhimService;
import com.cinema.services.VeService;
import com.cinema.utils.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class VeView extends JPanel {
    private DatabaseConnection databaseConnection;
    private VeController controller;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField, txtMaVe, txtMaSuatChieu, txtMaPhong, txtSoGhe,
            txtMaHoaDon, txtGiaVe, txtTrangThai, txtNgayDat;
    private JButton btnThem, btnSua, btnXoa, btnClear;

    public VeView() {
        try {
            databaseConnection = new DatabaseConnection();
            controller = new VeController(new VeService(databaseConnection));
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
                "Mã Vé", "Mã Suất Chiếu", "Mã Phòng", "Số Ghế", "Mã HĐ", "Giá Vé", "Trạng Thái", "Ngày Đặt"
        }, 0);

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectRowToForm();
            }
        });
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel formPanel = new JPanel(new GridLayout(8, 4, 5, 5)); // 8 hàng cho 8 trường
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin Vé"));

        formPanel.add(new JLabel("Mã Vé:"));
        txtMaVe = new JTextField();
        txtMaVe.setEditable(false);
        formPanel.add(txtMaVe);

        formPanel.add(new JLabel("Mã Suất Chiếu:"));
        txtMaSuatChieu = new JTextField();
        txtMaSuatChieu.setEditable(false);
        formPanel.add(txtMaSuatChieu);

        formPanel.add(new JLabel("Mã Phòng:"));
        txtMaPhong = new JTextField();
        txtMaPhong.setEditable(false);
        formPanel.add(txtMaPhong);

        formPanel.add(new JLabel("Số Ghế:"));
        txtSoGhe = new JTextField();
        formPanel.add(txtSoGhe);

        formPanel.add(new JLabel("Mã hoá đơn:"));
        txtMaHoaDon = new JTextField();
        txtMaHoaDon.setEditable(false);
        formPanel.add(txtMaHoaDon);

        formPanel.add(new JLabel("Giá Vé:"));
        txtGiaVe = new JTextField();
        formPanel.add(txtGiaVe);

        formPanel.add(new JLabel("Trạng Thái:"));
        txtTrangThai = new JTextField();
        formPanel.add(txtTrangThai);

        formPanel.add(new JLabel("Ngày đặt:"));
        txtNgayDat = new JTextField();
        formPanel.add(txtNgayDat);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnThem = new JButton("Thêm");
        btnSua = new JButton("Sửa");
        btnXoa = new JButton("Xóa");
        btnClear = new JButton("Clear");

        buttonPanel.add(btnThem);
        buttonPanel.add(btnSua);
        buttonPanel.add(btnXoa);
        buttonPanel.add(btnClear);

        btnThem.addActionListener(e -> themVe());
        btnSua.addActionListener(e -> suaVe());
        btnXoa.addActionListener(e -> xoaVe());
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
        List<Ve> danhSach = controller.findAll();
        if (danhSach != null) {
            for (Ve ve : danhSach) {
                tableModel.addRow(new Object[]{
                        ve.getMaVe(),
                        ve.getMaSuatChieu(),
                        ve.getMaPhong() != null ? ve.getMaPhong() : "Chưa đặt",
                        ve.getSoGhe(),
                        ve.getMaHoaDon() != null ? ve.getMaHoaDon() : "Chưa thanh toán",
                        formatCurrency(ve.getGiaVe()),
                        ve.getTrangThai().getValue(),
                        ve.getNgayDat() != null ? ve.getNgayDat().format(formatter) : "Chưa đặt"
                });
            }
        }
    }

    private void selectRowToForm() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            txtMaVe.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtMaSuatChieu.setText(tableModel.getValueAt(selectedRow, 1).toString());
            txtMaPhong.setText(tableModel.getValueAt(selectedRow, 2).toString());
            txtSoGhe.setText(tableModel.getValueAt(selectedRow, 3).toString());
            txtMaHoaDon.setText(tableModel.getValueAt(selectedRow, 4).toString());
            txtGiaVe.setText(tableModel.getValueAt(selectedRow, 5).toString());
            txtTrangThai.setText(tableModel.getValueAt(selectedRow, 6).toString());
            txtNgayDat.setText(tableModel.getValueAt(selectedRow, 7).toString());
        }
    }

    private String formatCurrency(BigDecimal amount) {
        return String.format("%,.0f VND", amount);
    }

    private void searchTickets() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mã vé.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        tableModel.setRowCount(0);
        List<Ve> veList = Collections.singletonList(controller.findVeById(Integer.parseInt(keyword)));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        veList.forEach(ve -> addVeToTable(ve, formatter));
    }

    private void addVeToTable(Ve ve, DateTimeFormatter formatter) {
    }

    private void themVe() {
        try {
            String soGhe = txtSoGhe.getText();
            BigDecimal giaVe = BigDecimal.valueOf(Long.parseLong(txtGiaVe.getText()));
            TrangThaiVe trangThai = TrangThaiVe.valueOf(txtTrangThai.getText());
            LocalDateTime ngayDat = LocalDateTime.now();

            Ve ve = new Ve(0, 0, 0, soGhe, 0, giaVe, trangThai, ngayDat);
            Ve result = controller.saveVe(ve);

            if (result != null) {
                JOptionPane.showMessageDialog(this, "Thêm vé thành công!");
                loadDataToTable();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm vé thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Giá vé phải là số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void suaVe() {
        if (txtMaVe.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn vé cần sửa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String soGhe = txtSoGhe.getText();
        BigDecimal giaVe = BigDecimal.valueOf(Long.parseLong(txtGiaVe.getText()));
        TrangThaiVe trangThai = TrangThaiVe.valueOf(txtTrangThai.getText());
        LocalDateTime ngayDat = LocalDateTime.parse(txtNgayDat.getText());

        Ve ve = new Ve(0, 0, 0, soGhe, 0, giaVe, trangThai, ngayDat);
        Ve result = controller.updateVe(ve);

        if (result != null) {
            JOptionPane.showMessageDialog(this, "Cập nhật phim thành công!");
            loadDataToTable();
        } else {
            JOptionPane.showMessageDialog(this, "Cập nhật phim thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xoaVe () {
        if (txtMaVe.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phim cần xóa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int maVe = Integer.parseInt(txtMaVe.getText());
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa phim này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean result = controller.deleteVe(maVe);
            if (result) {
                JOptionPane.showMessageDialog(this, "Xóa phim thành công!");
                loadDataToTable();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa phim thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm () {
        txtMaVe.setText("");
        txtMaSuatChieu.setText("");
        txtMaPhong.setText("");
        txtSoGhe.setText("");
        txtMaHoaDon.setText("");
        txtGiaVe.setText("");
        txtTrangThai.setText("");
        txtNgayDat.setText("");
        table.clearSelection();
    }
}