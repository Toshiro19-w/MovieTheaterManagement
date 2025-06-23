package com.cinema.views.admin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import com.cinema.components.ActivityLogPanel;
import com.cinema.components.UIConstants;
import com.cinema.models.BaoCao;
import com.cinema.models.dto.ChartPanel;
import com.cinema.models.dto.MetricPanel;
import com.cinema.models.repositories.DashboardRepository;
import com.cinema.models.repositories.Interface.IDashboardRepository;
import com.cinema.services.BaoCaoService;
import com.cinema.utils.DatabaseConnection;

public class DashboardView extends JPanel {
    private final int PREFERRED_WIDTH = 1024;
    private final int PREFERRED_HEIGHT = 768;
    private final int MIN_WIDTH = 800;
    private final int MIN_HEIGHT = 600;
    private DatabaseConnection databaseConnection;
    private final BaoCaoService baoCaoService;
    private final Random random = new Random();
    
    private MetricPanel totalSalesPanel;
    private MetricPanel totalRevenuePanel;
    private MetricPanel averageRatingPanel;
    private MetricPanel totalCustomersPanel;
    private ChartPanel chartPanel;
    
    // Thêm biến để kiểm soát tần suất nhấn nút
    private boolean isRefreshingData = false;
    private long lastRefreshTime = 0;
    private static final int REFRESH_COOLDOWN = 5000; // 5 giây

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
        refreshButton.addActionListener(e -> {
            long currentTime = System.currentTimeMillis();
            
            // Kiểm tra nếu đang làm mới hoặc chưa đủ thời gian giữa các lần nhấn
            if (isRefreshingData || (currentTime - lastRefreshTime < REFRESH_COOLDOWN)) {
                JOptionPane.showMessageDialog(this, 
                    "Vui lòng đợi ít nhất " + (REFRESH_COOLDOWN / 1000) + " giây trước khi làm mới lại.",
                    "Thông báo", 
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // Cập nhật thời gian làm mới cuối cùng
            lastRefreshTime = currentTime;
            isRefreshingData = true;
            
            refreshButton.setEnabled(false);
            refreshButton.setText("Đang tải...");
            
            // Sử dụng SwingWorker để tải dữ liệu trong background
            new javax.swing.SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    loadRealData();
                    return null;
                }
                
                @Override
                protected void done() {
                    // Kích hoạt lại nút sau khi hoàn thành
                    refreshButton.setEnabled(true);
                    refreshButton.setText("Làm mới");
                    isRefreshingData = false;
                }
            }.execute();
        });
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

        // Content panel with chart and activity log
        JPanel contentPanel = new JPanel(new BorderLayout(20, 0));
        contentPanel.setOpaque(false);

        // Chart panel (left side)
        chartPanel = new ChartPanel();
        chartPanel.setBorder(createCardBorder());
        contentPanel.add(chartPanel, BorderLayout.CENTER);

        // Activity log panel (right side)
        ActivityLogPanel activityLogPanel = null;
        try {
            activityLogPanel = new ActivityLogPanel();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        activityLogPanel.setPreferredSize(new Dimension(300, 0));
        contentPanel.add(activityLogPanel, BorderLayout.EAST);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

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
        DashboardRepository dashboardRepo = new DashboardRepository(databaseConnection);
        return dashboardRepo.countTotalCustomers();
    }
    
    private void updateMetricPanels(int totalTicketsSold, double totalRevenue, double averageRating, int totalCustomers) {
        try {
            // Cập nhật panel với dữ liệu thực tế
            totalSalesPanel.updateValue(formatNumber(totalTicketsSold));
            totalRevenuePanel.updateValue(formatCurrency(totalRevenue));
            averageRatingPanel.updateValue(String.format("%.1f", averageRating));
            totalCustomersPanel.updateValue(formatNumber(totalCustomers));
            
            // Lấy dữ liệu từ khoảng thời gian trước đó để so sánh
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime currentStartDate = endDate.minusDays(30);
            LocalDateTime previousStartDate = currentStartDate.minusDays(30);
            
            DashboardRepository dashboardRepo = new DashboardRepository(databaseConnection);
            IDashboardRepository.SummaryData previousData = dashboardRepo.getSummaryData(previousStartDate, currentStartDate);
            
            // Tính toán phần trăm thay đổi
            int previousTickets = previousData.getTicketsSold();
            double previousRevenue = previousData.getRevenue();
            double previousRating = previousData.getAverageRating();
            int previousCustomers = 0; // Không có dữ liệu so sánh cho khách hàng
            
            // Cập nhật thông tin thay đổi
            if (previousTickets > 0) {
                totalSalesPanel.updateChangeValue(formatNumber(totalTicketsSold - previousTickets), totalTicketsSold >= previousTickets);
            } else {
                totalSalesPanel.updateChangeValue(formatNumber(totalTicketsSold), true);
            }
            
            if (previousRevenue > 0) {
                double revenueDiff = totalRevenue - previousRevenue;
                totalRevenuePanel.updateChangeValue(formatCurrency(revenueDiff), revenueDiff >= 0);
            } else {
                totalRevenuePanel.updateChangeValue(formatCurrency(totalRevenue), true);
            }
            
            if (previousRating > 0) {
                double ratingDiff = averageRating - previousRating;
                averageRatingPanel.updateChangeValue(String.format("%+.1f", ratingDiff), ratingDiff >= 0);
            } else {
                averageRatingPanel.updateChangeValue(String.format("%.1f", averageRating), true);
            }
            
            // Không có dữ liệu so sánh cho khách hàng, hiển thị số lượng hiện tại
            totalCustomersPanel.updateChangeValue(formatNumber(totalCustomers), true);
            
        } catch (SQLException e) {
            // Nếu có lỗi, sử dụng giá trị mặc định
            totalSalesPanel.updateChangeValue("N/A", true);
            totalRevenuePanel.updateChangeValue("N/A", true);
            averageRatingPanel.updateChangeValue("N/A", true);
            totalCustomersPanel.updateChangeValue("N/A", true);
            e.printStackTrace();
        }
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
        
        // Lấy dữ liệu từ repository
        DashboardRepository dashboardRepo = new DashboardRepository(databaseConnection);
        Map<LocalDate, IDashboardRepository.DailySalesData> salesByDate = dashboardRepo.getSalesDataByDateRange(startDate, endDate);
        
        // Xử lý dữ liệu cho biểu đồ
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            int dayDiff = (int) (date.toEpochDay() - startDate.toEpochDay());
            
            if (dayDiff >= 0 && dayDiff < 7) {
                IDashboardRepository.DailySalesData data = salesByDate.get(date);
                if (data != null) {
                    int tickets = data.getTicketsSold();
                    double revenue = data.getRevenue();
                    
                    // Chuẩn hóa dữ liệu để hiển thị trên biểu đồ (giá trị từ 0-200)
                    salesData.set(dayDiff, Math.min(200, tickets * 10)); // Giả sử mỗi vé tương ứng 10 đơn vị trên biểu đồ
                    revenueData.set(dayDiff, Math.min(200, (int)(revenue / 50000))); // Giả sử mỗi 50,000đ tương ứng 1 đơn vị trên biểu đồ
                }
            }
        }
        
        // Cập nhật biểu đồ với dữ liệu thực tế
        chartPanel.setSalesData(salesData);
        chartPanel.setRevenueData(revenueData);
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
}