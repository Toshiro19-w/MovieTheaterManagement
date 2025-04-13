package com.cinema.views;

import com.cinema.controllers.PhimController;
import com.cinema.models.Phim;
import com.cinema.services.PhimService;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.ValidationUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class PhimView extends JPanel {
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private DatabaseConnection databaseConnection;
    private PhimController controller;
    private JTable table;
    private JTextField tenPhimField;
    private DefaultTableModel tableModel;
    private JTextField txtMaPhim, txtTenPhim, txtTenTheLoai, txtThoiLuong,
            txtNgayKhoiChieu, txtNuocSanXuat, txtMoTa, txtDinhDang, txtDaoDien, txtSoSuatChieu;
    private JButton btnThem, btnSua, btnXoa, btnClear;

    public PhimView() {
        try {
            databaseConnection = new DatabaseConnection();
            controller = new PhimController(new PhimService(databaseConnection));
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

    private static JPanel getJPanel() {
        JPanel searchPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        searchPanel.setOpaque(false);
        searchPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        searchPanel.setLayout(new GridBagLayout());
        searchPanel.setPreferredSize(new Dimension(300, 0));
        return searchPanel;
    }

    private void initializeUI() {
        JPanel searchPanel = getJPanel();
        // Set bố cục
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Tiêu đề
        JLabel titleLabel = new JLabel("Tìm Kiếm Phim", SwingConstants.HORIZONTAL);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        searchPanel.add(titleLabel, gbc);

        // Ô nhập
        gbc.gridx = 1;
        gbc.gridy++;
        tenPhimField = new JTextField();
        tenPhimField.setFont(new Font("Arial", Font.PLAIN, 14));
        setPlaceholder(tenPhimField, "Nhập tên phim...");
        searchPanel.add(tenPhimField, gbc);

        // Nút tìm kiếm
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        JButton searchBtn = new JButton("Tìm");
        searchBtn.setFont(new Font("Arial", Font.BOLD, 14));
        searchBtn.setBackground(new Color(0, 102, 204));
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFocusPainted(false);
        searchBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        searchBtn.addActionListener(_ -> searchPhim());
        searchPanel.add(searchBtn, gbc);

        add(searchPanel, BorderLayout.WEST);

        tableModel = new DefaultTableModel(new Object[]{
                "Mã Phim", "Tên Phim", "Tên Thể Loại", "Thời Lượng", "Ngày Khởi Chiếu",
                "Nước Sản Xuất", "Định Dạng", "Mô Tả", "Đạo Diễn", "Số Suất Chiếu"
        }, 0);

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectRowToForm();
            }
        });
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel formPanel = new JPanel(new GridLayout(10, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin Phim"));

        formPanel.add(new JLabel("Mã Phim:"));
        txtMaPhim = new JTextField();
        txtMaPhim.setEditable(false);
        formPanel.add(txtMaPhim);

        formPanel.add(new JLabel("Tên Phim:"));
        txtTenPhim = new JTextField();
        formPanel.add(txtTenPhim);

        formPanel.add(new JLabel("Tên Thể Loại:"));
        txtTenTheLoai = new JTextField();
        formPanel.add(txtTenTheLoai);

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

        formPanel.add(new JLabel("Số Suất Chiếu:"));
        txtSoSuatChieu = new JTextField();
        txtSoSuatChieu.setEditable(false);
        formPanel.add(txtSoSuatChieu);

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
        tableModel.setRowCount(0);
        List<Phim> danhSach = controller.findAllDetail();
        if (danhSach != null) {
            for (Phim phim : danhSach) {
                tableModel.addRow(new Object[]{
                        phim.getMaPhim(),
                        phim.getTenPhim(),
                        phim.getTenTheLoai(),
                        phim.getThoiLuong() + " phút",
                        phim.getNgayKhoiChieu() != null ? phim.getNgayKhoiChieu().format(formatter) : "Chưa có",
                        phim.getNuocSanXuat(),
                        phim.getDinhDang(),
                        phim.getMoTa(),
                        phim.getDaoDien(),
                        phim.getSoSuatChieu()
                });
            }
        }
    }

    private void selectRowToForm() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            txtMaPhim.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtTenPhim.setText(tableModel.getValueAt(selectedRow, 1).toString());
            txtTenTheLoai.setText(tableModel.getValueAt(selectedRow, 2).toString());
            txtThoiLuong.setText(tableModel.getValueAt(selectedRow, 3).toString());
            txtNgayKhoiChieu.setText(tableModel.getValueAt(selectedRow, 4).toString());
            txtNuocSanXuat.setText(tableModel.getValueAt(selectedRow, 5).toString());
            txtDinhDang.setText(tableModel.getValueAt(selectedRow, 6).toString());
            txtMoTa.setText(tableModel.getValueAt(selectedRow, 7).toString());
            txtDaoDien.setText(tableModel.getValueAt(selectedRow, 8).toString());
            txtSoSuatChieu.setText(tableModel.getValueAt(selectedRow, 9).toString());
        }
    }

    private void themPhim() {
        String tenPhim = txtTenPhim.getText();
        String tenTheLoai = txtTenTheLoai.getText();
        int thoiLuong = Integer.parseInt(txtThoiLuong.getText());
        String ngayKhoiChieuStr = txtNgayKhoiChieu.getText();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate ngayKhoiChieu = LocalDate.parse(ngayKhoiChieuStr, formatter);
        String nuocSanXuat = txtNuocSanXuat.getText();
        String dinhDang = txtDinhDang.getText();
        String moTa = txtMoTa.getText();
        String daoDien = txtDaoDien.getText();
        int soSuatChieu = Integer.parseInt(txtSoSuatChieu.getText());

        if (!ValidationUtils.isValidString(tenPhim)) {
            throw new IllegalArgumentException("Tên phim không được để trống");
        }
        ValidationUtils.validatePositive(thoiLuong, "Thời lượng phải là số dương");

        Phim phim = new Phim(0, tenPhim, tenTheLoai, thoiLuong, ngayKhoiChieu, nuocSanXuat, dinhDang, moTa, daoDien, soSuatChieu);
        Phim result = controller.savePhim(phim);

        if (result != null) {
            JOptionPane.showMessageDialog(this, "Thêm phim thành công!");
            loadDataToTable();
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, "Thêm phim thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void suaPhim() {
        if (txtMaPhim.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phim cần sửa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String tenPhim = txtTenPhim.getText();
        String tenTheLoai = txtTenTheLoai.getText();
        int thoiLuong = Integer.parseInt(txtThoiLuong.getText());
        String ngayKhoiChieuStr = txtNgayKhoiChieu.getText();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate ngayKhoiChieu = LocalDate.parse(ngayKhoiChieuStr, formatter);
        String nuocSanXuat = txtNuocSanXuat.getText();
        String dinhDang = txtDinhDang.getText();
        String moTa = txtMoTa.getText();
        String daoDien = txtDaoDien.getText();
        int soSuatChieu = Integer.parseInt(txtSoSuatChieu.getText());

        if (!ValidationUtils.isValidString(tenPhim)) {
            throw new IllegalArgumentException("Tên phim không được để trống");
        }
        ValidationUtils.validatePositive(thoiLuong, "Thời lượng phải là số dương");

        Phim phim = new Phim(0, tenPhim, tenTheLoai, thoiLuong, ngayKhoiChieu, nuocSanXuat, dinhDang, moTa, daoDien, soSuatChieu);
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

    private void searchPhim() {
        try {
            String tenPhim = tenPhimField.getText();
            if (!ValidationUtils.isValidString(tenPhim)) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập tên phim!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                loadDataToTable();
                return;
            }

            List<Phim> phimList = controller.searchPhimByTen(tenPhim);
            tableModel.setRowCount(0);
            for (Phim phim : phimList) {
                tableModel.addRow(new Object[]{
                        phim.getMaPhim(),
                        phim.getTenPhim(),
                        phim.getTenTheLoai(),
                        phim.getThoiLuong() + " phút",
                        phim.getNgayKhoiChieu() != null ? phim.getNgayKhoiChieu().format(formatter) : "Chưa có",
                        phim.getNuocSanXuat(),
                        phim.getDinhDang(),
                        phim.getMoTa(),
                        phim.getDaoDien(),
                        phim.getSoSuatChieu()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tìm kiếm phim: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        txtMaPhim.setText("");
        txtTenPhim.setText("");
        txtTenTheLoai.setText("");
        txtThoiLuong.setText("");
        txtNgayKhoiChieu.setText("");
        txtNuocSanXuat.setText("");
        txtDinhDang.setText("");
        txtMoTa.setText("");
        txtDaoDien.setText("");
        txtSoSuatChieu.setText("");
        table.clearSelection();
    }

    public void setPlaceholder(JTextField field, String placeholder) {
        field.setText(placeholder);
        field.setForeground(Color.GRAY);

        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }

            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(Color.GRAY);
                    field.setText(placeholder);
                }
            }
        });
    }
}