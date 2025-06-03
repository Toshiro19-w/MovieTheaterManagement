package com.cinema.views.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.cinema.components.ModernUIApplier;
import com.cinema.components.UIConstants;
import com.cinema.components.UnderlineTextField;
import com.cinema.models.TheLoaiPhim;
import com.cinema.models.repositories.TheLoaiRepository;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.SimpleDocumentListener;

public class TheLoaiSidebar extends JPanel {
    private UnderlineTextField txtMaTheLoai, txtTenTheLoai;
    private JLabel lblTenTheLoaiError;
    private JButton btnThem, btnSua, btnXoa, btnClear, btnClose;
    private DefaultTableModel tableModel;
    private JTable table;
    private TheLoaiRepository theLoaiRepository;
    private DatabaseConnection dbConnection;
    private ActionListener closeListener;

    public TheLoaiSidebar(DatabaseConnection dbConnection, ActionListener closeListener) {
        this.dbConnection = dbConnection;
        this.closeListener = closeListener;
        this.theLoaiRepository = new TheLoaiRepository(dbConnection);
        initUI();
        loadTheLoai();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(350, 600));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(200, 200, 200)));

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIConstants.PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel titleLabel = new JLabel("Quản lý thể loại", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        btnClose = ModernUIApplier.createUnicodeButton("\u00D7", UIConstants.ERROR_COLOR, Color.WHITE);
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnClose.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btnClose.addActionListener(closeListener);
        headerPanel.add(btnClose, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Content panel
        JPanel contentPanel = ModernUIApplier.createModernPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Form panel
        JPanel formPanel = new JPanel();
        formPanel.setOpaque(false);
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)), 
                "Thông tin thể loại"));

        // Mã thể loại
        JPanel maTheLoaiPanel = new JPanel(new BorderLayout(10, 0));
        maTheLoaiPanel.setOpaque(false);
        JLabel lblMaTheLoai = new JLabel("Mã thể loại:");
        lblMaTheLoai.setPreferredSize(new Dimension(80, 30));
        maTheLoaiPanel.add(lblMaTheLoai, BorderLayout.WEST);

        txtMaTheLoai = createStyledUnderlineTextField();
        txtMaTheLoai.setEditable(false);
        txtMaTheLoai.setBackground(new Color(245, 245, 245));
        maTheLoaiPanel.add(txtMaTheLoai, BorderLayout.CENTER);
        formPanel.add(maTheLoaiPanel);
        formPanel.add(Box.createVerticalStrut(10));

        // Tên thể loại
        JPanel tenTheLoaiPanel = new JPanel(new BorderLayout(10, 0));
        tenTheLoaiPanel.setOpaque(false);
        JLabel lblTenTheLoai = new JLabel("Tên thể loại:");
        lblTenTheLoai.setPreferredSize(new Dimension(80, 30));
        tenTheLoaiPanel.add(lblTenTheLoai, BorderLayout.WEST);

        txtTenTheLoai = createStyledUnderlineTextField();
        tenTheLoaiPanel.add(txtTenTheLoai, BorderLayout.CENTER);
        formPanel.add(tenTheLoaiPanel);
        formPanel.add(Box.createVerticalStrut(5));

        // Error label
        lblTenTheLoaiError = new JLabel("");
        lblTenTheLoaiError.setForeground(UIConstants.ERROR_COLOR);
        lblTenTheLoaiError.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblTenTheLoaiError.setVisible(false);
        JPanel errorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        errorPanel.setOpaque(false);
        errorPanel.add(Box.createHorizontalStrut(85));
        errorPanel.add(lblTenTheLoaiError);
        formPanel.add(errorPanel);
        formPanel.add(Box.createVerticalStrut(10));

        contentPanel.add(formPanel);
        contentPanel.add(Box.createVerticalStrut(10));

        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 5, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        btnThem = ModernUIApplier.createModernButton("Thêm", UIConstants.PRIMARY_COLOR, Color.WHITE);
        btnSua = ModernUIApplier.createModernButton("Sửa", UIConstants.PRIMARY_COLOR, Color.WHITE);
        btnXoa = ModernUIApplier.createModernButton("Xóa", UIConstants.ERROR_COLOR, Color.WHITE);
        btnClear = ModernUIApplier.createModernButton("Clear", UIConstants.SECONDARY_COLOR, Color.WHITE);

        buttonPanel.add(btnThem);
        buttonPanel.add(btnSua);
        buttonPanel.add(btnXoa);
        buttonPanel.add(btnClear);

        contentPanel.add(buttonPanel);
        contentPanel.add(Box.createVerticalStrut(15));

        // Table panel
        JPanel tablePanel = ModernUIApplier.createModernPanel();
        tablePanel.setLayout(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)), 
                "Danh sách thể loại"));

        tableModel = new DefaultTableModel(
                new Object[]{"Mã thể loại", "Tên thể loại"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ModernUIApplier.applyModernTableStyle(table);
        table.setRowHeight(30);

        // Center align text in cells
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        contentPanel.add(tablePanel);

        add(contentPanel, BorderLayout.CENTER);

        // Add listeners
        setupListeners();
    }

    private UnderlineTextField createStyledUnderlineTextField() {
        UnderlineTextField field = new UnderlineTextField(15);
        field.setFont(UIConstants.BODY_FONT);
        field.setUnderlineColor(new Color(200, 200, 200));
        field.setFocusColor(UIConstants.PRIMARY_COLOR);
        field.setErrorColor(UIConstants.ERROR_COLOR);
        return field;
    }

    private void setupListeners() {
        // Validation listener
        txtTenTheLoai.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            validateTenTheLoai();
            updateButtonState();
        }));

        // Table selection listener
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                txtMaTheLoai.setText(table.getValueAt(row, 0).toString());
                txtTenTheLoai.setText(table.getValueAt(row, 1).toString());
                validateTenTheLoai();
            }
        });

        // Button listeners
        btnThem.addActionListener(e -> themTheLoai());
        btnSua.addActionListener(e -> suaTheLoai());
        btnXoa.addActionListener(e -> xoaTheLoai());
        btnClear.addActionListener(e -> clearForm());

        // Initial button state
        updateButtonState();
    }

    private void validateTenTheLoai() {
        String tenTheLoai = txtTenTheLoai.getText().trim();
        if (tenTheLoai.isEmpty()) {
            lblTenTheLoaiError.setText("Tên thể loại không được để trống");
            lblTenTheLoaiError.setVisible(true);
            txtTenTheLoai.setError(true);
            return;
        }

        try {
            int maTheLoai = txtMaTheLoai.getText().isEmpty() ? 0 : Integer.parseInt(txtMaTheLoai.getText());
            if (theLoaiRepository.isTheLoaiExists(tenTheLoai, maTheLoai)) {
                lblTenTheLoaiError.setText("Tên thể loại đã tồn tại");
                lblTenTheLoaiError.setVisible(true);
                txtTenTheLoai.setError(true);
                return;
            }
        } catch (SQLException ex) {
            lblTenTheLoaiError.setText("Lỗi kiểm tra dữ liệu");
            lblTenTheLoaiError.setVisible(true);
            txtTenTheLoai.setError(true);
            return;
        }

        lblTenTheLoaiError.setVisible(false);
        txtTenTheLoai.setError(false);
    }

    private void updateButtonState() {
        boolean isValid = !lblTenTheLoaiError.isVisible() && !txtTenTheLoai.getText().trim().isEmpty();
        boolean hasSelection = !txtMaTheLoai.getText().trim().isEmpty();

        btnThem.setEnabled(isValid && !hasSelection);
        btnSua.setEnabled(isValid && hasSelection);
        btnXoa.setEnabled(hasSelection);
    }

    private void loadTheLoai() {
        try {
            tableModel.setRowCount(0);
            List<TheLoaiPhim> theLoaiList = theLoaiRepository.findAll();
            for (TheLoaiPhim theLoai : theLoaiList) {
                tableModel.addRow(new Object[]{
                    theLoai.getMaTheLoai(),
                    theLoai.getTenTheLoai()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu thể loại: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void themTheLoai() {
        try {
            String tenTheLoai = txtTenTheLoai.getText().trim();
            TheLoaiPhim theLoai = new TheLoaiPhim();
            theLoai.setTenTheLoai(tenTheLoai);
            theLoaiRepository.save(theLoai);
            JOptionPane.showMessageDialog(this, "Thêm thể loại thành công", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadTheLoai();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi thêm thể loại: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void suaTheLoai() {
        try {
            int maTheLoai = Integer.parseInt(txtMaTheLoai.getText());
            String tenTheLoai = txtTenTheLoai.getText().trim();
            TheLoaiPhim theLoai = new TheLoaiPhim(maTheLoai, tenTheLoai);
            theLoaiRepository.update(theLoai);
            JOptionPane.showMessageDialog(this, "Cập nhật thể loại thành công", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadTheLoai();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật thể loại: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xoaTheLoai() {
        if (JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa thể loại này?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                int maTheLoai = Integer.parseInt(txtMaTheLoai.getText());
                theLoaiRepository.delete(maTheLoai);
                JOptionPane.showMessageDialog(this, "Xóa thể loại thành công", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadTheLoai();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa thể loại: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        txtMaTheLoai.setText("");
        txtTenTheLoai.setText("");
        lblTenTheLoaiError.setVisible(false);
        txtTenTheLoai.setError(false);
        table.clearSelection();
        updateButtonState();
    }
    
    public void refreshData() {
        loadTheLoai();
    }
}