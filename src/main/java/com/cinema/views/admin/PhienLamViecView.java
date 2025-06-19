package com.cinema.views.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.cinema.controllers.PhienLamViecController;
import com.cinema.models.PhienLamViec;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.DateTimeFormatter;
import com.cinema.views.common.ResizableView;

public class PhienLamViecView extends JPanel implements ResizableView {
    private static final Color CINESTAR_BLUE = new Color(0, 51, 102);
    private static final Color CINESTAR_YELLOW = new Color(255, 204, 0);
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    
    private DatabaseConnection databaseConnection;
    private PhienLamViecController controller;
    private JTable phienLamViecTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> filterComboBox;
    private JDateChooser fromDateChooser;
    private JDateChooser toDateChooser;
    private JButton searchButton;
    private JButton refreshButton;
    
    public PhienLamViecView() throws SQLException {
        initializeDatabase();
        initComponents();
        controller = new PhienLamViecController(databaseConnection);
        loadData();
    }
    
    private void initializeDatabase() {
        try {
            databaseConnection = new DatabaseConnection();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Không thể kết nối cơ sở dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // North Panel - Filter options
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBackground(BACKGROUND_COLOR);
        
        JLabel filterLabel = new JLabel("Lọc theo:");
        filterComboBox = new JComboBox<>(new String[]{"Tất cả", "Hôm nay", "Tuần này", "Tháng này"});
        
        JLabel fromLabel = new JLabel("Từ ngày:");
        fromDateChooser = new JDateChooser();
        fromDateChooser.setDate(java.sql.Date.valueOf(LocalDate.now().minusMonths(1)));
        fromDateChooser.setPreferredSize(new Dimension(120, 25));
        
        JLabel toLabel = new JLabel("Đến ngày:");
        toDateChooser = new JDateChooser();
        toDateChooser.setDate(java.sql.Date.valueOf(LocalDate.now()));
        toDateChooser.setPreferredSize(new Dimension(120, 25));
        
        searchButton = new JButton("Tìm kiếm");
        searchButton.setBackground(CINESTAR_BLUE);
        searchButton.setForeground(Color.WHITE);
        
        refreshButton = new JButton("Làm mới");
        refreshButton.setBackground(CINESTAR_YELLOW);
        refreshButton.setForeground(CINESTAR_BLUE);
        
        filterPanel.add(filterLabel);
        filterPanel.add(filterComboBox);
        filterPanel.add(fromLabel);
        filterPanel.add(fromDateChooser);
        filterPanel.add(toLabel);
        filterPanel.add(toDateChooser);
        filterPanel.add(searchButton);
        filterPanel.add(refreshButton);
        
        // Center Panel - Table
        String[] columnNames = {"Mã phiên", "Mã nhân viên", "Thời gian bắt đầu", "Thời gian kết thúc", "Tổng doanh thu", "Số vé đã bán"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        phienLamViecTable = new JTable(tableModel);
        phienLamViecTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        phienLamViecTable.setRowHeight(25);
        phienLamViecTable.getTableHeader().setBackground(CINESTAR_BLUE);
        phienLamViecTable.getTableHeader().setForeground(Color.WHITE);
        phienLamViecTable.getTableHeader().setFont(new Font("Roboto", Font.BOLD, 14));
        
        JScrollPane scrollPane = new JScrollPane(phienLamViecTable);
        
        // Add components to main panel
        add(filterPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        // Add event listeners
        searchButton.addActionListener(_ -> searchPhienLamViec());
        refreshButton.addActionListener(_ -> loadData());
        filterComboBox.addActionListener(_ -> handleFilterChange());
    }
    
    public void loadData() {
        List<PhienLamViec> phienLamViecs = controller.getAllPhienLamViec();
        updateTable(phienLamViecs);
    }
    
    private void searchPhienLamViec() {
        LocalDateTime fromDate = LocalDateTime.of(
                fromDateChooser.getDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                LocalTime.MIN);
        LocalDateTime toDate = LocalDateTime.of(
                toDateChooser.getDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                LocalTime.MAX);
        
        List<PhienLamViec> phienLamViecs = controller.getPhienLamViecByDateRange(fromDate, toDate);
        updateTable(phienLamViecs);
    }
    
    private void handleFilterChange() {
        String selectedFilter = (String) filterComboBox.getSelectedItem();
        LocalDateTime fromDate = null;
        LocalDateTime toDate = LocalDateTime.now();
        
        switch (selectedFilter) {
            case "Hôm nay":
                fromDate = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
                break;
            case "Tuần này":
                fromDate = LocalDateTime.of(LocalDate.now().minusDays(7), LocalTime.MIN);
                break;
            case "Tháng này":
                fromDate = LocalDateTime.of(LocalDate.now().minusMonths(1), LocalTime.MIN);
                break;
            default:
                loadData();
                return;
        }
        
        List<PhienLamViec> phienLamViecs = controller.getPhienLamViecByDateRange(fromDate, toDate);
        updateTable(phienLamViecs);
    }
    
    private void updateTable(List<PhienLamViec> phienLamViecs) {
        tableModel.setRowCount(0);
        
        for (PhienLamViec phien : phienLamViecs) {
            String thoiGianBatDau = DateTimeFormatter.formatDateTime(phien.getThoiGianBatDau());
            String thoiGianKetThuc = phien.getThoiGianKetThuc() != null ? 
                    DateTimeFormatter.formatDateTime(phien.getThoiGianKetThuc()) : "Đang hoạt động";
            
            Object[] rowData = {
                    phien.getMaPhien(),
                    phien.getMaNhanVien(),
                    thoiGianBatDau,
                    thoiGianKetThuc,
                    String.format("%,.0f VNĐ", phien.getTongDoanhThu()),
                    phien.getSoVeDaBan()
            };
            
            tableModel.addRow(rowData);
        }
    }
    
    // Getters
    public DatabaseConnection getDatabaseConnection() { return databaseConnection; }
    public JTable getPhienLamViecTable() { return phienLamViecTable; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JComboBox<String> getFilterComboBox() { return filterComboBox; }
    public JDateChooser getFromDateChooser() { return fromDateChooser; }
    public JDateChooser getToDateChooser() { return toDateChooser; }
    public JButton getSearchButton() { return searchButton; }
    public JButton getRefreshButton() { return refreshButton; }

    @Override
    public Dimension getPreferredViewSize() {
        return new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    @Override
    public Dimension getMinimumViewSize() {
        return new Dimension(MIN_WIDTH, MIN_HEIGHT);
    }

    @Override
    public boolean needsScrolling() {
        // Cần scroll vì có bảng danh sách phiên làm việc
        return true;
    }
}