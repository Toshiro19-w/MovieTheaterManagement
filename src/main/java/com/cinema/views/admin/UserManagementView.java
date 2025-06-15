package com.cinema.views.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.sql.SQLException;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import com.cinema.views.common.ResizableView;

public class UserManagementView extends JPanel implements ResizableView {
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 600;
    private static final int MIN_WIDTH = 300;
    private static final int MIN_HEIGHT = 200;

    public UserManagementView() throws SQLException {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Roboto", Font.PLAIN, 14));
        tabbedPane.addTab("Nhân viên", new NhanVienView());
        tabbedPane.addTab("Khách hàng", new KhachHangView());
        tabbedPane.addTab("Phiên làm việc", new PhienLamViecView());
        add(tabbedPane, BorderLayout.CENTER);
    }

    @Override
    public Dimension getPreferredViewSize() {
        // Vì view này chứa nhiều tab với thông tin người dùng
        // nên để kích thước mặc định như interface đã định nghĩa
        return new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    @Override
    public Dimension getMinimumViewSize() {
        return new Dimension(MIN_WIDTH, MIN_HEIGHT);
    }

    @Override
    public boolean needsScrolling() {
        // Cần scroll vì có nhiều thông tin trong các tab
        return true;
    }
}