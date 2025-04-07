package com.cinema.views;

import com.cinema.controllers.BaoCaoController;
import com.cinema.controllers.NhanVienController;
import com.cinema.models.BaoCao;
import com.cinema.services.BaoCaoService;
import com.cinema.services.NhanVienService;
import com.cinema.utils.DatabaseConnection;
import org.apache.poi.sl.usermodel.*;
import org.apache.poi.sl.usermodel.Sheet;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
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
        JButton exportButton = new JButton("Xuất CSV"); // Nút xuất CSV
        timePanel.add(tuNgayLabel);
        timePanel.add(tuNgayField);
        timePanel.add(denNgayLabel);
        timePanel.add(denNgayField);
        timePanel.add(xemButton);
        timePanel.add(exportButton);

        // Bảng hiển thị báo cáo
        tableModel = new DefaultTableModel(new String[]{
                "Tên phim", "Số vé bán ra", "Tổng doanh thu"
        }, 0);
        JTable baoCaoTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(baoCaoTable);

        // Sự kiện nút "Xem báo cáo"
        xemButton.addActionListener(_ -> {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                LocalDateTime tuNgay = LocalDateTime.parse(tuNgayField.getText(), formatter);
                LocalDateTime denNgay = LocalDateTime.parse(denNgayField.getText(), formatter);
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
        });

        // Sự kiện nút "Xuất CSV"
        exportButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Chọn vị trí lưu file CSV");
            fileChooser.setSelectedFile(new File("BaoCaoDoanhThu.csv"));
            int userSelection = fileChooser.showSaveDialog(this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                try {
                    exportToCSV(fileToSave);
                    JOptionPane.showMessageDialog(this,
                            "Xuất file CSV thành công!",
                            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Lỗi khi xuất file CSV: " + ex.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Thêm các thành phần vào panel
        add(timePanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void exportToCSV(File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            // Ghi tiêu đề
            for (int col = 0; col < tableModel.getColumnCount(); col++) {
                writer.append(tableModel.getColumnName(col));
                if (col < tableModel.getColumnCount() - 1) {
                    writer.append(",");
                }
            }
            writer.append("\n");

            // Ghi dữ liệu
            for (int row = 0; row < tableModel.getRowCount(); row++) {
                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    Object value = tableModel.getValueAt(row, col);
                    if (value != null) {
                        writer.append(value.toString());
                    }
                    if (col < tableModel.getColumnCount() - 1) {
                        writer.append(",");
                    }
                }
                writer.append("\n");
            }
        }
    }
}