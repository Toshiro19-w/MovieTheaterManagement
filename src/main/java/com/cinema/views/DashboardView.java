package com.cinema.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

/**
 * Dashboard view for Cinema Management System
 * Provides real-time metrics, charts, and activity feed
 */
public class DashboardView extends JPanel {
    
    // UI Components
    private MetricPanel totalMoviesPanel, activeScreeningsPanel, ticketsSoldPanel, revenuePanel;
    private JLabel clockLabel;
    private ChartPanel chartPanel;
    private JPanel newsPanel;
    private Timer refreshTimer;
    private Timer clockTimer;
    private final Random random = new Random();
    
    // Data fields
    private int totalMovies = 350;
    private int activeScreenings = 42;
    private int ticketsSold = 1287;
    private double revenue = 18450000;
    
    // Color scheme - Modern UI colors
    private static final Color BACKGROUND_COLOR = new Color(248, 250, 252);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color ACCENT_COLOR = new Color(79, 70, 229); // Indigo-600
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94); // Green-500
    private static final Color ERROR_COLOR = new Color(239, 68, 68); // Red-500
    private static final Color WARNING_COLOR = new Color(245, 158, 11); // Amber-500
    private static final Color TEXT_PRIMARY = new Color(17, 24, 39); // Gray-900
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128); // Gray-500
    private static final Color BORDER_COLOR = new Color(229, 231, 235); // Gray-200
    private static final Color HOVER_COLOR = new Color(249, 250, 251); // Gray-50
    
    // Typography
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font VALUE_FONT = new Font("Segoe UI", Font.BOLD, 28);
    private static final Font CHANGE_FONT = new Font("Segoe UI", Font.ITALIC, 12);
    private static final Font NEWS_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font TIME_FONT = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font CLOCK_FONT = new Font("Segoe UI", Font.BOLD, 14);

    public DashboardView() {
        initializeComponents();
        setupLayout();
        startTimers();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout(20, 20));
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Initialize metric panels
        totalMoviesPanel = new MetricPanel("Tổng số phim", String.valueOf(totalMovies), "+2.3%", true);
        activeScreeningsPanel = new MetricPanel("Suất chiếu hoạt động", String.valueOf(activeScreenings), "-1.2%", false);
        ticketsSoldPanel = new MetricPanel("Vé bán hôm nay", formatNumber(ticketsSold), "+15.7%", true);
        revenuePanel = new MetricPanel("Doanh thu hôm nay", formatCurrency(revenue), "+12.4%", true);
        
        // Initialize chart panel
        chartPanel = new ChartPanel();
        
        // Initialize clock
        clockLabel = new JLabel();
        clockLabel.setFont(CLOCK_FONT);
        clockLabel.setForeground(ACCENT_COLOR);
        clockLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        updateClock();
    }
    
    private void setupLayout() {
        // Metrics panel
        JPanel metricsContainer = new JPanel(new GridLayout(1, 4, 20, 0));
        metricsContainer.setBackground(BACKGROUND_COLOR);
        metricsContainer.add(totalMoviesPanel);
        metricsContainer.add(activeScreeningsPanel);
        metricsContainer.add(ticketsSoldPanel);
        metricsContainer.add(revenuePanel);
        
        // Chart and news container
        JSplitPane contentPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        contentPane.setDividerLocation(650);
        contentPane.setResizeWeight(0.65);
        contentPane.setBorder(null);
        contentPane.setBackground(BACKGROUND_COLOR);
        
        // Chart container
        JPanel chartContainer = createChartContainer();
        
        // News container
        JPanel newsContainer = createNewsContainer();
        
        contentPane.setLeftComponent(chartContainer);
        contentPane.setRightComponent(newsContainer);
        
        add(metricsContainer, BorderLayout.NORTH);
        add(contentPane, BorderLayout.CENTER);
    }
    
    private JPanel createChartContainer() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(CARD_COLOR);
        container.setBorder(createCardBorder());
        
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        JLabel title = new JLabel("Thống kê doanh thu (7 ngày qua)");
        title.setFont(TITLE_FONT);
        title.setForeground(TEXT_PRIMARY);
        
        header.add(title, BorderLayout.WEST);
        header.add(clockLabel, BorderLayout.EAST);
        
        container.add(header, BorderLayout.NORTH);
        container.add(chartPanel, BorderLayout.CENTER);
        
        return container;
    }
    
    private JPanel createNewsContainer() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(CARD_COLOR);
        container.setBorder(createCardBorder());
        
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        JLabel title = new JLabel("Hoạt động gần đây");
        title.setFont(TITLE_FONT);
        title.setForeground(TEXT_PRIMARY);
        
        JLabel refreshBtn = createRefreshButton();
        
        header.add(title, BorderLayout.WEST);
        header.add(refreshBtn, BorderLayout.EAST);
        
        // News panel
        newsPanel = new JPanel();
        newsPanel.setLayout(new BoxLayout(newsPanel, BoxLayout.Y_AXIS));
        newsPanel.setBackground(CARD_COLOR);
        
        updateNewsPanel();
        
        container.add(header, BorderLayout.NORTH);
        container.add(newsPanel, BorderLayout.CENTER);
        
        return container;
    }
    
    private JLabel createRefreshButton() {
        JLabel refreshBtn = new JLabel("🔄 Làm mới");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.setForeground(ACCENT_COLOR);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        refreshBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                refreshData();
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                refreshBtn.setForeground(ACCENT_COLOR.darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                refreshBtn.setForeground(ACCENT_COLOR);
            }
        });
        
        return refreshBtn;
    }
    
    private void updateNewsPanel() {
        newsPanel.removeAll();
        
        String[] activities = {
            "Phim mới 'Deadpool & Wolverine' đã được thêm",
            "Suất chiếu 14:30 phòng A01 đã được đặt",
            "Vé #T" + (10000 + random.nextInt(9999)) + " đã được bán",
            "Báo cáo doanh thu tuần đã được tạo",
            "Cập nhật thông tin phim 'Inside Out 2'",
            "Người dùng 'admin' đã đăng nhập",
            "Khuyến mãi 20% đã được kích hoạt",
            "Bảo trì hệ thống đã hoàn thành",
            "Cập nhật giá vé cho rạp premium",
            "Thêm combo bắp nước mới"
        };
        
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        Date now = new Date();
        
        for (int i = 0; i < Math.min(8, activities.length); i++) {
            Date activityTime = new Date(now.getTime() - (i * 15 + random.nextInt(10)) * 60000);
            JPanel newsItem = createNewsItem(activities[i], timeFormat.format(activityTime));
            newsPanel.add(newsItem);
        }
        
        newsPanel.revalidate();
        newsPanel.repaint();
    }
    
    private JPanel createNewsItem(String text, String time) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(CARD_COLOR);
        item.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            new EmptyBorder(12, 0, 12, 0)
        ));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        JLabel textLabel = new JLabel(text);
        textLabel.setFont(NEWS_FONT);
        textLabel.setForeground(TEXT_PRIMARY);
        
        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(TIME_FONT);
        timeLabel.setForeground(TEXT_SECONDARY);
        
        item.add(textLabel, BorderLayout.CENTER);
        item.add(timeLabel, BorderLayout.EAST);
        
        // Hover effect
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                item.setBackground(HOVER_COLOR);
                item.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                item.setBackground(CARD_COLOR);
                item.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
        
        return item;
    }
    
    private void refreshData() {
        // Update metrics with realistic variations
        totalMovies += random.nextInt(3) - 1; // ±1
        activeScreenings = Math.max(1, activeScreenings + random.nextInt(5) - 2); // ±2
        ticketsSold += random.nextInt(50) - 10; // ±25
        revenue += (random.nextInt(2000000) - 500000); // ±1M
        
        // Ensure positive values
        totalMovies = Math.max(1, totalMovies);
        ticketsSold = Math.max(0, ticketsSold);
        revenue = Math.max(0, revenue);
        
        // Update UI
        totalMoviesPanel.updateValue(String.valueOf(totalMovies));
        activeScreeningsPanel.updateValue(String.valueOf(activeScreenings));
        ticketsSoldPanel.updateValue(formatNumber(ticketsSold));
        revenuePanel.updateValue(formatCurrency(revenue));
        
        // Update chart and news
        chartPanel.updateData();
        updateNewsPanel();
    }
    
    private void updateClock() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date now = new Date();
        
        clockLabel.setText(String.format("<html><div style='text-align: right'>%s<br><span style='font-size: 10px; font-weight: normal'>%s</span></div></html>",
            timeFormat.format(now), dateFormat.format(now)));
    }
    
    private void startTimers() {
        // Data refresh timer - every 30 seconds
        refreshTimer = new Timer(30000, e -> refreshData());
        refreshTimer.start();
        
        // Clock timer - every second
        clockTimer = new Timer(1000, e -> updateClock());
        clockTimer.start();
    }
    
    public void stopTimers() {
        if (refreshTimer != null && refreshTimer.isRunning()) {
            refreshTimer.stop();
        }
        if (clockTimer != null && clockTimer.isRunning()) {
            clockTimer.stop();
        }
    }
    
    public void resumeTimers() {
        if (refreshTimer != null && !refreshTimer.isRunning()) {
            refreshTimer.start();
        }
        if (clockTimer != null && !clockTimer.isRunning()) {
            clockTimer.start();
        }
        updateClock();
    }
    
    private javax.swing.border.Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(20, 20, 20, 20)
        );
    }
    
    private String formatNumber(int number) {
        return new DecimalFormat("#,###").format(number);
    }
    
    private String formatCurrency(double amount) {
        return new DecimalFormat("#,###").format(amount) + "đ";
    }
    
    // Custom metric panel component
    private class MetricPanel extends JPanel {
        private JLabel valueLabel;
        private JLabel changeLabel;
        private final String title;
        
        public MetricPanel(String title, String value, String change, boolean isPositive) {
            this.title = title;
            setupPanel(value, change, isPositive);
        }
        
        private void setupPanel(String value, String change, boolean isPositive) {
            setLayout(new BorderLayout());
            setBackground(CARD_COLOR);
            setBorder(createCardBorder());
            
            // Title
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(TITLE_FONT);
            titleLabel.setForeground(TEXT_SECONDARY);
            
            // Value
            valueLabel = new JLabel(value);
            valueLabel.setFont(VALUE_FONT);
            valueLabel.setForeground(TEXT_PRIMARY);
            
            // Change indicator
            changeLabel = new JLabel(change);
            changeLabel.setFont(CHANGE_FONT);
            changeLabel.setForeground(isPositive ? SUCCESS_COLOR : ERROR_COLOR);
            
            // Layout
            JPanel changePanel = new JPanel(new BorderLayout());
            changePanel.setOpaque(false);
            changePanel.add(changeLabel, BorderLayout.WEST);
            
            add(titleLabel, BorderLayout.NORTH);
            add(valueLabel, BorderLayout.CENTER);
            add(changePanel, BorderLayout.SOUTH);
            
            // Hover effect
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(HOVER_COLOR);
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(CARD_COLOR);
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            });
        }
        
        public void updateValue(String value) {
            valueLabel.setText(value);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Subtle gradient background
            GradientPaint gradient = new GradientPaint(
                0, 0, getBackground(),
                0, getHeight(), getBackground().brighter()
            );
            g2d.setPaint(gradient);
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            
            g2d.dispose();
        }
    }
    
    // Enhanced chart panel
    private class ChartPanel extends JPanel {
        private List<Integer> salesData = new ArrayList<>();
        private List<Integer> revenueData = new ArrayList<>();
        private final int MAX_POINTS = 7;
        private final String[] DAYS = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        
        public ChartPanel() {
            setPreferredSize(new Dimension(600, 300));
            setBackground(CARD_COLOR);
            initializeData();
        }
        
        private void initializeData() {
            // Initialize with sample data
            for (int i = 0; i < MAX_POINTS; i++) {
                salesData.add(60 + random.nextInt(80)); // 60-140 range
                revenueData.add(40 + random.nextInt(100)); // 40-140 range
            }
        }
        
        public void updateData() {
            // Update data with smooth transitions
            for (int i = 0; i < salesData.size(); i++) {
                int currentSales = salesData.get(i);
                int newSales = Math.max(20, Math.min(180, currentSales + random.nextInt(20) - 10));
                salesData.set(i, newSales);
                
                int currentRevenue = revenueData.get(i);
                int newRevenue = Math.max(20, Math.min(180, currentRevenue + random.nextInt(20) - 10));
                revenueData.set(i, newRevenue);
            }
            repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int width = getWidth();
            int height = getHeight();
            int padding = 40;
            int chartWidth = width - 2 * padding;
            int chartHeight = height - 2 * padding;
            
            // Draw grid
            drawGrid(g2d, padding, chartWidth, chartHeight, height);
            
            // Draw axes
            drawAxes(g2d, padding, chartWidth, chartHeight, width, height);
            
            // Draw data lines
            drawDataLine(g2d, salesData, ACCENT_COLOR, padding, chartWidth, chartHeight, height);
            drawDataLine(g2d, revenueData, SUCCESS_COLOR, padding, chartWidth, chartHeight, height);
            
            // Draw legend
            drawLegend(g2d, width, padding);
            
            g2d.dispose();
        }
        
        private void drawGrid(Graphics2D g2d, int padding, int chartWidth, int chartHeight, int height) {
            g2d.setColor(new Color(BORDER_COLOR.getRed(), BORDER_COLOR.getGreen(), BORDER_COLOR.getBlue(), 100));
            
            // Horizontal grid lines
            for (int i = 0; i <= 5; i++) {
                int y = padding + (chartHeight * i) / 5;
                g2d.drawLine(padding, y, padding + chartWidth, y);
            }
            
            // Vertical grid lines
            int xStep = chartWidth / (MAX_POINTS - 1);
            for (int i = 0; i < MAX_POINTS; i++) {
                int x = padding + i * xStep;
                g2d.drawLine(x, padding, x, height - padding);
            }
        }
        
        private void drawAxes(Graphics2D g2d, int padding, int chartWidth, int chartHeight, int width, int height) {
            g2d.setColor(TEXT_SECONDARY);
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            
            // X-axis labels
            int xStep = chartWidth / (MAX_POINTS - 1);
            for (int i = 0; i < MAX_POINTS; i++) {
                int x = padding + i * xStep;
                g2d.drawString(DAYS[i], x - 8, height - padding + 20);
            }
            
            // Y-axis labels
            for (int i = 0; i <= 5; i++) {
                int y = padding + (chartHeight * i) / 5;
                String label = String.valueOf((5 - i) * 40);
                g2d.drawString(label, padding - 25, y + 3);
            }
        }
        
        private void drawDataLine(Graphics2D g2d, List<Integer> data, Color color, int padding, int chartWidth, int chartHeight, int height) {
            if (data.size() < 2) return;
            
            int xStep = chartWidth / (MAX_POINTS - 1);
            int maxValue = 200;
            
            // Calculate points
            int[] xPoints = new int[data.size()];
            int[] yPoints = new int[data.size()];
            
            for (int i = 0; i < data.size(); i++) {
                xPoints[i] = padding + i * xStep;
                yPoints[i] = height - padding - (data.get(i) * chartHeight) / maxValue;
            }
            
            // Draw line
            g2d.setColor(color);
            g2d.setStroke(new java.awt.BasicStroke(3, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            
            for (int i = 0; i < data.size() - 1; i++) {
                g2d.drawLine(xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1]);
            }
            
            // Draw points
            for (int i = 0; i < data.size(); i++) {
                g2d.setColor(CARD_COLOR);
                g2d.fillOval(xPoints[i] - 4, yPoints[i] - 4, 8, 8);
                g2d.setColor(color);
                g2d.drawOval(xPoints[i] - 4, yPoints[i] - 4, 8, 8);
            }
        }
        
        private void drawLegend(Graphics2D g2d, int width, int padding) {
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            
            // Sales legend
            g2d.setColor(ACCENT_COLOR);
            g2d.fillRect(width - 150, padding, 12, 12);
            g2d.setColor(TEXT_PRIMARY);
            g2d.drawString("Vé bán", width - 130, padding + 10);
            
            // Revenue legend
            g2d.setColor(SUCCESS_COLOR);
            g2d.fillRect(width - 150, padding + 20, 12, 12);
            g2d.setColor(TEXT_PRIMARY);
            g2d.drawString("Doanh thu", width - 130, padding + 30);
        }
    }
}