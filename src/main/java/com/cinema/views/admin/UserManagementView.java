package com.cinema.views.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.sql.SQLException;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

public class UserManagementView extends JPanel {
    private static final Color CINESTAR_BLUE = new Color(0, 51, 102);
    private static final Color CINESTAR_YELLOW = new Color(255, 204, 0);
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);

    public UserManagementView() throws SQLException {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Roboto", Font.PLAIN, 14));
        tabbedPane.addTab("Nhân viên", new NhanVienView());
        tabbedPane.addTab("Khách hàng", new KhachHangView());
        add(tabbedPane, BorderLayout.CENTER);
    }
}