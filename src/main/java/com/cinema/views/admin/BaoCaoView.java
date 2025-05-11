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
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import com.cinema.controllers.BaoCaoController;
import com.cinema.models.BaoCao;
import com.cinema.models.repositories.BaoCaoRepository;
import com.cinema.services.BaoCaoService;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.SimpleDocumentListener;
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

        // Panel chọn khoảng thời gian
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

        // Panel chứa bảng và biểu đồ
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Bảng hiển thị báo cáo
        tableModel = new DefaultTableModel(new String[]{
                "Tên phim", "Số vé bán ra", "Tổng doanh thu", "Điểm đánh giá TB"
        }, 0);
        baoCaoTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(baoCaoTable);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel chứa biểu đồ
        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setOpaque(false);
        contentPanel.add(chartPanel, BorderLayout.SOUTH);

        // Sự kiện
        xemButton.addActionListener(_ -> xemBaoCao());
        xuatfileButton.addActionListener(_ -> xuatFile());

        // Thêm kiểm tra real-time
        tuNgayField.getDocument().addDocumentListener(new SimpleDocumentListener(this::validateDateFields));
        denNgayField.getDocument().addDocumentListener(new SimpleDocumentListener(this::validateDateFields));

        // Thêm các thành phần vào panel
        add(timePanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        // Kiểm tra ban đầu
        validateDateFields();
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
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            if (baoCaoList.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Không có dữ liệu báo cáo trong khoảng thời gian này!",
                        messages.getString("error"), JOptionPane.INFORMATION_MESSAGE);
            }
            for (BaoCao baoCao : baoCaoList) {
                tableModel.addRow(new Object[]{
                        baoCao.getTenPhim(),
                        baoCao.getSoVeBanRa(),
                        formatCurrency(baoCao.getTongDoanhThu()),
                        formatRating(baoCao.getDiemDanhGiaTrungBinh())
                });
                dataset.addValue(baoCao.getTongDoanhThu(), "Doanh thu", baoCao.getTenPhim());
            }

            // Tạo và hiển thị biểu đồ
            JFreeChart barChart = ChartFactory.createBarChart(
                    "Doanh thu theo phim",
                    "Tên phim", "Doanh thu (VND)",
                    dataset, PlotOrientation.VERTICAL,
                    false, true, false
            );
            ChartPanel chart = new ChartPanel(barChart);
            chart.setPreferredSize(new Dimension(800, 300));
            chartPanel.removeAll();
            chartPanel.add(chart, BorderLayout.CENTER);
            chartPanel.revalidate();
            chartPanel.repaint();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    messages.getString("databaseError") + ex.getMessage(),
                    messages.getString("error"), JOptionPane.ERROR_MESSAGE);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    messages.getString("invalidDateFormat"),
                    messages.getString("error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuatFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn vị trí lưu file Excel");
        fileChooser.setSelectedFile(new File("BaoCaoDoanhThu.xlsx"));
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                exportToExcel(fileToSave);
                JOptionPane.showMessageDialog(this,
                        messages.getString("exportSuccess"),
                        messages.getString("success"), JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        messages.getString("exportError") + ex.getMessage(),
                        messages.getString("error"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportToExcel(File file) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Báo cáo Doanh thu");

            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < tableModel.getColumnCount(); col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(tableModel.getColumnName(col));
                cell.setCellStyle(headerStyle);
            }

            for (int row = 0; row < tableModel.getRowCount(); row++) {
                Row dataRow = sheet.createRow(row + 1);
                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    Cell cell = dataRow.createCell(col);
                    Object value = tableModel.getValueAt(row, col);
                    if (value instanceof String string) {
                        cell.setCellValue(string);
                    } else if (value instanceof Integer integer) {
                        cell.setCellValue(integer);
                    } else if (value instanceof Double aDouble) {
                        cell.setCellValue(aDouble);
                    } else if (value != null) {
                        cell.setCellValue(value.toString());
                    }
                }
            }

            for (int col = 0; col < tableModel.getColumnCount(); col++) {
                sheet.autoSizeColumn(col);
            }

            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
            }
        }
    }
}