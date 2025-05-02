package com.cinema.views;

import com.cinema.enums.LoaiTaiKhoan;
import com.cinema.utils.PermissionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class HeaderPanel extends JPanel {
    private final String username;
    private final LoaiTaiKhoan loaiTaiKhoan;
    private final Consumer<String> menuCallback;
    private final Consumer<Void> logoutCallback;
    private final PermissionManager permissionManager;

    public HeaderPanel(String username, LoaiTaiKhoan loaiTaiKhoan, Consumer<String> menuCallback, Consumer<Void> logoutCallback) {
        this.username = username;
        this.loaiTaiKhoan = loaiTaiKhoan;
        this.menuCallback = menuCallback;
        this.logoutCallback = logoutCallback;
        this.permissionManager = new PermissionManager(loaiTaiKhoan);

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1280, 80));
        setBackground(new Color(0, 48, 135));

        initComponents();
    }

    private void initComponents() {
        // Logo + Title
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        logoPanel.setOpaque(false);

        JLabel logoLabel = new JLabel(new ImageIcon("")); // Thay bằng đường dẫn logo thực tế
        logoLabel.setText("Cinema App");
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setFont(new Font("Arial", Font.BOLD, 24));
        logoPanel.add(logoLabel);

        add(logoPanel, BorderLayout.WEST);

        // Menu (chỉ cho USER)
        if (loaiTaiKhoan == LoaiTaiKhoan.USER) {
            JPanel menuPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
            menuPanel.setOpaque(false);

            String[] userFeatures = {"Phim đang chiếu", "Thông tin cá nhân"};
            for (String feature : userFeatures) {
                JButton button = new JButton(feature);
                button.setForeground(Color.WHITE);
                button.setBackground(new Color(0, 48, 135));
                button.setBorderPainted(false);
                button.setFont(new Font("Arial", Font.PLAIN, 16));
                button.addActionListener(_ -> menuCallback.accept(feature));
                menuPanel.add(button);
            }
            add(menuPanel, BorderLayout.CENTER);
        } else {
            // Giữ menu gốc cho các vai trò khác
            JPanel menuPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
            menuPanel.setOpaque(false);

            for (String feature : permissionManager.getPermissions()) {
                JButton button = new JButton(feature);
                button.setForeground(Color.WHITE);
                button.setBackground(new Color(0, 48, 135));
                button.setBorderPainted(false);
                button.setFont(new Font("Arial", Font.PLAIN, 16));
                button.addActionListener(_ -> menuCallback.accept(feature));
                menuPanel.add(button);
            }
            add(menuPanel, BorderLayout.CENTER);
        }

        // User info
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setOpaque(false);
        JLabel userLabel = new JLabel("Xin chào, " + username);
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JButton logoutButton = new JButton("Đăng xuất");
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setBackground(new Color(0, 48, 135));
        logoutButton.setBorderPainted(false);
        logoutButton.setFont(new Font("Arial", Font.PLAIN, 14));
        logoutButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                logoutCallback.accept(null);
            }
        });
        userPanel.add(userLabel);
        userPanel.add(logoutButton);
        add(userPanel, BorderLayout.EAST);
    }
}