package com.cinema.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import com.cinema.controllers.ActivityLogController;
import com.cinema.models.ActivityLog;

public class ActivityLogPanel extends JPanel {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
    private JPanel logsContainer;
    private List<ActivityLog> activityLogs = new ArrayList<>();
    private final int MAX_LOGS = 10;
    private ActivityLogController activityLogController;
    
    public ActivityLogPanel() throws IOException {
        this.activityLogController = new ActivityLogController();
        
        setLayout(new BorderLayout());
        setBackground(UIConstants.CARD_COLOR);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Tiêu đề
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIConstants.CARD_COLOR);
        
        JLabel titleLabel = new JLabel("Hoạt động gần đây");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(UIConstants.TEXT_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Nút làm mới
        JButton refreshButton = new JButton("Làm mới");
        refreshButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshButton.setForeground(UIConstants.TEXT_PRIMARY);
        refreshButton.setBackground(UIConstants.BUTTON_COLOR);
        refreshButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshButton.addActionListener(e -> loadLogs());
        headerPanel.add(refreshButton, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Container cho các log
        logsContainer = new JPanel();
        logsContainer.setLayout(new BoxLayout(logsContainer, BoxLayout.Y_AXIS));
        logsContainer.setBackground(UIConstants.CARD_COLOR);
        
        JScrollPane scrollPane = new JScrollPane(logsContainer);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        add(scrollPane, BorderLayout.CENTER);
        
        // Tải log từ cơ sở dữ liệu
        loadLogs();
    }
    
    public void loadLogs() {
        try {
            activityLogs = activityLogController.getRecentLogs(MAX_LOGS);
            updateLogsDisplay();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                    "Lỗi khi tải hoạt động: " + e.getMessage(), 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            // Nếu không tải được từ cơ sở dữ liệu, hiển thị giao diện trống
            activityLogs.clear();
            updateLogsDisplay();
        }
    }
    
    public void addLog(String action, String description, int maNguoiDung) {
        try {
            int logId = activityLogController.addLog(action, description, maNguoiDung);
            if (logId > 0) {
                loadLogs(); // Chỉ tải lại danh sách log khi thêm thành công
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                    "Lỗi khi thêm hoạt động: " + e.getMessage(), 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateLogsDisplay() {
        logsContainer.removeAll();
        
        if (activityLogs.isEmpty()) {
            JLabel emptyLabel = new JLabel("Không có hoạt động nào gần đây");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            emptyLabel.setForeground(UIConstants.TEXT_SECONDARY);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            logsContainer.add(Box.createVerticalGlue());
            logsContainer.add(emptyLabel);
            logsContainer.add(Box.createVerticalGlue());
        } else {
            for (ActivityLog log : activityLogs) {
                JPanel logPanel = createLogPanel(log);
                logsContainer.add(logPanel);
                logsContainer.add(Box.createVerticalStrut(10));
            }
        }
        
        logsContainer.revalidate();
        logsContainer.repaint();
    }
    
    private JPanel createLogPanel(ActivityLog log) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(UIConstants.CARD_COLOR);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER_COLOR));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        
        // Action
        JLabel actionLabel = new JLabel(log.getLoaiHoatDong());
        actionLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        actionLabel.setForeground(getActionColor(log.getLoaiHoatDong()));
        
        // Description
        JLabel descLabel = new JLabel(log.getMoTa());
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(UIConstants.TEXT_SECONDARY);
        
        // User and Timestamp
        String timeStr = log.getFormattedTime() != null ? log.getFormattedTime() : "N/A";
        String userName = log.getTenNguoiDung() != null ? log.getTenNguoiDung() : "User " + log.getMaNguoiDung();
        JLabel timeLabel = new JLabel(userName + " - " + timeStr);
        timeLabel.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        timeLabel.setForeground(UIConstants.LIGHT_TEXT_COLOR);
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(UIConstants.CARD_COLOR);
        textPanel.setBorder(new EmptyBorder(5, 0, 5, 0));
        
        actionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        textPanel.add(actionLabel);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(descLabel);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(timeLabel);
        
        panel.add(textPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private Color getActionColor(String action) {
        if (action.contains("Thêm")) return UIConstants.SUCCESS_COLOR;
        if (action.contains("Sửa")) return UIConstants.ACCENT_COLOR;
        if (action.contains("Xóa")) return UIConstants.ERROR_COLOR;
        if (action.contains("Bán")) return UIConstants.SUCCESS_COLOR;
        if (action.contains("Đăng nhập")) return UIConstants.INFO_COLOR;
        return UIConstants.TEXT_PRIMARY;
    }
}