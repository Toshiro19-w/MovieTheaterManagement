package com.cinema.views;

import com.cinema.controllers.VeController;
import com.cinema.models.TrangThaiVe;
import com.cinema.models.Ve;
import com.cinema.services.VeService;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.ValidationUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class VeView extends JPanel {
    private DateTimeFormatter ngayDatformatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private DateTimeFormatter ngayGioChieuFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private VeController controller;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField soGheField;
    private JTextField searchField, txtMaVe, txtTrangThai, txtGiaVe, txtSoGhe,
            txtNgayDat, txtTenPhong, txtNgayGioChieu, txtTenPhim;
    private JButton btnThem, btnSua, btnXoa, btnClear;

    public VeView() {
        try {
            DatabaseConnection databaseConnection = new DatabaseConnection();
            controller = new VeController(new VeService(databaseConnection));
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
        JPanel searchPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        searchPanel.setPreferredSize(new Dimension(200, 300));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Tìm kiếm vé"));
        searchPanel.add(new JLabel("Số ghế:"));
        soGheField = new JTextField();
        searchPanel.add(soGheField);

        JButton searchButton = new JButton("Tìm kiếm");
        searchButton.addActionListener(e -> searchVe());
        searchPanel.add(searchButton);

        add(searchPanel, BorderLayout.WEST);

        tableModel = new DefaultTableModel(new Object[]{
                "Mã Vé", "Trạng Thái", "Giá Vé", "Số Ghế",
                "Ngày Đặt", "Tên Phòng", "Ngày Giờ Chiếu", "Tên Phim"
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

        formPanel.add(new JLabel("Trạng Thái:"));
        txtTrangThai = new JTextField();
        formPanel.add(txtTrangThai);

        formPanel.add(new JLabel("Giá Vé:"));
        txtGiaVe = new JTextField();
        formPanel.add(txtGiaVe);

        formPanel.add(new JLabel("Số Ghế:"));
        txtSoGhe = new JTextField();
        formPanel.add(txtSoGhe);

        formPanel.add(new JLabel("Ngày Đặt:"));
        txtNgayDat = new JTextField();
        txtNgayDat.setEditable(false);
        formPanel.add(txtNgayDat);

        formPanel.add(new JLabel("Tên Phòng:"));
        txtTenPhong = new JTextField();
        formPanel.add(txtTenPhong);

        formPanel.add(new JLabel("Ngày Giờ Chiếu:"));
        txtNgayGioChieu = new JTextField();
        formPanel.add(txtNgayGioChieu);

        formPanel.add(new JLabel("Tên Phim:"));
        txtTenPhim = new JTextField();
        formPanel.add(txtTenPhim);

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
        tableModel.setRowCount(0);
        List<Ve> danhSach = controller.findAllDetail();
        if (danhSach != null) {
            for (Ve ve : danhSach) {
                tableModel.addRow(new Object[]{
                        ve.getMaVe(),
                        ve.getTrangThai().getValue(),
                        formatCurrency(ve.getGiaVe()),
                        ve.getSoGhe(),
                        ve.getNgayDat() != null ? ve.getNgayDat().format(ngayDatformatter) : "Chưa đặt",
                        ve.getTenPhong() != null ? ve.getTenPhong() : "Chưa đặt",
                        ve.getNgayGioChieu() != null ? ve.getNgayGioChieu().format(ngayGioChieuFormatter) : "Chưa có",
                        ve.getTenPhim()
                });
            }
        }
    }

    private void selectRowToForm() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            txtMaVe.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtTrangThai.setText(tableModel.getValueAt(selectedRow, 1).toString());
            txtGiaVe.setText(tableModel.getValueAt(selectedRow, 2).toString());
            txtSoGhe.setText(tableModel.getValueAt(selectedRow, 3).toString());
            txtNgayDat.setText(tableModel.getValueAt(selectedRow, 4).toString());
            txtTenPhong.setText(tableModel.getValueAt(selectedRow, 5).toString());
            txtNgayGioChieu.setText(tableModel.getValueAt(selectedRow, 6).toString());
            txtTenPhim.setText(tableModel.getValueAt(selectedRow, 7).toString());
        }
    }

    private String formatCurrency(BigDecimal amount) {
        return String.format("%,.0f VND", amount);
    }

    private void themVe() {
        TrangThaiVe trangThai = TrangThaiVe.valueOf(txtTrangThai.getText());
        String soGhe = txtSoGhe.getText();
        BigDecimal giaVe = BigDecimal.valueOf(Long.parseLong(txtGiaVe.getText()));
        LocalDateTime ngayDat = LocalDateTime.now();
        String tenPhong = txtTenPhong.getText();
        String ngayGioChieuStr = txtNgayGioChieu.getText();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalDateTime ngayGioChieu = LocalDateTime.parse(ngayGioChieuStr, formatter);
        String tenPhim = txtTenPhim.getText();

        Ve ve = new Ve(0, trangThai, giaVe, soGhe, ngayDat, tenPhong, LocalDateTime.parse(ngayGioChieuStr), tenPhim);
        Ve result = controller.saveVe(ve);

        if (result != null) {
            JOptionPane.showMessageDialog(this, "Thêm vé thành công!");
            loadDataToTable();
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, "Thêm vé thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void suaVe() {
        if (txtMaVe.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn vé cần sửa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        TrangThaiVe trangThai = TrangThaiVe.valueOf(txtTrangThai.getText());
        String soGhe = txtSoGhe.getText();
        BigDecimal giaVe = BigDecimal.valueOf(Long.parseLong(txtGiaVe.getText()));
        LocalDateTime ngayDat = LocalDateTime.now();
        String tenPhong = txtTenPhong.getText();
        String ngayGioChieuStr = txtNgayGioChieu.getText();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalDateTime ngayGioChieu = LocalDateTime.parse(ngayGioChieuStr, formatter);
        String tenPhim = txtTenPhim.getText();

        Ve ve = new Ve(0, trangThai, giaVe, soGhe, ngayDat, tenPhong, LocalDateTime.parse(ngayGioChieuStr), tenPhim);
        Ve result = controller.saveVe(ve);

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

    private void searchVe() {
        String soGhe = soGheField.getText();
        if (!ValidationUtils.isValidString(soGhe)) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số ghế!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            loadDataToTable();
            return;
        }

        List<Ve> veList = controller.searchVeBySoGhe(soGhe);
        tableModel.setRowCount(0);
        for (Ve ve : veList) {
            tableModel.addRow(new Object[]{
                    ve.getMaVe(),
                    ve.getTrangThai().getValue(),
                    formatCurrency(ve.getGiaVe()),
                    ve.getSoGhe(),
                    ve.getNgayDat() != null ? ve.getNgayDat().format(ngayDatformatter) : "Chưa đặt",
                    ve.getTenPhong() != null ? ve.getTenPhong() : "Chưa đặt",
                    ve.getNgayGioChieu() != null ? ve.getNgayGioChieu().format(ngayGioChieuFormatter) : "Chưa có",
                    ve.getTenPhim()
            });
        }
    }

    private void clearForm () {
        txtMaVe.setText("");
        txtTenPhong.setText("");
        txtNgayGioChieu.setText("");
        txtSoGhe.setText("");
        txtTenPhim.setText("");
        txtGiaVe.setText("");
        txtTrangThai.setText("");
        txtNgayDat.setText("");
        table.clearSelection();
    }
}