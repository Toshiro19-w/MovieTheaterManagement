package com.cinema.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class DashboardView extends JPanel {
    private JLabel totalMoviesLabel, activeScreeningsLabel, ticketsSoldLabel, revenueLabel;
    private JLabel newMovieLabel, screeningAddedLabel, ticketSoldLabel, revenueReportLabel;
    private JLabel userUpdateLabel, screeningUpdateLabel, ticketRefundLabel, newMovieAddedLabel;
    private JLabel dailySalesLabel, systemCheckLabel;

    public DashboardView() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Metrics Panel
        JPanel metricsPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        metricsPanel.setBackground(new Color(245, 245, 245));

        JPanel totalMovies = createMetricPanel("Total Movies", "350", "+8%");
        JPanel activeScreenings = createMetricPanel("Active Screenings", "42", "-2%");
        JPanel ticketsSold = createMetricPanel("Tickets Sold Today", "1,287", "+15%");
        JPanel revenue = createMetricPanel("Revenue Today", "$18,450", "+10%");

        metricsPanel.add(totalMovies);
        metricsPanel.add(activeScreenings);
        metricsPanel.add(ticketsSold);
        metricsPanel.add(revenue);
        
        // Chart and News Panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(400);
        splitPane.setEnabled(false);
        splitPane.setBorder(null);
        splitPane.setBackground(new Color(245, 245, 245));

        // Chart Panel (simplified)
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBorder(BorderFactory.createTitledBorder("Báo cáo & Thống kê (Đoạn thứ 7 ngày qua)"));
        chartPanel.add(new JLabel("Chart Placeholder"), BorderLayout.CENTER);

        // News Panel
        JPanel newsPanel = new JPanel();
        newsPanel.setLayout(new BoxLayout(newsPanel, BoxLayout.Y_AXIS));
        newsPanel.setBorder(BorderFactory.createTitledBorder("Hoạt động Gần đây"));
        
        newMovieLabel = new JLabel("New movie 'Inside Out 2' added.");
        screeningAddedLabel = new JLabel("Screening added for 'Deadpool 3' at 14:00.");
        ticketSoldLabel = new JLabel("Ticket #T12345 sold.");
        revenueReportLabel = new JLabel("Revenue report generated for past week.");
        userUpdateLabel = new JLabel("User 'Admin' updated system settings.");
        screeningUpdateLabel = new JLabel("Screening updated for 'Avatar: The Way of Water' (Room 3).");
        ticketRefundLabel = new JLabel("Ticket #T12347 marked as refund pending.");
        newMovieAddedLabel = new JLabel("New movie 'Joker: Folie à Deux' added.");
        dailySalesLabel = new JLabel("Daily ticket sales summary generated.");
        systemCheckLabel = new JLabel("System performance checked.");

        newsPanel.add(createNewsItem(newMovieLabel));
        newsPanel.add(createNewsItem(screeningAddedLabel));
        newsPanel.add(createNewsItem(ticketSoldLabel));
        newsPanel.add(createNewsItem(revenueReportLabel));
        newsPanel.add(createNewsItem(userUpdateLabel));
        newsPanel.add(createNewsItem(screeningUpdateLabel));
        newsPanel.add(createNewsItem(ticketRefundLabel));
        newsPanel.add(createNewsItem(newMovieAddedLabel));
        newsPanel.add(createNewsItem(dailySalesLabel));
        newsPanel.add(createNewsItem(systemCheckLabel));

        splitPane.setLeftComponent(chartPanel);
        splitPane.setRightComponent(newsPanel);

        add(metricsPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createMetricPanel(String title, String value, String change) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Roboto", Font.BOLD, 20));
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel changeLabel = new JLabel(change);
        changeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        changeLabel.setForeground(change.startsWith("+") ? new Color(0, 128, 0) : new Color(255, 0, 0));

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);
        panel.add(changeLabel, BorderLayout.SOUTH);

        return panel;
    }

    private Component createNewsItem(JLabel label) {
        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return label;
    }

    public void updateMetrics(int totalMovies, int activeScreenings, int ticketsSold, double revenue) {
        DecimalFormat df = new DecimalFormat("#,###.00");
        totalMoviesLabel.setText(String.valueOf(totalMovies));
        activeScreeningsLabel.setText(String.valueOf(activeScreenings));
        ticketsSoldLabel.setText(String.valueOf(ticketsSold));
        revenueLabel.setText("$" + df.format(revenue));
    }
}