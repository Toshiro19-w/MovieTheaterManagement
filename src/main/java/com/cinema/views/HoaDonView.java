package com.cinema.views;

import com.cinema.controllers.HoaDonController;
import com.cinema.models.HoaDon;
import com.cinema.models.Ve;
import com.cinema.services.HoaDonService;
import com.cinema.services.VeService;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.ValidationUtils;
import com.formdev.flatlaf.FlatLightLaf;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HoaDonView extends JPanel {
    BaoCaoView baoCaoView = new BaoCaoView();
    private HoaDonController hoaDonController;
    private JComboBox<String> tenKhachHangCombo; // Sử dụng JComboBox để gợi ý tên
    private JTextField maKhachHangField;
    private DefaultTableModel tableModel;
    private JComboBox<String> searchTypeCombo;
    private JTextField tenKhachHangField;
    private JTable hoaDonTable;
    private JPanel inputPanel;
    private CardLayout cardLayout;

    public HoaDonView() {
        try {
            DatabaseConnection databaseConnection = new DatabaseConnection();
            hoaDonController = new HoaDonController(new HoaDonService(databaseConnection),
                    new VeService(databaseConnection));
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể đọc file cấu hình cơ sở dữ liệu!");
            System.exit(1);
        }
        initUI();
    }

    private void initUI() {
        this.setLayout(new BorderLayout(10, 10));
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Form tìm kiếm
        JPanel formPanel = new JPanel(new BorderLayout(10, 10));

        // ComboBox chọn loại tìm kiếm
        JPanel searchTypePanel = new JPanel(new FlowLayout());
        searchTypePanel.add(new JLabel("Tìm kiếm theo:"));
        searchTypeCombo = new JComboBox<>(new String[]{"Mã khách hàng", "Tên khách hàng"});
        searchTypePanel.add(searchTypeCombo);
        formPanel.add(searchTypePanel, BorderLayout.NORTH);

        // Panel chứa các trường nhập liệu (sử dụng CardLayout)
        cardLayout = new CardLayout();
        inputPanel = new JPanel(cardLayout);

        // Trường nhập mã khách hàng
        JPanel maKhachHangPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        maKhachHangPanel.add(new JLabel("Mã khách hàng:"));
        maKhachHangField = new JTextField();
        maKhachHangPanel.add(maKhachHangField);
        inputPanel.add(maKhachHangPanel, "MaKhachHang");

        // Trường nhập tên khách hàng (dùng JComboBox để gợi ý)
        JPanel tenKhachHangPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        tenKhachHangPanel.add(new JLabel("Tên khách hàng:"));
        tenKhachHangCombo = new JComboBox<>();
        tenKhachHangCombo.setEditable(true);
        loadTenKhachHang(); // Tải danh sách tên khách hàng
        tenKhachHangPanel.add(tenKhachHangCombo);
        inputPanel.add(tenKhachHangPanel, "TenKhachHang");

        formPanel.add(inputPanel, BorderLayout.CENTER);
        this.add(formPanel, BorderLayout.NORTH);

        // Bảng hiển thị hóa đơn và vé
        tableModel = new DefaultTableModel(
                new String[]{"Mã hóa đơn", "Ngày lập", "Tổng tiền", "Mã vé", "Ghế", "Giá vé"}, 0);
        hoaDonTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(hoaDonTable);
        this.add(scrollPane, BorderLayout.CENTER);

        // Nút chức năng
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton xemLichSuButton = new JButton("Xem lịch sử hóa đơn");
        JButton exportButton = new JButton("Xuất Excel");
        xemLichSuButton.addActionListener(_ -> xemLichSu());
        exportButton.addActionListener(_ -> exportToExcel());
        buttonPanel.add(xemLichSuButton);
        buttonPanel.add(exportButton);
        this.add(buttonPanel, BorderLayout.SOUTH);

        // Sự kiện thay đổi loại tìm kiếm
        searchTypeCombo.addActionListener(e -> {
            String selectedType = (String) searchTypeCombo.getSelectedItem();
            if ("Mã khách hàng".equals(selectedType)) {
                cardLayout.show(inputPanel, "MaKhachHang");
            } else {
                cardLayout.show(inputPanel, "TenKhachHang");
            }
        });

        // Mặc định hiển thị trường mã khách hàng
        cardLayout.show(inputPanel, "MaKhachHang");
    }

    private String formatCurrency(BigDecimal amount) {
        return String.format("%,.0f VND", amount);
    }

    private void loadTenKhachHang() {
        List<String> tenKhachHangList = hoaDonController.getAllTenKhachHang();
        for (String ten : tenKhachHangList) {
            tenKhachHangCombo.addItem(ten);
        }
    }

    private void xemLichSu() {
            String selectedType = (String) searchTypeCombo.getSelectedItem();
            List<HoaDon> hoaDonList;
            DateTimeFormatter ngayLapFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

            if ("Mã khách hàng".equals(selectedType)) {
                // Tìm kiếm bằng mã khách hàng
                String maKhachHangStr = maKhachHangField.getText();
                if (!ValidationUtils.isValidString(maKhachHangStr)) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập mã khách hàng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int maKhachHang = Integer.parseInt(maKhachHangStr);
                ValidationUtils.validatePositive(maKhachHang, "Mã khách hàng phải là số dương");
                hoaDonList = hoaDonController.getLichSuHoaDon(maKhachHang);
            } else {
                // Tìm kiếm bằng tên khách hàng
                String tenKhachHang = (String) tenKhachHangCombo.getSelectedItem();
                if (!ValidationUtils.isValidString(tenKhachHang)) {
                    JOptionPane.showMessageDialog(this, "Vui lòng chọn hoặc nhập tên khách hàng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                hoaDonList = hoaDonController.getLichSuHoaDonByTenKhachHang(tenKhachHang);
            }

            if (hoaDonList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy hóa đơn nào cho khách hàng này.");
                return;
            }

            // Xóa dữ liệu cũ trong bảng
            tableModel.setRowCount(0);

            // Thêm dữ liệu vào bảng
            for (HoaDon hd : hoaDonList) {
                List<Ve> veList = hoaDonController.getVeByHoaDon(hd.getMaHoaDon());
                if (veList.isEmpty()) {
                    tableModel.addRow(new Object[]{
                            hd.getMaHoaDon(),
                            hd.getNgayLap().format(ngayLapFormatter),
                            formatCurrency(hd.getTongTien()),
                            "", "", ""
                    });
                } else {
                    for (Ve ve : veList) {
                        tableModel.addRow(new Object[]{
                                hd.getMaHoaDon(),
                                hd.getNgayLap().format(ngayLapFormatter),
                                formatCurrency(hd.getTongTien()),
                                ve.getMaVe(),
                                ve.getSoGhe(),
                                formatCurrency(ve.getGiaVe())
                        });
                    }
                }
            }
    }

    private void exportToExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn vị trí lưu file Excel");
        fileChooser.setSelectedFile(new File("LichSuHoaDon.xlsx"));
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                Workbook workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet("Lịch sử hóa đơn");

                Row headerRow = sheet.createRow(0);
                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    Cell cell = headerRow.createCell(col);
                    cell.setCellValue(tableModel.getColumnName(col));
                    CellStyle headerStyle = workbook.createCellStyle();
                    Font font = workbook.createFont();
                    font.setBold(true);
                    headerStyle.setFont(font);
                    cell.setCellStyle(headerStyle);
                }

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
                            cell.setCellValue(value.toString());
                        }
                    }
                }

                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    sheet.autoSizeColumn(col);
                }

                try (FileOutputStream fileOut = new FileOutputStream(fileToSave)) {
                    workbook.write(fileOut);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                workbook.close();

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
}