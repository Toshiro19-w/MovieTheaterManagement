package com.cinema.views;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class QuanLyView extends JFrame {
    private JPanel mainContentPanel;
    private CardLayout cardLayout;
    private static String username;

    public QuanLyView(String username) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }

        this.username = username;

        setTitle("Hệ thống quản lý rạp chiếu phim");
        setSize(1200, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel logoLabel = new JLabel("Cinema Management");
        headerPanel.add(logoLabel, BorderLayout.WEST);

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String[] sections = {"Phim", "Suất chiếu", "Vé", "Thanh toán", "Nhân viên", "Báo cáo"};
        for (String section : sections) {
            JButton button = new JButton(section);
            button.addActionListener(_ -> cardLayout.show(mainContentPanel, section));
            navPanel.add(button);
        }
        headerPanel.add(navPanel, BorderLayout.CENTER);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel usernameLabel = new JLabel("Xin Chào, " + username);
        JButton logoutButton = new JButton("Đăng xuất");
        userPanel.add(usernameLabel);
        userPanel.add(logoutButton);
        logoutButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
                SwingUtilities.invokeLater(() -> new LoginView().setVisible(true));
            }
        });
        headerPanel.add(userPanel, BorderLayout.EAST);

        // Main content
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.add(new PhimView(), "Phim");
        mainContentPanel.add(new SuatChieuView(), "Suất chiếu");
        mainContentPanel.add(new VeView(), "Vé");
        mainContentPanel.add(new ThanhToanView(), "Thanh toán");
        mainContentPanel.add(new NhanVienView(), "Nhân viên");
        //mainContentPanel.add(new BaoCaoView(), "Báo cáo");

        add(headerPanel, BorderLayout.NORTH);
        add(mainContentPanel, BorderLayout.CENTER);

        setVisible(true);
    }
}