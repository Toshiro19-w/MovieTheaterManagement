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

import com.cinema.controllers.LichSuGiaVeController;
import com.cinema.models.LichSuGiaVe;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.DateTimeFormatter;

public class LichSuGiaVeView extends JPanel {
    private static final Color CINESTAR_BLUE = new Color(0, 51, 102);
    private static final Color CINESTAR_YELLOW = new Color(255, 204, 0);
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    
    private DatabaseConnection databaseConnection;
    private LichSuGiaVeController controller;
    private JTable lichSuGiaVeTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> loaiGheComboBox;
    private JDateChooser fromDateChooser;
    private JDateChooser toDateChooser;
    private JButton searchButton;
    private JButton refreshButton;
    
    public LichSuGiaVeView() throws SQLException {
        initializeDatabase();
        initComponents();
        controller = new LichSuGiaVeController(databaseConnection);
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
        
        JLabel loaiGheLabel = new JLabel("Loại ghế:");
        loaiGheComboBox = new JComboBox<>(new String[]{"Tất cả", "Thuong", "VIP"});
        
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
        
        filterPanel.add(loaiGheLabel);
        filterPanel.add(loaiGheComboBox);
        filterPanel.add(fromLabel);
        filterPanel.add(fromDateChooser);
        filterPanel.add(toLabel);
        filterPanel.add(toDateChooser);
        filterPanel.add(searchButton);
        filterPanel.add(refreshButton);
        
        // Center Panel - Table
        String[] columnNames = {"Mã lịch sử", "Loại ghế", "Giá vé cũ", "Giá vé mới", "Ngày thay đổi", "Người thay đổi"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        lichSuGiaVeTable = new JTable(tableModel);
        lichSuGiaVeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lichSuGiaVeTable.setRowHeight(25);
        lichSuGiaVeTable.getTableHeader().setBackground(CINESTAR_BLUE);
        lichSuGiaVeTable.getTableHeader().setForeground(Color.WHITE);
        lichSuGiaVeTable.getTableHeader().setFont(new Font("Roboto", Font.BOLD, 14));
        
        JScrollPane scrollPane = new JScrollPane(lichSuGiaVeTable);
        
        // Add components to main panel
        add(filterPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        // Add event listeners
        searchButton.addActionListener(e -> searchLichSuGiaVe());
        refreshButton.addActionListener(e -> loadData());
        loaiGheComboBox.addActionListener(e -> handleLoaiGheChange());
    }
    
    public void loadData() {
        try {
            List<LichSuGiaVe> lichSuList = controller.getAllLichSuGiaVe();
            updateTable(lichSuList);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void searchLichSuGiaVe() {
        try {
            LocalDateTime fromDate = LocalDateTime.of(
                    fromDateChooser.getDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                    LocalTime.MIN);
            LocalDateTime toDate = LocalDateTime.of(
                    toDateChooser.getDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                    LocalTime.MAX);
            
            List<LichSuGiaVe> lichSuList = controller.getLichSuGiaVeByTimeRange(fromDate, toDate);
            updateTable(lichSuList);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tìm kiếm: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void handleLoaiGheChange() {
        try {
            String selectedLoaiGhe = (String) loaiGheComboBox.getSelectedItem();
            
            if ("Tất cả".equals(selectedLoaiGhe)) {
                loadData();
                return;
            }
            
            List<LichSuGiaVe> lichSuList = controller.getLichSuGiaVeByLoaiGhe(selectedLoaiGhe);
            updateTable(lichSuList);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lọc dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateTable(List<LichSuGiaVe> lichSuList) {
        tableModel.setRowCount(0);
        
        for (LichSuGiaVe lichSu : lichSuList) {
            String ngayThayDoi = DateTimeFormatter.formatDateTime(lichSu.getNgayThayDoi());
            String nguoiThayDoi = lichSu.getNguoiThayDoi() != null ? String.valueOf(lichSu.getNguoiThayDoi()) : "N/A";
            
            Object[] rowData = {
                    lichSu.getMaLichSu(),
                    lichSu.getLoaiGhe(),
                    String.format("%,.0f VNĐ", lichSu.getGiaVeCu()),
                    String.format("%,.0f VNĐ", lichSu.getGiaVeMoi()),
                    ngayThayDoi,
                    nguoiThayDoi
            };
            
            tableModel.addRow(rowData);
        }
    }
    
    // Getters
    public DatabaseConnection getDatabaseConnection() { return databaseConnection; }
    public JTable getLichSuGiaVeTable() { return lichSuGiaVeTable; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JComboBox<String> getLoaiGheComboBox() { return loaiGheComboBox; }
    public JDateChooser getFromDateChooser() { return fromDateChooser; }
    public JDateChooser getToDateChooser() { return toDateChooser; }
    public JButton getSearchButton() { return searchButton; }
    public JButton getRefreshButton() { return refreshButton; }
}
