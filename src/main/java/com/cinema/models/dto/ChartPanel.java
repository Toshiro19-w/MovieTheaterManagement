// ChartPanelDTO.java
package com.cinema.models.dto;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.*;
import com.cinema.components.UIConstants;

public class ChartPanel extends JPanel {
    private List<Integer> salesData = new ArrayList<>();
    private List<Integer> revenueData = new ArrayList<>();
    private final int MAX_POINTS = 7;
    private final String[] DAYS = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
    private final Random random = new Random();

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

    public void setSalesData(List<Integer> salesData) {
        this.salesData = salesData;
        repaint();
    }

    public void setRevenueData(List<Integer> revenueData) {
        this.revenueData = revenueData;
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
