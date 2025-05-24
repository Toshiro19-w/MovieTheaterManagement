package com.cinema.views.admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

public class GiaVeManagementView extends JPanel {
    private static final Color CINESTAR_BLUE = new Color(0, 51, 102);
    private static final Color CINESTAR_YELLOW = new Color(255, 204, 0);
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    
    private JTabbedPane tabbedPane;
    private VeView veView;
    private LichSuGiaVeView lichSuGiaVeView;
    
    public GiaVeManagementView() throws SQLException {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(10, 10, 10, 10));
        
        initComponents();
    }
    
    private void initComponents() throws SQLException {
        // Tabbed Pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Roboto", Font.PLAIN, 14));
        
        // Create child views
        veView = new VeView();
        lichSuGiaVeView = new LichSuGiaVeView();
        
        // Add tabs
        tabbedPane.addTab("Quản lý vé", veView);
        tabbedPane.addTab("Lịch sử giá vé", lichSuGiaVeView);
        
        // Add to main panel
        add(tabbedPane, BorderLayout.CENTER);
    }
}