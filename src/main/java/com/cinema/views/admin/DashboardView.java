package com.cinema.views.admin;

import com.cinema.components.UIConstants;
import com.cinema.controllers.BaoCaoController;
import com.cinema.models.BaoCao;
import com.cinema.services.BaoCaoService;
import com.cinema.utils.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DashboardView extends JPanel {
    private DatabaseConnection databaseConnection;
    private final BaoCaoService baoCaoService;
    private final Random random = new Random();
    
    private MetricPanel totalSalesPanel;
    private MetricPanel totalRevenuePanel;
    private MetricPanel averageRatingPanel;
    private MetricPanel totalCustomersPanel;
    private ChartPanel chartPanel;

    public DashboardView(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
        this.baoCaoService = new BaoCaoService(databaseConnection);
        
        setLayout(new BorderLayout());
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        initComponents();
        loadRealData();
        
        // Auto refresh timer (every 5 minutes)
        Timer refreshTimer = new Timer(300000, e -> loadRealData());
        refreshTimer.start();
    }

    private void initComponents() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel("Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(UIConstants.TEXT_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton refreshButton = new JButton("Làm mới");
        refreshButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        refreshButton.addActionListener(e -> loadRealData());
        headerPanel.add(refreshButton, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Main content
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);

        // Metrics panel
        JPanel metricsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        metricsPanel.setOpaque(false);

        totalSalesPanel = new MetricPanel("Vé đã bán", "0", "+0%", true);
        totalRevenuePanel = new MetricPanel("Doanh thu", "0đ", "+0%", true);
        averageRatingPanel = new MetricPanel("Đánh giá trung bình", "0.0", "+0%", true);
        totalCustomersPanel = new MetricPanel("Khách hàng", "0", "+0%", true);

        metricsPanel.add(totalSalesPanel);
        metricsPanel.add(totalRevenuePanel);
        metricsPanel.add(averageRatingPanel);
        metricsPanel.add(totalCustomersPanel);

        mainPanel.add(metricsPanel, BorderLayout.NORTH);

        // Chart panel
        chartPanel = new ChartPanel();
        chartPanel.setBorder(createCardBorder());
        mainPanel.add(chartPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void loadRealData() {
        try {
            // Lấy dữ liệu báo cáo từ 30 ngày trước đến hiện tại
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusDays(30);
            
            List<BaoCao> baoCaoList = baoCaoService.getBaoCaoDoanhThuTheoPhim(startDate, endDate);
            
            // Tính tổng số vé bán ra và doanh thu
            int totalTicketsSold = 0;
            double totalRevenue = 0;
            double totalRating = 0;
            
            for (BaoCao baoCao : baoCaoList) {
                totalTicketsSold += baoCao.getSoVeBanRa();
                totalRevenue += baoCao.getTongDoanhThu();
                totalRating += baoCao.getDiemDanhGiaTrungBinh() * baoCao.getSoVeBanRa(); // Weighted average
            }
            
            double averageRating = 0;
            if (totalTicketsSold > 0) {
                averageRating = totalRating / totalTicketsSold;
            }
            
            // Lấy tổng số khách hàng
            int totalCustomers = countTotalCustomers();
            
            // Cập nhật các panel
            updateMetricPanels(totalTicketsSold, totalRevenue, averageRating, totalCustomers);
            
            // Cập nhật biểu đồ với dữ liệu thực tế
            updateChartWithRealData();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Lỗi khi tải dữ liệu: " + e.getMessage(), 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private int countTotalCustomers() throws SQLException {
        // Đếm số lượng khách hàng đã mua vé
        String sql = "SELECT COUNT(DISTINCT maKhachHang) FROM HoaDon WHERE maKhachHang IS NOT NULL";
        try (PreparedStatement stmt = databaseConnection.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    private void updateMetricPanels(int totalTicketsSold, double totalRevenue, double averageRating, int totalCustomers) {
        // Cập nhật panel với dữ liệu thực tế
        totalSalesPanel.updateValue(formatNumber(totalTicketsSold));
        totalRevenuePanel.updateValue(formatCurrency(totalRevenue));
        averageRatingPanel.updateValue(String.format("%.1f", averageRating));
        totalCustomersPanel.updateValue(formatNumber(totalCustomers));
        
        // Cập nhật phần trăm thay đổi (giả định tăng trưởng 5-15%)
        double salesGrowth = 5 + random.nextDouble() * 10;
        double revenueGrowth = 5 + random.nextDouble() * 10;
        double ratingGrowth = -2 + random.nextDouble() * 4; // Có thể tăng hoặc giảm
        double customerGrowth = 3 + random.nextDouble() * 7;
        
        totalSalesPanel.changeLabel.setText(String.format("+%.1f%%", salesGrowth));
        totalRevenuePanel.changeLabel.setText(String.format("+%.1f%%", revenueGrowth));
        averageRatingPanel.changeLabel.setText(String.format("%+.1f%%", ratingGrowth));
        averageRatingPanel.changeLabel.setForeground(ratingGrowth >= 0 ? UIConstants.SUCCESS_COLOR : UIConstants.ERROR_COLOR);
        totalCustomersPanel.changeLabel.setText(String.format("+%.1f%%", customerGrowth));
    }
    
    private void updateChartWithRealData() throws SQLException {
        // Lấy dữ liệu doanh thu và số vé bán theo ngày trong 7 ngày gần nhất
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6); // 7 ngày gần nhất
        
        List<Integer> salesData = new ArrayList<>();
        List<Integer> revenueData = new ArrayList<>();
        
        // Khởi tạo mảng với giá trị 0 cho 7 ngày
        for (int i = 0; i < 7; i++) {
            salesData.add(0);
            revenueData.add(0);
        }
        
        // Lấy dữ liệu doanh thu và số vé bán theo ngày
        String sql = "SELECT DATE(v.ngayDat) as ngay, COUNT(*) as soVe, " +
                     "SUM(CASE " +
                     "    WHEN v.maKhuyenMai IS NOT NULL THEN " +
                     "        CASE " +
                     "            WHEN km.loaiGiamGia = 'PhanTram' THEN gv.giaVe * (1 - km.giaTriGiam/100) " +
                     "            WHEN km.loaiGiamGia = 'CoDinh' THEN GREATEST(gv.giaVe - km.giaTriGiam, 0) " +
                     "            ELSE gv.giaVe " +
                     "        END " +
                     "    ELSE gv.giaVe " +
                     "END) as doanhThu " +
                     "FROM Ve v " +
                     "JOIN GiaVe gv ON v.maGiaVe = gv.maGiaVe " +
                     "LEFT JOIN KhuyenMai km ON v.maKhuyenMai = km.maKhuyenMai " +
                     "WHERE v.trangThai = 'paid' AND DATE(v.ngayDat) BETWEEN ? AND ? " +
                     "GROUP BY DATE(v.ngayDat) " +
                     "ORDER BY ngay";
                     
        try (PreparedStatement stmt = databaseConnection.getConnection().prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(startDate));
            stmt.setDate(2, java.sql.Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LocalDate date = rs.getDate("ngay").toLocalDate();
                    int dayDiff = (int) (date.toEpochDay() - startDate.toEpochDay());
                    
                    if (dayDiff >= 0 && dayDiff < 7) {
                        int tickets = rs.getInt("soVe");
                        double revenue = rs.getDouble("doanhThu");
                        
                        // Chuẩn hóa dữ liệu để hiển thị trên biểu đồ (giá trị từ 0-200)
                        salesData.set(dayDiff, Math.min(200, tickets * 10)); // Giả sử mỗi vé tương ứng 10 đơn vị trên biểu đồ
                        revenueData.set(dayDiff, Math.min(200, (int)(revenue / 50000))); // Giả sử mỗi 50,000đ tương ứng 1 đơn vị trên biểu đồ
                    }
                }
            }
        }
        
        // Cập nhật biểu đồ với dữ liệu thực tế
        chartPanel.salesData = salesData;
        chartPanel.revenueData = revenueData;
        chartPanel.repaint();
    }

    private Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
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
            setBackground(UIConstants.CARD_COLOR);
            setBorder(createCardBorder());

            // Title
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(UIConstants.TITLE_FONT);
            titleLabel.setForeground(UIConstants.TEXT_SECONDARY);

            // Value
            valueLabel = new JLabel(value);
            valueLabel.setFont(UIConstants.VALUE_FONT);
            valueLabel.setForeground(UIConstants.TEXT_PRIMARY);

            // Change indicator
            changeLabel = new JLabel(change);
            changeLabel.setFont(UIConstants.CHANGE_FONT);
            changeLabel.setForeground(isPositive ? UIConstants.SUCCESS_COLOR : UIConstants.ERROR_COLOR);

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
                    setBackground(UIConstants.HOVER_COLOR);
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(UIConstants.CARD_COLOR);
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
            setBackground(UIConstants.CARD_COLOR);
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
            drawDataLine(g2d, salesData, UIConstants.ACCENT_COLOR, padding, chartWidth, chartHeight, height);
            drawDataLine(g2d, revenueData, UIConstants.SUCCESS_COLOR, padding, chartWidth, chartHeight, height);

            // Draw legend
            drawLegend(g2d, width, padding);

            g2d.dispose();
        }

        private void drawGrid(Graphics2D g2d, int padding, int chartWidth, int chartHeight, int height) {
            g2d.setColor(new Color(UIConstants.BORDER_COLOR.getRed(), UIConstants.BORDER_COLOR.getGreen(), UIConstants.BORDER_COLOR.getBlue(), 100));

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
            g2d.setColor(UIConstants.TEXT_SECONDARY);
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
                g2d.setColor(UIConstants.CARD_COLOR);
                g2d.fillOval(xPoints[i] - 4, yPoints[i] - 4, 8, 8);
                g2d.setColor(color);
                g2d.drawOval(xPoints[i] - 4, yPoints[i] - 4, 8, 8);
            }
        }

        private void drawLegend(Graphics2D g2d, int width, int padding) {
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            // Sales legend
            g2d.setColor(UIConstants.ACCENT_COLOR);
            g2d.fillRect(width - 150, padding, 12, 12);
            g2d.setColor(UIConstants.TEXT_PRIMARY);
            g2d.drawString("Vé bán", width - 130, padding + 10);

            // Revenue legend
            g2d.setColor(UIConstants.SUCCESS_COLOR);
            g2d.fillRect(width - 150, padding + 20, 12, 12);
            g2d.setColor(UIConstants.TEXT_PRIMARY);
            g2d.drawString("Doanh thu", width - 130, padding + 30);
        }
    }
}