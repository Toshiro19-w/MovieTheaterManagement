package com.cinema.views.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import com.cinema.controllers.BaoCaoController;
import com.cinema.models.BaoCaoNhanVien;
import com.cinema.services.BaoCaoService;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.SimpleDocumentListener;
import com.cinema.utils.ValidationUtils;

public class EmployeeReportPanel extends JPanel {
    private BaoCaoController controller;
    private JTextField tuNgayField;
    private JTextField denNgayField;
    private JLabel tuNgayErrorLabel;
    private JLabel denNgayErrorLabel;
    private DefaultTableModel tableModel;
    private JPanel chartPanel;
    private ResourceBundle messages;

    private static final Font LABEL_FONT = new Font("Inter", Font.PLAIN, 14);
    private static final Font BUTTON_FONT = new Font("Inter", Font.BOLD, 14);
    private static final Color PRIMARY_COLOR = new Color(59, 130, 246);

    public EmployeeReportPanel() {
        try {
            DatabaseConnection databaseConnection = new DatabaseConnection();
            controller = new BaoCaoController(new BaoCaoService(databaseConnection));
            messages = ResourceBundle.getBundle("Messages");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, messages.getString("dbConnectionError"), messages.getString("error"), JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));

        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        timePanel.setOpaque(false);

        JLabel tuNgayLabel = new JLabel("Từ ngày:");
        tuNgayLabel.setFont(LABEL_FONT);
        tuNgayField = createStyledTextField();
        tuNgayField.setText("01/01/2025 00:00:00");
        tuNgayErrorLabel = ValidationUtils.createErrorLabel();

        JLabel denNgayLabel = new JLabel("Đến ngày:");
        denNgayLabel.setFont(LABEL_FONT);
        denNgayField = createStyledTextField();
        denNgayField.setText("31/12/2025 23:59:59");
        denNgayErrorLabel = ValidationUtils.createErrorLabel();

        JButton xemButton = createStyledButton("Xem báo cáo");
        JButton xuatfileButton = createStyledButton("Xuất Excel");

        timePanel.add(tuNgayLabel);
        timePanel.add(tuNgayField);
        timePanel.add(tuNgayErrorLabel);
        timePanel.add(denNgayLabel);
        timePanel.add(denNgayField);
        timePanel.add(denNgayErrorLabel);
        timePanel.add(xemButton);
        timePanel.add(xuatfileButton);
        
        tableModel = new DefaultTableModel(new String[]{"Tên nhân viên", "Vai trò", "Số vé đã bán", "Tổng doanh thu"}, 0);
        JTable baoCaoTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(baoCaoTable);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel chứa biểu đồ
        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setOpaque(false);
        contentPanel.add(chartPanel, BorderLayout.SOUTH);

        add(timePanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        xemButton.addActionListener(_ -> xemBaoCao());
        xuatfileButton.addActionListener(_ -> xuatFile());
        tuNgayField.getDocument().addDocumentListener(new SimpleDocumentListener(this::validateDateFields));
        denNgayField.getDocument().addDocumentListener(new SimpleDocumentListener(this::validateDateFields));

        validateDateFields();
    }

    private void xemBaoCao() {
        try {
            LocalDateTime tuNgay = ValidationUtils.parseDateTime(tuNgayField.getText());
            LocalDateTime denNgay = ValidationUtils.parseDateTime(denNgayField.getText());

            if (!ValidationUtils.isValidDateRange(tuNgay, denNgay)) {
                JOptionPane.showMessageDialog(this, messages.getString("invalidDateRange"), messages.getString("error"), JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<BaoCaoNhanVien> baoCaoList = controller.getBaoCaoNhanVien(tuNgay, denNgay);
            tableModel.setRowCount(0);

            DefaultCategoryDataset revenueDataset = new DefaultCategoryDataset();
            DefaultCategoryDataset ticketsDataset = new DefaultCategoryDataset();

            if (baoCaoList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không có dữ liệu báo cáo trong khoảng thời gian này!", messages.getString("information"), JOptionPane.INFORMATION_MESSAGE);
                chartPanel.removeAll();
                chartPanel.revalidate();
                chartPanel.repaint();
                return;
            }

            for (BaoCaoNhanVien baoCao : baoCaoList) {
                tableModel.addRow(new Object[]{
                        baoCao.getTenNhanVien(),
                        baoCao.getVaiTro(),
                        baoCao.getSoVeDaBan(),
                        formatCurrency(baoCao.getTongDoanhThu())
                });
                revenueDataset.addValue(baoCao.getTongDoanhThu(), "Doanh thu", baoCao.getTenNhanVien());
                ticketsDataset.addValue(baoCao.getSoVeDaBan(), "Số vé bán ra", baoCao.getTenNhanVien());
            }

            JFreeChart chart = createLineChart(revenueDataset, ticketsDataset);
            chartPanel.removeAll();
            ChartPanel chartComponent = new ChartPanel(chart);
            chartComponent.setPreferredSize(new Dimension(800, 400));
            chartPanel.add(chartComponent, BorderLayout.CENTER);
            chartPanel.revalidate();
            chartPanel.repaint();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi lấy dữ liệu báo cáo: " + e.getMessage(), messages.getString("error"), JOptionPane.ERROR_MESSAGE);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Định dạng ngày không hợp lệ. Vui lòng nhập theo định dạng dd/MM/yyyy HH:mm:ss", messages.getString("error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuatFile() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu để xuất!", messages.getString("error"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn vị trí lưu file");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setSelectedFile(new File("BaoCaoNhanVien.xlsx"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().endsWith(".xlsx")) {
                file = new File(file.getAbsolutePath() + ".xlsx");
            }

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Báo cáo nhân viên");
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(tableModel.getColumnName(i));
                }
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    Row row = sheet.createRow(i + 1);
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        Cell cell = row.createCell(j);
                        Object value = tableModel.getValueAt(i, j);
                        if (value != null) {
                            cell.setCellValue(value.toString());
                        }
                    }
                }
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    sheet.autoSizeColumn(i);
                }
                try (FileOutputStream fileOut = new FileOutputStream(file)) {
                    workbook.write(fileOut);
                }
                JOptionPane.showMessageDialog(this, "Xuất file thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất file: " + e.getMessage(), messages.getString("error"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private JTextField createStyledTextField() {
        JTextField field = new JTextField(15);
        field.setFont(LABEL_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        return field;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void validateDateFields() {
        ValidationUtils.validateDateTimeField(tuNgayField, tuNgayErrorLabel, messages, "Ngày bắt đầu");
        ValidationUtils.validateDateTimeField(denNgayField, denNgayErrorLabel, messages, "Ngày kết thúc");
        if (!tuNgayErrorLabel.isVisible() && !denNgayErrorLabel.isVisible()) {
            try {
                LocalDateTime tuNgay = ValidationUtils.parseDateTime(tuNgayField.getText());
                LocalDateTime denNgay = ValidationUtils.parseDateTime(denNgayField.getText());
                if (!ValidationUtils.isValidDateRange(tuNgay, denNgay)) {
                    ValidationUtils.showError(denNgayErrorLabel, messages.getString("invalidDateRange"));
                    ValidationUtils.setErrorBorder(denNgayField);
                }
            } catch (DateTimeParseException e) {
                // Lỗi đã được xử lý
            }
        }
    }

    private String formatCurrency(double amount) {
        return String.format("%,.0f VND", amount);
    }

    private JFreeChart createLineChart(DefaultCategoryDataset revenueDataset, DefaultCategoryDataset ticketsDataset) {
        // Trục Doanh thu
        NumberAxis revenueAxis = new NumberAxis("Doanh thu (VND)");
        revenueAxis.setNumberFormatOverride(java.text.NumberFormat.getCurrencyInstance(new Locale("vi", "VN")));
        LineAndShapeRenderer revenueRenderer = new LineAndShapeRenderer();
        revenueRenderer.setSeriesPaint(0, PRIMARY_COLOR);
        revenueRenderer.setSeriesStroke(0, new java.awt.BasicStroke(2.5f));

        // Trục Số vé
        NumberAxis ticketsAxis = new NumberAxis("Số vé bán ra");
        ticketsAxis.setNumberFormatOverride(java.text.NumberFormat.getIntegerInstance());
        LineAndShapeRenderer ticketsRenderer = new LineAndShapeRenderer();
        ticketsRenderer.setSeriesPaint(0, new Color(249, 115, 22));
        ticketsRenderer.setSeriesStroke(0, new java.awt.BasicStroke(2.5f));
        
        // Tạo Plot
        CategoryPlot plot = new CategoryPlot();
        plot.setDataset(0, revenueDataset);
        plot.setRenderer(0, revenueRenderer);
        plot.setRangeAxis(0, revenueAxis);
        plot.mapDatasetToRangeAxis(0, 0);

        plot.setDataset(1, ticketsDataset);
        plot.setRenderer(1, ticketsRenderer);
        plot.setRangeAxis(1, ticketsAxis);
        plot.mapDatasetToRangeAxis(1, 1);

        plot.setDomainAxis(new CategoryAxis("Nhân viên"));
        plot.setBackgroundPaint(new Color(248, 250, 252));
        plot.setRangeGridlinePaint(new Color(229, 231, 235));
        
        JFreeChart chart = new JFreeChart(
            "Hiệu suất nhân viên",
            new Font("Inter", Font.BOLD, 18),
            plot,
            true
        );
        chart.setBackgroundPaint(Color.WHITE);
        return chart;
    }
}
