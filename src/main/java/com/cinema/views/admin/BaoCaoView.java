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

            // Tạo biểu đồ
            JFreeChart chart = ChartFactory.createBarChart(
                    "Biểu đồ doanh thu theo phim",
                    "Tên phim",
                    "Doanh thu (VND)",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );

            // Hiển thị biểu đồ
            chartPanel.removeAll();
            ChartPanel chartComponent = new ChartPanel(chart);
            chartComponent.setPreferredSize(new Dimension(800, 400));
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
}