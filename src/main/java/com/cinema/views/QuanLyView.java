package com.cinema.views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class QuanLyView extends JFrame {
    private JPanel mainPanel;
    private JMenuBar menuBar;
    private JMenu menuQuanLy, menuBaoCao;
    private JMenuItem menuItemPhim, menuItemVe, menuItemNhanVien, menuItemSuatChieu, menuItemKhachHang, menuItemDoanhThu;

    public QuanLyView() {
        initUI();
    }

    private void initUI() {
        setTitle("Hệ thống quản lý rạp chiếu phim");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        mainPanel = new JPanel(new GridLayout(1, 0));
        add(mainPanel);

        menuBar = new JMenuBar();

        menuQuanLy = new JMenu("Quản Lý");
        menuItemPhim = createMenuItem("Quản lý phim", this::openQuanLyPhim);
        menuItemVe = createMenuItem("Quản lý vé", this::openQuanLyVe);
        menuItemNhanVien = createMenuItem("Quản lý nhân viên", this::openQuanLyNhanVien);
        menuItemSuatChieu = createMenuItem("Quản lý suất chiếu", this::openQuanLySuatChieu);
        menuItemKhachHang = createMenuItem("Quản lý khách hàng", this::openQuanLyKhachHang);

        menuQuanLy.add(menuItemPhim);
        menuQuanLy.add(menuItemVe);
        menuQuanLy.add(menuItemNhanVien);
        menuQuanLy.add(menuItemSuatChieu);
        menuQuanLy.add(menuItemKhachHang);
        menuBar.add(menuQuanLy);

        menuBaoCao = new JMenu("Báo cáo");
        menuItemDoanhThu = createMenuItem("Doanh thu", this::openBaoCaoDoanhThu);
        menuBaoCao.add(menuItemDoanhThu);
        menuBar.add(menuBaoCao);

        setJMenuBar(menuBar);
    }

    private JMenuItem createMenuItem(String title, java.awt.event.ActionListener action) {
        JMenuItem menuItem = new JMenuItem(title);
        menuItem.addActionListener(action);
        return menuItem;
    }

    private void openQuanLyPhim(ActionEvent evt) {
        openFrame(new PhimView());
    }

    private void openQuanLyVe(ActionEvent evt) {
        //openFrame(new QuanLyVeFrame());
    }

    private void openQuanLyNhanVien(ActionEvent evt) {
        //openFrame(new QuanLyNhanVienFrame());
    }

    private void openQuanLySuatChieu(ActionEvent evt) {
        //openFrame(new QuanLySuatChieuFrame());
    }

    private void openQuanLyKhachHang(ActionEvent evt) {
        //openFrame(new QuanLyKhachHangFrame());
    }

    private void openBaoCaoDoanhThu(ActionEvent evt) {
        //openFrame(new BaoCaoDoanhThuFrame());
    }

    private void openFrame(JFrame frame) {
        frame.setVisible(true);
        frame.pack();
        frame.setLocationRelativeTo(null);
        this.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new QuanLyView().setVisible(true));
    }
}