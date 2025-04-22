package com.cinema.views;

import com.cinema.controllers.PhimController;
import com.cinema.utils.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;

public class PhimView extends JPanel {
    private DatabaseConnection databaseConnection;
    private JTextField txtSearchTenPhim;
    private JTextField txtTenPhim, txtTenTheLoai, txtThoiLuong,
            txtNgayKhoiChieu, txtNuocSanXuat, txtDinhDang, txtMoTa,
            txtDaoDien, txtSearchTenTheLoai, txtSearchNuocSanXuat, txtSearchDaoDien;
    private JLabel txtMaPhim, posterLabel;
    private JButton btnChonAnh; // Nút chọn ảnh
    private String selectedPosterPath; // Lưu tên file ảnh đã chọn
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnThem, btnSua, btnXoa, btnClear;

    public PhimView() throws SQLException {
        try {
            databaseConnection = new DatabaseConnection();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Không thể đọc file cấu hình cơ sở dữ liệu!");
            return;
        }

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Phần tìm kiếm
        JPanel searchPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        searchPanel.setBorder(BorderFactory.createTitledBorder("TÌM KIẾM"));
        searchPanel.add(new JLabel("Tên Phim:"));
        txtSearchTenPhim = new JTextField();
        searchPanel.add(txtSearchTenPhim);
        searchPanel.add(new JLabel("Thể Loại:"));
        txtSearchTenTheLoai = new JTextField();
        searchPanel.add(txtSearchTenTheLoai);
        searchPanel.add(new JLabel("Nước Sản Xuất:"));
        txtSearchNuocSanXuat = new JTextField();
        searchPanel.add(txtSearchNuocSanXuat);
        searchPanel.add(new JLabel("Đạo diễn:"));
        txtSearchDaoDien = new JTextField();
        searchPanel.add(txtSearchDaoDien);

        // Phần thông tin phim
        JPanel infoPanel = new JPanel(new BorderLayout(10, 10));
        infoPanel.setBorder(BorderFactory.createTitledBorder("THÔNG TIN PHIM"));

        // Panel chứa các trường thông tin
        JPanel fieldsPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        fieldsPanel.add(new JLabel("Mã Phim:"));
        txtMaPhim = new JLabel();
        fieldsPanel.add(txtMaPhim);

        fieldsPanel.add(new JLabel("Tên Phim:"));
        txtTenPhim = new JTextField();
        fieldsPanel.add(txtTenPhim);

        fieldsPanel.add(new JLabel("Thể Loại:"));
        txtTenTheLoai = new JTextField();
        fieldsPanel.add(txtTenTheLoai);

        fieldsPanel.add(new JLabel("Thời Lượng:"));
        txtThoiLuong = new JTextField();
        fieldsPanel.add(txtThoiLuong);

        fieldsPanel.add(new JLabel("Ngày Khởi Chiếu:"));
        txtNgayKhoiChieu = new JTextField();
        fieldsPanel.add(txtNgayKhoiChieu);

        fieldsPanel.add(new JLabel("Nước Sản Xuất:"));
        txtNuocSanXuat = new JTextField();
        fieldsPanel.add(txtNuocSanXuat);

        fieldsPanel.add(new JLabel("Định Dạng:"));
        txtDinhDang = new JTextField();
        fieldsPanel.add(txtDinhDang);

        fieldsPanel.add(new JLabel("Mô Tả:"));
        txtMoTa = new JTextField();
        fieldsPanel.add(txtMoTa);

        fieldsPanel.add(new JLabel("Đạo Diễn:"));
        txtDaoDien = new JTextField();
        fieldsPanel.add(txtDaoDien);

        // Panel chứa nút chọn ảnh
        JPanel chonAnhPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnChonAnh = new JButton("Chọn Ảnh");
        btnChonAnh.addActionListener(e -> chonAnh());
        chonAnhPanel.add(btnChonAnh);
        fieldsPanel.add(chonAnhPanel);

        // Thêm posterLabel để hiển thị ảnh
        posterLabel = new JLabel();
        posterLabel.setPreferredSize(new Dimension(150, 200));
        posterLabel.setHorizontalAlignment(SwingConstants.CENTER);
        posterLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        // Kết hợp fieldsPanel và posterLabel
        infoPanel.add(fieldsPanel, BorderLayout.CENTER);
        infoPanel.add(posterLabel, BorderLayout.EAST);

        // Kết hợp searchPanel và infoPanel
        JPanel northPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        northPanel.add(searchPanel);
        northPanel.add(infoPanel);

        // Phần bảng danh sách phim
        String[] columns = {"Mã Phim", "Tên Phim", "Thể Loại", "Thời Lượng", "Ngày Khởi Chiếu", "Nước Sản Xuất", "Định Dạng", "Mô Tả", "Đạo Diễn"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("DANH SÁCH PHIM"));

        // Phần nút thao tác
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnThem = new JButton("THÊM");
        btnSua = new JButton("SỬA");
        btnXoa = new JButton("XÓA");
        btnClear = new JButton("CLEAR");
        buttonPanel.add(btnThem);
        buttonPanel.add(btnSua);
        buttonPanel.add(btnXoa);
        buttonPanel.add(btnClear);

        // Sắp xếp layout
        add(northPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH); // Sửa "bottomPanel" thành "buttonPanel"

        // Khởi tạo controller
        new PhimController(this);
    }

    private void chonAnh() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn ảnh phim");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Image files", "jpg", "png", "jpeg", "gif"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            selectedPosterPath = selectedFile.getName(); // Chỉ lưu tên file
            try {
                // Sao chép file vào resources/images/posters/
                Path source = selectedFile.toPath();
                Path target = Paths.get("src/main/resources/images/posters/" + selectedPosterPath);
                Files.createDirectories(target.getParent()); // Tạo thư mục nếu chưa tồn tại
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);

                // Hiển thị ảnh trong posterLabel
                ImageIcon posterIcon = new ImageIcon(selectedFile.getPath());
                Image scaledImage = posterIcon.getImage().getScaledInstance(150, 200, Image.SCALE_SMOOTH);
                posterLabel.setIcon(new ImageIcon(scaledImage));
                posterLabel.setText("");
            } catch (IOException e) {
                e.printStackTrace();
                posterLabel.setIcon(null);
                posterLabel.setText("Không thể tải ảnh");
            }
        }
    }

    // Getters
    public DatabaseConnection getDatabaseConnection() { return databaseConnection; }
    public JTextField getTxtSearchTenPhim() { return txtSearchTenPhim; }
    public JLabel getTxtMaPhim() { return txtMaPhim; }
    public JTextField getTxtTenPhim() { return txtTenPhim; }
    public JTextField getTxtTenTheLoai() { return txtTenTheLoai; }
    public JTextField getTxtThoiLuong() { return txtThoiLuong; }
    public JTextField getTxtNgayKhoiChieu() { return txtNgayKhoiChieu; }
    public JTextField getTxtNuocSanXuat() { return txtNuocSanXuat; }
    public JTextField getTxtDinhDang() { return txtDinhDang; }
    public JTextField getTxtMoTa() { return txtMoTa; }
    public JTextField getTxtDaoDien() { return txtDaoDien; }
    public JTextField getTxtSearchTenTheLoai() { return txtSearchTenTheLoai; }
    public JTextField getTxtSearchNuocSanXuat() { return txtSearchNuocSanXuat; }
    public JTextField getTxtSearchDaoDien() { return txtSearchDaoDien; }
    public JLabel getPosterLabel() { return posterLabel; }
    public String getSelectedPosterPath() { return selectedPosterPath; }
    public void clearSelectedPosterPath() { selectedPosterPath = null; }
    public JTable getTable() { return table; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JButton getBtnThem() { return btnThem; }
    public JButton getBtnSua() { return btnSua; }
    public JButton getBtnXoa() { return btnXoa; }
    public JButton getBtnClear() { return btnClear; }
}