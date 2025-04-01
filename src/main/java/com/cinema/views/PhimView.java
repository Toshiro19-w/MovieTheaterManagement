package com.cinema.views;

import com.cinema.controllers.PhimController;
import com.cinema.models.Phim;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PhimView extends JFrame {
    private DefaultTableModel tableModel;
    private PhimController phimController;
    private JTextField searchField;

    public PhimView() {
        phimController = new PhimController();
        setTitle("Quản lý Phim");
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Bảng hiển thị danh sách phim
        tableModel = new DefaultTableModel(new String[]{
                "Mã Phim", "Tên Phim", "Mã Thể Loại", "Thời Lượng", "Ngày khởi chiếu",
                "Nước sản xuất", "Định dạng", "Mô tả", "Đạo diễn"}, 0);
        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        // Panel chứa nút và ô nhập tìm kiếm
        JPanel controlPanel = new JPanel();
        searchField = new JTextField(15);
        JButton btnLoad = new JButton("Tải Danh Sách Phim");
        JButton btnSearch = new JButton("Tìm Kiếm");

        btnLoad.addActionListener(e -> loadMovies());
        btnSearch.addActionListener(e -> searchMovies());

        controlPanel.add(btnLoad);
        controlPanel.add(new JLabel("Nhập tên phim:"));
        controlPanel.add(searchField);
        controlPanel.add(btnSearch);

        add(scrollPane, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
    }

    private void loadMovies() {
        tableModel.setRowCount(0);
        List<Phim> phimList = phimController.hienThiTatCaPhim();
        for (Phim phim : phimList) {
            tableModel.addRow(new Object[]{
                    phim.getMaPhim(), phim.getTenPhim(), phim.getMaTheLoai(), phim.getThoiLuong(),
                    phim.getNgayKhoiChieu(), phim.getNuocSanXuat(), phim.getDinhDang(), phim.getMoTa(), phim.getDaoDien()
            });
        }
    }

    private void searchMovies() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập từ khóa tìm kiếm!");
            return;
        }

        tableModel.setRowCount(0);
        List<Phim> phimList = phimController.hienThiTatCaPhim();
        phimList.stream()
                .filter(phim -> phim.getTenPhim().toLowerCase().contains(keyword.toLowerCase()))
                .forEach(phim -> tableModel.addRow(new Object[]{
                        phim.getMaPhim(), phim.getTenPhim(), phim.getMaTheLoai(), phim.getThoiLuong(),
                        phim.getNgayKhoiChieu(), phim.getNuocSanXuat(), phim.getDinhDang(), phim.getMoTa(), phim.getDaoDien()
                }));
    }
}