package com.cinema.views;

import com.cinema.controllers.VeController;
import com.cinema.models.*;
import com.cinema.services.VeService;
import com.cinema.utils.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class VeView extends JPanel {
    private DateTimeFormatter ngayDatFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private DateTimeFormatter ngayGioChieuFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private VeController controller;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField soGheField;
    private JTextField txtMaVe, txtGiaVe, txtSoGhe, txtNgayDat, txtTenPhong, txtTenPhim;
    private JFormattedTextField txtNgayGioChieu;
    private JComboBox<String> cbTrangThai;
    private JButton btnThem, btnSua, btnXoa, btnClear;
    private JLabel lblTenKhachHang, lblSoDienThoai, lblEmail;

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
        // Sidebar tìm kiếm (bên trái)
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setPreferredSize(new Dimension(200, 300));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Tìm kiếm vé"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        searchPanel.add(new JLabel("Số ghế:"), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        soGheField = new JTextField(15);
        searchPanel.add(soGheField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        JButton searchButton = new JButton("Tìm kiếm");
        searchButton.addActionListener(e -> searchVe());
        searchPanel.add(searchButton, gbc);

        add(searchPanel, BorderLayout.WEST);

        // Bảng danh sách vé
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
        add(scrollPane, BorderLayout.CENTER);

        // Sidebar khách hàng (bên phải)
        JPanel customerPanel = new JPanel(new GridBagLayout());
        customerPanel.setPreferredSize(new Dimension(200, 300));
        customerPanel.setBorder(BorderFactory.createTitledBorder("Thông tin khách hàng"));
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        customerPanel.add(new JLabel("Tên khách hàng:"), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        lblTenKhachHang = new JLabel("-");
        customerPanel.add(lblTenKhachHang, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        customerPanel.add(new JLabel("Số điện thoại:"), gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        lblSoDienThoai = new JLabel("-");
        customerPanel.add(lblSoDienThoai, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        customerPanel.add(new JLabel("Email:"), gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        lblEmail = new JLabel("-");
        customerPanel.add(lblEmail, gbc);

        add(customerPanel, BorderLayout.EAST);

        // Form nhập liệu (bên dưới)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin vé"));
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Mã vé
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Mã Vé:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        txtMaVe = new JTextField(10);
        txtMaVe.setEditable(false);
        formPanel.add(txtMaVe, gbc);

        // Trạng thái
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Trạng Thái:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        cbTrangThai = new JComboBox<>(new String[]{"BOOKED", "CANCELLED", "PAID"});
        formPanel.add(cbTrangThai, gbc);

        // Giá vé
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Giá Vé:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        txtGiaVe = new JTextField(10);
        formPanel.add(txtGiaVe, gbc);

        // Số ghế
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Số Ghế:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        txtSoGhe = new JTextField(10);
        formPanel.add(txtSoGhe, gbc);

        // Ngày đặt
        gbc.gridx = 2;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Ngày Đặt:"), gbc);

        gbc.gridx = 3;
        gbc.gridy = 0;
        txtNgayDat = new JTextField(10);
        txtNgayDat.setEditable(false);
        formPanel.add(txtNgayDat, gbc);

        // Tên phòng
        gbc.gridx = 2;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Tên Phòng:"), gbc);

        gbc.gridx = 3;
        gbc.gridy = 1;
        txtTenPhong = new JTextField(10);
        formPanel.add(txtTenPhong, gbc);

        // Ngày giờ chiếu
        gbc.gridx = 2;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Ngày Giờ Chiếu:"), gbc);

        gbc.gridx = 3;
        gbc.gridy = 2;
        txtNgayGioChieu = new JFormattedTextField(ngayGioChieuFormatter);
        txtNgayGioChieu.setColumns(10);
        formPanel.add(txtNgayGioChieu, gbc);

        // Tên phim
        gbc.gridx = 2;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Tên Phim:"), gbc);

        gbc.gridx = 3;
        gbc.gridy = 3;
        txtTenPhim = new JTextField(10);
        formPanel.add(txtTenPhim, gbc);

        // Nút thao tác
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

        // Panel dưới cùng
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
                        ve.getNgayDat() != null ? ve.getNgayDat().format(ngayDatFormatter) : "Chưa đặt",
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
            cbTrangThai.setSelectedItem(tableModel.getValueAt(selectedRow, 1).toString());
            txtGiaVe.setText(tableModel.getValueAt(selectedRow, 2).toString().replaceAll("[^\\d]", ""));
            txtSoGhe.setText(tableModel.getValueAt(selectedRow, 3).toString());
            txtNgayDat.setText(tableModel.getValueAt(selectedRow, 4).toString());
            txtTenPhong.setText(tableModel.getValueAt(selectedRow, 5).toString());
            txtNgayGioChieu.setText(tableModel.getValueAt(selectedRow, 6).toString().trim());
            txtTenPhim.setText(tableModel.getValueAt(selectedRow, 7).toString());

            // Cập nhật thông tin khách hàng
            updateCustomerInfo(Integer.parseInt(txtMaVe.getText()));
        }
    }

    private void updateCustomerInfo(int maVe) {
        KhachHang khachHang = controller.getKhachHangByMaVe(maVe);
        if (khachHang != null) {
            lblTenKhachHang.setText(khachHang.getHoTen());
            lblSoDienThoai.setText(khachHang.getSoDienThoai());
            lblEmail.setText(khachHang.getEmail());
        } else {
            lblTenKhachHang.setText("-");
            lblSoDienThoai.setText("-");
            lblEmail.setText("-");
        }
    }

    private String formatCurrency(BigDecimal amount) {
        DecimalFormat formatter = new DecimalFormat("#,### VND");
        return formatter.format(amount);
    }

    private BigDecimal parseCurrency(String currencyStr) {
        try {
            String cleanStr = currencyStr.replaceAll("[^\\d]", "");
            return new BigDecimal(cleanStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Giá vé không hợp lệ: " + currencyStr);
        }
    }

    private void themVe() {
        try {
            if (!validateForm()) {
                return;
            }

            TrangThaiVe trangThai = TrangThaiVe.valueOf(cbTrangThai.getSelectedItem().toString());
            String soGhe = txtSoGhe.getText();
            BigDecimal giaVe = parseCurrency(txtGiaVe.getText());
            LocalDateTime ngayDat = LocalDateTime.now();
            String tenPhong = txtTenPhong.getText();
            String ngayGioChieuStr = txtNgayGioChieu.getText().trim();
            LocalDateTime ngayGioChieu = LocalDateTime.parse(ngayGioChieuStr, ngayGioChieuFormatter);
            String tenPhim = txtTenPhim.getText();

            Ve ve = new Ve(0, trangThai, giaVe, soGhe, ngayDat, tenPhong, ngayGioChieu, tenPhim);
            Ve result = controller.saveVe(ve);

            if (result != null) {
                JOptionPane.showMessageDialog(this, "Thêm vé thành công!");
                loadDataToTable();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm vé thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Ngày giờ chiếu không hợp lệ! Vui lòng nhập theo định dạng dd/MM/yyyy HH:mm:ss", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void suaVe() {
        if (txtMaVe.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn vé cần sửa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            if (!validateForm()) {
                return;
            }

            int maVe = Integer.parseInt(txtMaVe.getText());
            TrangThaiVe trangThai = TrangThaiVe.valueOf(cbTrangThai.getSelectedItem().toString());
            String soGhe = txtSoGhe.getText();
            BigDecimal giaVe = parseCurrency(txtGiaVe.getText());
            LocalDateTime ngayDat = LocalDateTime.now();
            String tenPhong = txtTenPhong.getText();
            String ngayGioChieuStr = txtNgayGioChieu.getText().trim();
            LocalDateTime ngayGioChieu = LocalDateTime.parse(ngayGioChieuStr, ngayGioChieuFormatter);
            String tenPhim = txtTenPhim.getText();

            Ve ve = new Ve(maVe, trangThai, giaVe, soGhe, ngayDat, tenPhong, ngayGioChieu, tenPhim);
            Ve result = controller.updateVe(ve);

            if (result != null) {
                JOptionPane.showMessageDialog(this, "Cập nhật vé thành công!");
                loadDataToTable();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật vé thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Ngày giờ chiếu không hợp lệ! Vui lòng nhập theo định dạng dd/MM/yyyy HH:mm:ss", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validateForm() {
        if (!ValidationUtils.isValidString(txtSoGhe.getText())) {
            JOptionPane.showMessageDialog(this, "Số ghế không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!ValidationUtils.isValidString(txtTenPhong.getText())) {
            JOptionPane.showMessageDialog(this, "Tên phòng không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!ValidationUtils.isValidString(txtTenPhim.getText())) {
            JOptionPane.showMessageDialog(this, "Tên phim không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!ValidationUtils.isValidString(txtNgayGioChieu.getText())) {
            JOptionPane.showMessageDialog(this, "Ngày giờ chiếu không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void xoaVe() {
        if (txtMaVe.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn vé cần xóa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int maVe = Integer.parseInt(txtMaVe.getText());
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa vé này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean result = controller.deleteVe(maVe);
            if (result) {
                JOptionPane.showMessageDialog(this, "Xóa vé thành công!");
                loadDataToTable();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa vé thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
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
                    ve.getNgayDat() != null ? ve.getNgayDat().format(ngayDatFormatter) : "Chưa đặt",
                    ve.getTenPhong() != null ? ve.getTenPhong() : "Chưa đặt",
                    ve.getNgayGioChieu() != null ? ve.getNgayGioChieu().format(ngayGioChieuFormatter) : "Chưa có",
                    ve.getTenPhim()
            });
        }
    }

    private void clearForm() {
        txtMaVe.setText("");
        txtTenPhong.setText("");
        txtNgayGioChieu.setText("");
        txtSoGhe.setText("");
        txtTenPhim.setText("");
        txtGiaVe.setText("");
        cbTrangThai.setSelectedIndex(0);
        txtNgayDat.setText("");
        lblTenKhachHang.setText("-");
        lblSoDienThoai.setText("-");
        lblEmail.setText("-");
        table.clearSelection();
    }
}