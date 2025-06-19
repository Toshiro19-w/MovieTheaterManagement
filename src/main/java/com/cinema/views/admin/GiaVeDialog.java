package com.cinema.views.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JCheckBox;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.cinema.components.DatePicker;
import com.cinema.components.UIConstants;
import com.cinema.controllers.GiaVeController;
import com.cinema.models.GiaVe;
import com.cinema.utils.ValidationUtils;

public class GiaVeDialog extends JDialog {
    private JTable tableGiaVe;
    private DefaultTableModel tableModel;
    private JTextField txtMaGiaVe, txtGiaVe, txtNgayApDung, txtNgayKetThuc, txtGhiChu;
    private JComboBox<String> cbLoaiGhe;
    private JButton btnThem, btnSua, btnXoa, btnLamMoi, btnDong;
    private JLabel giaVeErrorLabel, ngayApDungErrorLabel, ngayKetThucErrorLabel, loaiGheErrorLabel;
    private JCheckBox chkKhongGioiHan;
    private GiaVeController controller;
    private ResourceBundle messages;

    public GiaVeDialog(JFrame parent) throws IOException {
        super(parent, "Quản lý giá vé", true);
        messages = ResourceBundle.getBundle("Messages");
        initializeUI();
        try {
            controller = new GiaVeController(this);
            controller.loadGiaVeData();
        } catch (SQLException e) {
            showError("Lỗi kết nối cơ sở dữ liệu: " + e.getMessage());
        }
    }

    private void initializeUI() {
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        setBackground(UIConstants.BACKGROUND_COLOR);
        
        // Form panel
        JPanel formPanel = createFormPanel();
        
        // Table panel
        JPanel tablePanel = createTablePanel();
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        
        // Add panels to dialog
        add(formPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR),
            "Thông tin giá vé",
            TitledBorder.LEFT, TitledBorder.TOP,
            UIConstants.HEADER_FONT,
            UIConstants.TEXT_COLOR
        ));
        panel.setBackground(UIConstants.CARD_BACKGROUND);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Mã giá vé
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblMaGiaVe = new JLabel("Mã giá vé:");
        lblMaGiaVe.setFont(UIConstants.LABEL_FONT);
        panel.add(lblMaGiaVe, gbc);
        
        gbc.gridx = 1;
        txtMaGiaVe = new JTextField(10);
        txtMaGiaVe.setEditable(false);
        txtMaGiaVe.setFont(UIConstants.BODY_FONT);
        panel.add(txtMaGiaVe, gbc);
        
        // Loại ghế
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lblLoaiGhe = new JLabel("Loại ghế:");
        lblLoaiGhe.setFont(UIConstants.LABEL_FONT);
        panel.add(lblLoaiGhe, gbc);
        
        gbc.gridx = 1;
        cbLoaiGhe = new JComboBox<>(new String[]{"Thuong", "VIP"});
        cbLoaiGhe.setFont(UIConstants.BODY_FONT);
        panel.add(cbLoaiGhe, gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        loaiGheErrorLabel = new JLabel("");
        loaiGheErrorLabel.setForeground(UIConstants.ERROR_COLOR);
        loaiGheErrorLabel.setFont(UIConstants.SMALL_FONT);
        panel.add(loaiGheErrorLabel, gbc);
        
        // Giá vé
        gbc.gridx = 2; gbc.gridy = 0;
        JLabel lblGiaVe = new JLabel("Giá vé:");
        lblGiaVe.setFont(UIConstants.LABEL_FONT);
        panel.add(lblGiaVe, gbc);
        
        gbc.gridx = 3;
        txtGiaVe = new JTextField(10);
        txtGiaVe.setFont(UIConstants.BODY_FONT);
        panel.add(txtGiaVe, gbc);
        
        gbc.gridx = 3; gbc.gridy = 1;
        giaVeErrorLabel = new JLabel("");
        giaVeErrorLabel.setForeground(UIConstants.ERROR_COLOR);
        giaVeErrorLabel.setFont(UIConstants.SMALL_FONT);
        panel.add(giaVeErrorLabel, gbc);
        
        // Ngày áp dụng
        gbc.gridx = 2; gbc.gridy = 2;
        JLabel lblNgayApDung = new JLabel("Ngày áp dụng:");
        lblNgayApDung.setFont(UIConstants.LABEL_FONT);
        panel.add(lblNgayApDung, gbc);
        
        gbc.gridx = 3;
        txtNgayApDung = new JTextField(10);
        txtNgayApDung.setFont(UIConstants.BODY_FONT);
        
        JPanel datePanel = new JPanel(new BorderLayout());
        datePanel.add(txtNgayApDung, BorderLayout.CENTER);
        
        JButton btnDatePicker = new JButton("...");
        btnDatePicker.addActionListener(e -> {
            DatePicker datePicker = new DatePicker(txtNgayApDung);
            datePicker.setVisible(true);
        });
        datePanel.add(btnDatePicker, BorderLayout.EAST);
        
        panel.add(datePanel, gbc);
        
        gbc.gridx = 3; gbc.gridy = 3;
        ngayApDungErrorLabel = new JLabel("");
        ngayApDungErrorLabel.setForeground(UIConstants.ERROR_COLOR);
        ngayApDungErrorLabel.setFont(UIConstants.SMALL_FONT);
        panel.add(ngayApDungErrorLabel, gbc);
        
        // Ngày kết thúc
        gbc.gridx = 2; gbc.gridy = 4;
        JLabel lblNgayKetThuc = new JLabel("Ngày kết thúc:");
        lblNgayKetThuc.setFont(UIConstants.LABEL_FONT);
        panel.add(lblNgayKetThuc, gbc);
        
        gbc.gridx = 3;
        JPanel endDatePanel = new JPanel(new BorderLayout());
        
        txtNgayKetThuc = new JTextField(10);
        txtNgayKetThuc.setFont(UIConstants.BODY_FONT);
        endDatePanel.add(txtNgayKetThuc, BorderLayout.CENTER);
        
        JButton btnEndDatePicker = new JButton("...");
        btnEndDatePicker.addActionListener(e -> {
            DatePicker datePicker = new DatePicker(txtNgayKetThuc);
            datePicker.setVisible(true);
        });
        endDatePanel.add(btnEndDatePicker, BorderLayout.EAST);
        
        panel.add(endDatePanel, gbc);
        
        gbc.gridx = 3; gbc.gridy = 5;
        ngayKetThucErrorLabel = new JLabel("");
        ngayKetThucErrorLabel.setForeground(UIConstants.ERROR_COLOR);
        ngayKetThucErrorLabel.setFont(UIConstants.SMALL_FONT);
        panel.add(ngayKetThucErrorLabel, gbc);
        
        gbc.gridx = 3; gbc.gridy = 6;
        chkKhongGioiHan = new JCheckBox("Không giới hạn thời gian");
        chkKhongGioiHan.setFont(UIConstants.BODY_FONT);
        chkKhongGioiHan.addActionListener(e -> {
            txtNgayKetThuc.setEnabled(!chkKhongGioiHan.isSelected());
            btnEndDatePicker.setEnabled(!chkKhongGioiHan.isSelected());
            if (chkKhongGioiHan.isSelected()) {
                txtNgayKetThuc.setText("");
                ValidationUtils.hideError(ngayKetThucErrorLabel);
                ValidationUtils.setNormalBorder(txtNgayKetThuc);
            }
        });
        panel.add(chkKhongGioiHan, gbc);
        
        // Ghi chú
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel lblGhiChu = new JLabel("Ghi chú:");
        lblGhiChu.setFont(UIConstants.LABEL_FONT);
        panel.add(lblGhiChu, gbc);
        
        gbc.gridx = 1;
        txtGhiChu = new JTextField(20);
        txtGhiChu.setFont(UIConstants.BODY_FONT);
        panel.add(txtGhiChu, gbc);
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        // Create table model
        String[] columns = {"Mã giá vé", "Loại ghế", "Giá vé", "Ngày áp dụng", "Ngày kết thúc", "Ghi chú"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableGiaVe = new JTable(tableModel);
        tableGiaVe.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableGiaVe.setRowHeight(30);
        tableGiaVe.setFont(UIConstants.BODY_FONT);
        tableGiaVe.getTableHeader().setFont(UIConstants.SUBHEADER_FONT);
        tableGiaVe.getTableHeader().setBackground(UIConstants.PRIMARY_COLOR);
        tableGiaVe.getTableHeader().setForeground(Color.WHITE);
        
        // Set cell renderer
        tableGiaVe.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (isSelected) {
                    c.setBackground(UIConstants.HOVER_COLOR);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : UIConstants.BLUE_LIGHTEST);
                }
                
                return c;
            }
        });
        
        // Add selection listener
        tableGiaVe.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableGiaVe.getSelectedRow() != -1) {
                int row = tableGiaVe.getSelectedRow();
                txtMaGiaVe.setText(tableGiaVe.getValueAt(row, 0).toString());
                cbLoaiGhe.setSelectedItem(tableGiaVe.getValueAt(row, 1).toString());
                txtGiaVe.setText(tableGiaVe.getValueAt(row, 2).toString().replace(",", "").replace("VND", "").trim());
                txtNgayApDung.setText(tableGiaVe.getValueAt(row, 3).toString());
                
                // Xử lý ngày kết thúc
                Object ngayKetThucValue = tableGiaVe.getValueAt(row, 4);
                if (ngayKetThucValue != null && !ngayKetThucValue.toString().equals("Không giới hạn")) {
                    txtNgayKetThuc.setText(ngayKetThucValue.toString());
                    chkKhongGioiHan.setSelected(false);
                    txtNgayKetThuc.setEnabled(true);
                } else {
                    txtNgayKetThuc.setText("");
                    chkKhongGioiHan.setSelected(true);
                    txtNgayKetThuc.setEnabled(false);
                }
                
                txtGhiChu.setText(tableGiaVe.getValueAt(row, 5) != null ? tableGiaVe.getValueAt(row, 5).toString() : "");
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(tableGiaVe);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        btnThem = createButton("Thêm", UIConstants.SUCCESS_COLOR);
        btnSua = createButton("Sửa", UIConstants.PRIMARY_COLOR);
        btnXoa = createButton("Xóa", UIConstants.ERROR_COLOR);
        btnLamMoi = createButton("Làm mới", UIConstants.INFO_COLOR);
        btnDong = createButton("Đóng", UIConstants.SECONDARY_COLOR);
        
        panel.add(btnThem);
        panel.add(btnSua);
        panel.add(btnXoa);
        panel.add(btnLamMoi);
        panel.add(btnDong);
        
        // Add action listeners
        btnThem.addActionListener(e -> {
            if (validateForm()) {
                try {
                    GiaVe giaVe = getGiaVeFromForm();
                    controller.addGiaVe(giaVe);
                    clearForm();
                } catch (Exception ex) {
                    showError("Lỗi khi thêm giá vé: " + ex.getMessage());
                }
            }
        });
        
        btnSua.addActionListener(e -> {
            if (tableGiaVe.getSelectedRow() == -1) {
                showError("Vui lòng chọn giá vé cần sửa");
                return;
            }
            
            if (validateForm()) {
                try {
                    GiaVe giaVe = getGiaVeFromForm();
                    controller.updateGiaVe(giaVe);
                    clearForm();
                } catch (Exception ex) {
                    showError("Lỗi khi cập nhật giá vé: " + ex.getMessage());
                }
            }
        });
        
        btnXoa.addActionListener(e -> {
            if (tableGiaVe.getSelectedRow() == -1) {
                showError("Vui lòng chọn giá vé cần xóa");
                return;
            }
            
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn xóa giá vé này không?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION
            );
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    int maGiaVe = Integer.parseInt(txtMaGiaVe.getText());
                    controller.deleteGiaVe(maGiaVe);
                    clearForm();
                } catch (Exception ex) {
                    showError("Lỗi khi xóa giá vé: " + ex.getMessage());
                }
            }
        });
        
        btnLamMoi.addActionListener(e -> clearForm());
        
        btnDong.addActionListener(e -> dispose());
        
        // Add validation listeners
        addValidationListeners();
        
        return panel;
    }
    
    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(UIConstants.BUTTON_FONT);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(100, 35));
        return button;
    }
    
    private void addValidationListeners() {
        // Validate giá vé
        txtGiaVe.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { validateGiaVe(); }
            @Override
            public void removeUpdate(DocumentEvent e) { validateGiaVe(); }
            @Override
            public void changedUpdate(DocumentEvent e) { validateGiaVe(); }
        });
        
        // Validate ngày áp dụng
        txtNgayApDung.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { validateNgayApDung(); }
            @Override
            public void removeUpdate(DocumentEvent e) { validateNgayApDung(); }
            @Override
            public void changedUpdate(DocumentEvent e) { validateNgayApDung(); }
        });
        
        // Validate ngày kết thúc
        txtNgayKetThuc.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { validateNgayKetThuc(); }
            @Override
            public void removeUpdate(DocumentEvent e) { validateNgayKetThuc(); }
            @Override
            public void changedUpdate(DocumentEvent e) { validateNgayKetThuc(); }
        });
        
        // Validate loại ghế
        cbLoaiGhe.addActionListener(e -> validateLoaiGhe());
    }
    
    private void validateGiaVe() {
        String giaVeStr = txtGiaVe.getText().trim();
        if (giaVeStr.isEmpty()) {
            ValidationUtils.showError(giaVeErrorLabel, "Giá vé không được để trống");
            ValidationUtils.setErrorBorder(txtGiaVe);
            return;
        }
        
        try {
            BigDecimal giaVe = new BigDecimal(giaVeStr);
            if (giaVe.compareTo(BigDecimal.ZERO) <= 0) {
                ValidationUtils.showError(giaVeErrorLabel, "Giá vé phải lớn hơn 0");
                ValidationUtils.setErrorBorder(txtGiaVe);
            } else if (giaVe.compareTo(new BigDecimal(500000)) > 0) {
                ValidationUtils.showError(giaVeErrorLabel, "Giá vé không được vượt quá 500,000 VND");
                ValidationUtils.setErrorBorder(txtGiaVe);
            } else {
                ValidationUtils.hideError(giaVeErrorLabel);
                ValidationUtils.setNormalBorder(txtGiaVe);
            }
        } catch (NumberFormatException e) {
            ValidationUtils.showError(giaVeErrorLabel, "Giá vé phải là số");
            ValidationUtils.setErrorBorder(txtGiaVe);
        }
    }
    
    private void validateNgayApDung() {
        String ngayApDungStr = txtNgayApDung.getText().trim();
        if (ngayApDungStr.isEmpty()) {
            ValidationUtils.showError(ngayApDungErrorLabel, "Ngày áp dụng không được để trống");
            ValidationUtils.setErrorBorder(txtNgayApDung);
            return;
        }
        
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            Date date = sdf.parse(ngayApDungStr);
            
            // Kiểm tra ngày áp dụng không được trước ngày hiện tại
            Date today = new Date();
            if (date.before(today)) {
                ValidationUtils.showError(ngayApDungErrorLabel, "Ngày áp dụng không được trước ngày hiện tại");
                ValidationUtils.setErrorBorder(txtNgayApDung);
            } else {
                ValidationUtils.hideError(ngayApDungErrorLabel);
                ValidationUtils.setNormalBorder(txtNgayApDung);
            }
        } catch (ParseException e) {
            ValidationUtils.showError(ngayApDungErrorLabel, "Ngày áp dụng không hợp lệ (yyyy-MM-dd)");
            ValidationUtils.setErrorBorder(txtNgayApDung);
        }
    }
    
    private void validateLoaiGhe() {
        String loaiGhe = (String) cbLoaiGhe.getSelectedItem();
        if (loaiGhe == null || loaiGhe.trim().isEmpty()) {
            ValidationUtils.showError(loaiGheErrorLabel, "Vui lòng chọn loại ghế");
            ValidationUtils.setErrorBorder(cbLoaiGhe);
        } else {
            ValidationUtils.hideError(loaiGheErrorLabel);
            ValidationUtils.setNormalBorder(cbLoaiGhe);
        }
    }
    
    private void validateNgayKetThuc() {
        if (chkKhongGioiHan.isSelected()) {
            ValidationUtils.hideError(ngayKetThucErrorLabel);
            ValidationUtils.setNormalBorder(txtNgayKetThuc);
            return;
        }
        
        String ngayKetThucStr = txtNgayKetThuc.getText().trim();
        if (ngayKetThucStr.isEmpty()) {
            ValidationUtils.showError(ngayKetThucErrorLabel, "Ngày kết thúc không được để trống");
            ValidationUtils.setErrorBorder(txtNgayKetThuc);
            return;
        }
        
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            Date ngayKetThuc = sdf.parse(ngayKetThucStr);
            
            // Kiểm tra ngày kết thúc phải sau ngày áp dụng
            String ngayApDungStr = txtNgayApDung.getText().trim();
            if (!ngayApDungStr.isEmpty()) {
                try {
                    Date ngayApDung = sdf.parse(ngayApDungStr);
                    if (ngayKetThuc.before(ngayApDung)) {
                        ValidationUtils.showError(ngayKetThucErrorLabel, "Ngày kết thúc phải sau ngày áp dụng");
                        ValidationUtils.setErrorBorder(txtNgayKetThuc);
                        return;
                    }
                } catch (ParseException e) {
                    // Ngày áp dụng không hợp lệ, sẽ được xử lý bởi validateNgayApDung()
                }
            }
            
            ValidationUtils.hideError(ngayKetThucErrorLabel);
            ValidationUtils.setNormalBorder(txtNgayKetThuc);
        } catch (ParseException e) {
            ValidationUtils.showError(ngayKetThucErrorLabel, "Ngày kết thúc không hợp lệ (yyyy-MM-dd)");
            ValidationUtils.setErrorBorder(txtNgayKetThuc);
        }
    }
    
    private boolean validateForm() {
        validateGiaVe();
        validateNgayApDung();
        validateLoaiGhe();
        
        if (!chkKhongGioiHan.isSelected()) {
            validateNgayKetThuc();
        }
        
        return giaVeErrorLabel.getText().isEmpty() && 
               ngayApDungErrorLabel.getText().isEmpty() &&
               loaiGheErrorLabel.getText().isEmpty() &&
               (chkKhongGioiHan.isSelected() || ngayKetThucErrorLabel.getText().isEmpty());
    }
    
    private GiaVe getGiaVeFromForm() throws ParseException {
        GiaVe giaVe = new GiaVe();
        
        if (!txtMaGiaVe.getText().isEmpty()) {
            giaVe.setMaGiaVe(Integer.parseInt(txtMaGiaVe.getText()));
        }
        
        giaVe.setLoaiGhe(cbLoaiGhe.getSelectedItem().toString());
        giaVe.setGiaVe(new BigDecimal(txtGiaVe.getText().trim()));
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date ngayApDung = sdf.parse(txtNgayApDung.getText().trim());
        giaVe.setNgayApDung(LocalDate.ofInstant(ngayApDung.toInstant(), ZoneId.systemDefault()));
        
        // Xử lý ngày kết thúc
        if (!chkKhongGioiHan.isSelected() && !txtNgayKetThuc.getText().trim().isEmpty()) {
            Date ngayKetThuc = sdf.parse(txtNgayKetThuc.getText().trim());
            giaVe.setNgayKetThuc(LocalDate.ofInstant(ngayKetThuc.toInstant(), ZoneId.systemDefault()));
        } else {
            giaVe.setNgayKetThuc(null); // Không giới hạn
        }
        
        giaVe.setGhiChu(txtGhiChu.getText().trim());
        
        return giaVe;
    }
    
    public void clearForm() {
        txtMaGiaVe.setText("");
        cbLoaiGhe.setSelectedIndex(0);
        txtGiaVe.setText("");
        txtNgayApDung.setText("");
        txtNgayKetThuc.setText("");
        txtGhiChu.setText("");
        chkKhongGioiHan.setSelected(false);
        txtNgayKetThuc.setEnabled(true);
        ValidationUtils.hideError(giaVeErrorLabel);
        ValidationUtils.hideError(ngayApDungErrorLabel);
        ValidationUtils.hideError(ngayKetThucErrorLabel);
        ValidationUtils.hideError(loaiGheErrorLabel);
        ValidationUtils.setNormalBorder(txtGiaVe);
        ValidationUtils.setNormalBorder(txtNgayApDung);
        ValidationUtils.setNormalBorder(txtNgayKetThuc);
        ValidationUtils.setNormalBorder(cbLoaiGhe);
        tableGiaVe.clearSelection();
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
    
    // Getters
    public JTextField getTxtGiaVe() {
        return txtGiaVe;
    }
    
    public JTextField getTxtNgayApDung() {
        return txtNgayApDung;
    }
    
    public JComboBox<String> getCbLoaiGhe() {
        return cbLoaiGhe;
    }
    
    public JLabel getGiaVeErrorLabel() {
        return giaVeErrorLabel;
    }
    
    public JLabel getNgayApDungErrorLabel() {
        return ngayApDungErrorLabel;
    }
    
    public JLabel getLoaiGheErrorLabel() {
        return loaiGheErrorLabel;
    }
    
    public JTextField getTxtNgayKetThuc() {
        return txtNgayKetThuc;
    }
    
    public JLabel getNgayKetThucErrorLabel() {
        return ngayKetThucErrorLabel;
    }
    
    public JCheckBox getChkKhongGioiHan() {
        return chkKhongGioiHan;
    }
    
    // Getters
    public JTable getTableGiaVe() {
        return tableGiaVe;
    }
    
    public DefaultTableModel getTableModel() {
        return tableModel;
    }
}