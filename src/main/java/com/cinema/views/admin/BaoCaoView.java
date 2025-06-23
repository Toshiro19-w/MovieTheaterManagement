package com.cinema.views.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTabbedPane;
import javax.swing.table.DefaultTableModel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import com.cinema.controllers.BaoCaoController;
import com.cinema.models.BaoCao;
import com.cinema.models.repositories.BaoCaoRepository;
import com.cinema.services.BaoCaoService;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.ValidationUtils;

public class BaoCaoView extends JPanel {
    private DatabaseConnection databaseConnection;
    private BaoCaoController controller;
    private BaoCaoRepository baoCaoRepository;
    private JTextField tuNgayField;
    private JTextField denNgayField;
    private JLabel tuNgayErrorLabel;
    private JLabel denNgayErrorLabel;
    private DefaultTableModel tableModel;
    private JTable baoCaoTable;
    private JPanel chartPanel;
    private ResourceBundle messages;

    private static final Font LABEL_FONT = new Font("Inter", Font.PLAIN, 14);
    private static final Font BUTTON_FONT = new Font("Inter", Font.BOLD, 14);
    private static final Color PRIMARY_COLOR = new Color(59, 130, 246);

    public BaoCaoView() {
        try {
            databaseConnection = new DatabaseConnection();
            baoCaoRepository = new BaoCaoRepository(databaseConnection);
            controller = new BaoCaoController(new BaoCaoService(databaseConnection));
            messages = ResourceBundle.getBundle("Messages");
            if (!baoCaoRepository.isViewExists()) {
                JOptionPane.showMessageDialog(this,
                        "View ThongKeDoanhThuPhim không tồn tại! Vui lòng tạo view trước.",
                        messages.getString("error"), JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, messages.getString("dbConnectionError"), messages.getString("error"), JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));

        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Tab for Movie Revenue Report
        tabbedPane.addTab("Báo cáo doanh thu phim", new MovieRevenueReportPanel());
        
        // Tab for Employee Report
        tabbedPane.addTab("Báo cáo nhân viên", new EmployeeReportPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
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
                // Lỗi đã được xử lý trong validateDateTimeField
            }
        }
    }

    private String formatCurrency(double amount) {
        return String.format("%,.0f VND", amount);
    }

    private String formatRating(double rating) {
        return String.format("%.1f", rating);
    }

    private void xemBaoCao() {
        try {
            LocalDateTime tuNgay = ValidationUtils.parseDateTime(tuNgayField.getText());
            LocalDateTime denNgay = ValidationUtils.parseDateTime(denNgayField.getText());

            if (!ValidationUtils.isValidDateRange(tuNgay, denNgay)) {
                JOptionPane.showMessageDialog(this,
                        messages.getString("invalidDateRange"),
                        messages.getString("error"), JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<BaoCao> baoCaoList = controller.getBaoCaoDoanhThuTheoPhim(tuNgay, denNgay);
            tableModel.setRowCount(0);

            if (baoCaoList.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Không có dữ liệu báo cáo trong khoảng thời gian này!",
                        messages.getString("information"), JOptionPane.INFORMATION_MESSAGE);
                chartPanel.removeAll();
                chartPanel.revalidate();
                chartPanel.repaint();
                return;
            }

            DefaultCategoryDataset revenueDataset = new DefaultCategoryDataset();
            DefaultCategoryDataset ticketsDataset = new DefaultCategoryDataset();

            for (BaoCao baoCao : baoCaoList) {
                tableModel.addRow(new Object[]{
                        baoCao.getTenPhim(),
                        baoCao.getSoVeBanRa(),
                        formatCurrency(baoCao.getTongDoanhThu()),
                        formatRating(baoCao.getDiemDanhGiaTrungBinh())
                });
                revenueDataset.addValue(baoCao.getTongDoanhThu(), "Doanh thu", baoCao.getTenPhim());
                ticketsDataset.addValue(baoCao.getSoVeBanRa(), "Số vé bán ra", baoCao.getTenPhim());
            }

            // Tạo biểu đồ chuyên nghiệp
            JFreeChart chart = createProfessionalChart(revenueDataset, ticketsDataset, baoCaoList);

            // Hiển thị biểu đồ
            chartPanel.removeAll();
            ChartPanel chartComponent = new ChartPanel(chart);
            chartComponent.setPreferredSize(new Dimension(800, 400));
            chartComponent.setMouseWheelEnabled(true);
            chartPanel.add(chartComponent, BorderLayout.CENTER);
            chartPanel.revalidate();
            chartPanel.repaint();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi lấy dữ liệu báo cáo: " + e.getMessage(),
                    messages.getString("error"), JOptionPane.ERROR_MESSAGE);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,
                    "Định dạng ngày không hợp lệ. Vui lòng nhập theo định dạng dd/MM/yyyy HH:mm:ss",
                    messages.getString("error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private JFreeChart createProfessionalChart(DefaultCategoryDataset revenueDataset,
            DefaultCategoryDataset ticketsDataset, List<BaoCao> baoCaoList) {
        // C trục tung (Y) cho doanh thu
        NumberAxis revenueAxis = new NumberAxis("Doanh thu");
        revenueAxis.setNumberFormatOverride(java.text.NumberFormat.getCurrencyInstance());
        
        // C renderer cho doanh thu (biểu đồ cột)
        BarRenderer revenueRenderer = new BarRenderer();
        revenueRenderer.setSeriesPaint(0, PRIMARY_COLOR);
        revenueRenderer.setDrawBarOutline(false);
        revenueRenderer.setDefaultToolTipGenerator(new CustomToolTipGenerator(baoCaoList));

        // C trục tung (Y) cho số vé bán ra
        NumberAxis ticketsAxis = new NumberAxis("Số vé bán ra");
        ticketsAxis.setNumberFormatOverride(java.text.NumberFormat.getIntegerInstance());

        // C renderer cho số vé (biểu đồ đường)
        LineAndShapeRenderer ticketsRenderer = new LineAndShapeRenderer();
        ticketsRenderer.setSeriesPaint(0, new Color(249, 115, 22)); // Orange color
        ticketsRenderer.setSeriesStroke(0, new java.awt.BasicStroke(2.5f));
        ticketsRenderer.setDefaultToolTipGenerator(new CustomToolTipGenerator(baoCaoList));

        // Tạo plot chính và kết hợp các thành phần
        CategoryPlot plot = new CategoryPlot();
        plot.setDataset(0, revenueDataset);
        plot.setRenderer(0, revenueRenderer);
        plot.setRangeAxis(0, revenueAxis);

        plot.setDataset(1, ticketsDataset);
        plot.setRenderer(1, ticketsRenderer);
        plot.setRangeAxis(1, ticketsAxis);

        plot.mapDatasetToRangeAxis(0, 0);
        plot.mapDatasetToRangeAxis(1, 1);
        
        CategoryAxis domainAxis = new CategoryAxis("Phim");
        plot.setDomainAxis(domainAxis);
        plot.setOrientation(PlotOrientation.VERTICAL);
        
        plot.setBackgroundPaint(new Color(248, 250, 252));
        plot.setRangeGridlinePaint(new Color(229, 231, 235));
        
        // Tạo biểu đồ
        JFreeChart chart = new JFreeChart(
            "Biểu đồ doanh thu và lượng vé bán ra",
            new Font("Inter", Font.BOLD, 18),
            plot,
            true
        );
        chart.setBackgroundPaint(Color.WHITE);
        
        return chart;
    }

    private void xuatFile() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Không có dữ liệu để xuất!",
                    messages.getString("error"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn vị trí lưu file");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setSelectedFile(new File("BaoCaoDoanhThuPhim.xlsx"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().endsWith(".xlsx")) {
                file = new File(file.getAbsolutePath() + ".xlsx");
            }

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Báo cáo doanh thu phim");

                // Tạo header
                Row headerRow = sheet.createRow(0);
                CellStyle headerStyle = workbook.createCellStyle();
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(tableModel.getColumnName(i));
                    cell.setCellStyle(headerStyle);
                }

                // Thêm dữ liệu
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

                // Tự động điều chỉnh độ rộng cột
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    sheet.autoSizeColumn(i);
                }

                // Ghi file
                try (FileOutputStream fileOut = new FileOutputStream(file)) {
                    workbook.write(fileOut);
                }

                JOptionPane.showMessageDialog(this,
                        "Xuất file thành công!",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Lỗi khi xuất file: " + e.getMessage(),
                        messages.getString("error"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static class CustomToolTipGenerator extends StandardCategoryToolTipGenerator {
        private final Map<String, BaoCao> baoCaoData;

        public CustomToolTipGenerator(List<BaoCao> baoCaoList) {
            this.baoCaoData = new HashMap<>();
            for (BaoCao baoCao : baoCaoList) {
                baoCaoData.put(baoCao.getTenPhim(), baoCao);
            }
        }

        @Override
        public String generateToolTip(CategoryDataset dataset, int row, int column) {
            String tenPhim = (String) dataset.getColumnKey(column);
            BaoCao baoCao = baoCaoData.get(tenPhim);

            if (baoCao == null) {
                return null; // or a default tooltip
            }

            StringBuilder sb = new StringBuilder();
            sb.append("<html><b>").append(baoCao.getTenPhim()).append("</b><br>");
            sb.append("Doanh thu: ").append(String.format("%,.0f VND", baoCao.getTongDoanhThu())).append("<br>");
            sb.append("Số vé bán ra: ").append(baoCao.getSoVeBanRa()).append("<br>");
            sb.append("Đánh giá TB: ").append(String.format("%.1f", baoCao.getDiemDanhGiaTrungBinh()))
                    .append("</html>");

            return sb.toString();
        }
    }
}