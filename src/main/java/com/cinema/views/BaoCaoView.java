package com.cinema.views;

import com.cinema.controllers.BaoCaoController;
import com.cinema.controllers.NhanVienController;
import com.cinema.models.BaoCao;
import com.cinema.services.BaoCaoService;
import com.cinema.services.NhanVienService;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.ValidationUtils;
import org.apache.poi.sl.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class BaoCaoView extends JPanel {
    private DatabaseConnection databaseConnection;
    private BaoCaoController controller;
    private JTextField tuNgayField;
    private JTextField denNgayField;
    private DefaultTableModel tableModel;

    public BaoCaoView() {
        try {
            databaseConnection = new DatabaseConnection();
            controller = new BaoCaoController(new BaoCaoService(databaseConnection));
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Không thể đọc file cấu hình cơ sở dữ liệu!");
            System.exit(1);
        }
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Panel chọn khoảng thời gian
        JPanel timePanel = new JPanel(new FlowLayout());
        JLabel tuNgayLabel = new JLabel("Từ ngày:");
        tuNgayField = new JTextField("2025-04-06 00:00", 15);
        JLabel denNgayLabel = new JLabel("Đến ngày:");
        denNgayField = new JTextField("2025-04-07 23:59", 15);
        JButton xemButton = new JButton("Xem báo cáo");
        JButton xuatfileButton = new JButton("Xuất CSV"); // Nút xuất CSV
        timePanel.add(tuNgayLabel);
        timePanel.add(tuNgayField);
        timePanel.add(denNgayLabel);
        timePanel.add(denNgayField);
        timePanel.add(xemButton);
        timePanel.add(xuatfileButton);

        // Bảng hiển thị báo cáo
        tableModel = new DefaultTableModel(new String[]{
                "Tên phim", "Số vé bán ra", "Tổng doanh thu"
        }, 0);
        JTable baoCaoTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(baoCaoTable);

        // Sự kiện
        xemButton.addActionListener(_ -> xemBaoCao());
        xuatfileButton.addActionListener(e -> xuatFile());

        // Thêm các thành phần vào panel
        add(timePanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void xemBaoCao(){
        try {
            // Validate ngày giờ
            LocalDateTime tuNgay = ValidationUtils.validateDateTime(tuNgayField.getText(), "Ngày bắt đầu");
            LocalDateTime denNgay = ValidationUtils.validateDateTime(denNgayField.getText(), "Ngày kết thúc");

            // Kiểm tra logic thời gian
            ValidationUtils.validateDateRange(tuNgay, denNgay);

            // Lấy dữ liệu báo cáo
            List<BaoCao> baoCaoList = controller.getBaoCaoDoanhThuTheoPhim(tuNgay, denNgay);
            tableModel.setRowCount(0); // Xóa dữ liệu cũ
            for (BaoCao baoCao : baoCaoList) {
                tableModel.addRow(new Object[]{
                        baoCao.getTenPhim(),
                        baoCao.getSoVeBanRa(),
                        baoCao.getTongDoanhThu()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi tải báo cáo: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Định dạng ngày không hợp lệ! (yyyy-MM-dd HH:mm)",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuatFile(){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn vị trí lưu file Excel");
        fileChooser.setSelectedFile(new File("BaoCaoDoanhThu.xlsx"));
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                exportToExcel(fileToSave);
                JOptionPane.showMessageDialog(this,
                        "Xuất file Excel thành công!",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Lỗi khi xuất file Excel: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportToExcel(File file) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Báo cáo Doanh thu");

        // Tạo style cho header
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        // Ghi header
        Row headerRow = sheet.createRow(0);
        for (int col = 0; col < tableModel.getColumnCount(); col++) {
            Cell cell = headerRow.createCell(col);
            cell.setCellValue(tableModel.getColumnName(col));
            cell.setCellStyle(headerStyle);
        }

        // Ghi dữ liệu
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            Row dataRow = sheet.createRow(row + 1);
            for (int col = 0; col < tableModel.getColumnCount(); col++) {
                Cell cell = dataRow.createCell(col);
                Object value = tableModel.getValueAt(row, col);

                if (value instanceof String) {
                    cell.setCellValue((String) value);
                } else if (value instanceof Integer) {
                    cell.setCellValue((Integer) value);
                } else if (value instanceof Double) {
                    cell.setCellValue((Double) value);
                } else if (value != null) {
                    cell.setCellValue(value.toString()); // fallback cho các kiểu khác
                }
            }
        }

        // Tự động căn chỉnh độ rộng cột
        for (int col = 0; col < tableModel.getColumnCount(); col++) {
            sheet.autoSizeColumn(col);
        }

        // Ghi ra file
        try (FileOutputStream fileOut = new FileOutputStream(file)) {
            workbook.write(fileOut);
        }

        workbook.close();
    }
}