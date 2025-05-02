package com.cinema.views;

import javax.swing.*;
import java.awt.*;

public class UserInfoView extends JDialog {
    private final String username;

    public UserInfoView(JFrame parent, String username) {
        super(parent, "Thông tin cá nhân", true);
        this.username = username;

        setSize(300, 200);
        setLayout(new BorderLayout());
        setLocationRelativeTo(parent);

        initializeComponents();
    }

    private void initializeComponents() {
        JLabel infoLabel = new JLabel("Thông tin cá nhân của " + username + " (Chưa triển khai chi tiết)", SwingConstants.CENTER);
        infoLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(infoLabel, BorderLayout.CENTER);

        JButton closeButton = new JButton("Đóng");
        closeButton.addActionListener(_ -> dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}