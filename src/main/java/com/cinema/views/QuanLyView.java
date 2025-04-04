package com.cinema.views;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

public class QuanLyView extends JFrame {
    private JPanel mainContentPanel;
    private CardLayout cardLayout;

    public QuanLyView() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }

        setTitle("Hệ thống quản lý rạp chiếu phim");
        setSize(1200, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel logoLabel = new JLabel("Cinema Management");
        headerPanel.add(logoLabel, BorderLayout.WEST);

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String[] sections = {"Phim", "Suất chiếu", "Vé", "Hóa đơn", "Nhân viên", "Báo cáo"};
        for (String section : sections) {
            JButton button = new JButton(section);
            button.addActionListener(_ -> cardLayout.show(mainContentPanel, section));
            navPanel.add(button);
        }
        headerPanel.add(navPanel, BorderLayout.CENTER);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel usernameLabel = new JLabel("Username");
        JButton logoutButton = new JButton("Đăng xuất");
        userPanel.add(usernameLabel);
        userPanel.add(logoutButton);
        headerPanel.add(userPanel, BorderLayout.EAST);

        // Main content
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.add(new PhimView(), "Phim");
        //mainContentPanel.add(new SuatChieuView(), "Suất chiếu");
        mainContentPanel.add(new VeView(), "Vé");
        //mainContentPanel.add(new HoaDonView(), "Hoá đơn");
        //mainContentPanel.add(new NhanVienView(), "Nhân viên");
        //mainContentPanel.add(new BaoCaoView(), "Báo cáo");

        add(headerPanel, BorderLayout.NORTH);
        add(mainContentPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    public static void main(String[] args) {
        new QuanLyView();
    }
}