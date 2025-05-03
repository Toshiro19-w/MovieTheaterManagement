package com.cinema.views;

import com.cinema.enums.LoaiTaiKhoan;
import com.cinema.utils.PermissionManager;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.function.Consumer;

public class HeaderPanel extends JPanel {
    private final String username;
    private final LoaiTaiKhoan loaiTaiKhoan;
    private final Consumer<String> menuCallback;
    private final Consumer<String> searchCallback;
    private final Consumer<Void> logoutCallback;
    private final PermissionManager permissionManager;

    public HeaderPanel(String username, LoaiTaiKhoan loaiTaiKhoan, Consumer<String> menuCallback, Consumer<String> searchCallback, Consumer<Void> logoutCallback) {
        this.username = username;
        this.loaiTaiKhoan = loaiTaiKhoan;
        this.menuCallback = menuCallback;
        this.searchCallback = searchCallback;
        this.logoutCallback = logoutCallback;
        this.permissionManager = new PermissionManager(loaiTaiKhoan);

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1280, 80));
        setBackground(new Color(0, 48, 135));

        initComponents();
    }

    private void initComponents() {
        // Left: Logo + Cinema App Button
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        logoPanel.setOpaque(false);

        JLabel logoLabel = new JLabel(new ImageIcon("")); // Thay bằng đường dẫn logo thực tế
        logoPanel.add(logoLabel);

        JButton homeButton = new JButton("Cinema App");
        homeButton.setForeground(Color.WHITE);
        homeButton.setBackground(new Color(0, 48, 135));
        homeButton.setBorderPainted(false);
        homeButton.setFont(new Font("Arial", Font.BOLD, 24));
        homeButton.addActionListener(_ -> menuCallback.accept("Phim đang chiếu"));
        logoPanel.add(homeButton);

        add(logoPanel, BorderLayout.WEST);

        // Center: Search bar
        if (loaiTaiKhoan == LoaiTaiKhoan.USER) {
            JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            searchPanel.setOpaque(false);

            JTextField searchField = new JTextField(30);
            searchField.setPreferredSize(new Dimension(400, 30));
            searchField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    searchCallback.accept(searchField.getText().trim());
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    searchCallback.accept(searchField.getText().trim());
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    searchCallback.accept(searchField.getText().trim());
                }
            });
            searchPanel.add(searchField);

            add(searchPanel, BorderLayout.CENTER);
        } else {
            // Keep original menu for other roles
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

        // Right: User avatar + username button
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setOpaque(false);

        JLabel avatarLabel = new JLabel();
        try {
            ImageIcon avatarIcon = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("images/avatars/default.png")));
            Image scaledImage = avatarIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            avatarLabel.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            avatarLabel.setText("Avatar");
        }
        userPanel.add(avatarLabel);

        JButton userButton = new JButton("Xin chào, " + username);
        userButton.setForeground(Color.WHITE);
        userButton.setBackground(new Color(0, 48, 135));
        userButton.setBorderPainted(false);
        userButton.setFont(new Font("Arial", Font.PLAIN, 14));
        userButton.addActionListener(_ -> menuCallback.accept("Thông tin cá nhân"));
        userPanel.add(userButton);

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
        userPanel.add(logoutButton);

        add(userPanel, BorderLayout.EAST);
    }
}