package com.cinema.views.admin;

import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import com.cinema.components.*;
import com.cinema.models.NhanVien;
import com.cinema.models.TheLoaiPhim;
import com.cinema.models.repositories.TheLoaiRepository;
import com.cinema.utils.*;

public class TheLoaiSidebar extends JPanel {
    private UnderlineTextField txtMaTheLoai, txtTenTheLoai;
    private JLabel lblTenTheLoaiError;
    private JButton btnThem, btnSua, btnXoa, btnClear, btnClose;
    private DefaultTableModel tableModel;
    private JTable table;
    private TheLoaiRepository theLoaiRepository;
    private DatabaseConnection dbConnection;
    private ActionListener closeListener;
    private NhanVien currentNhanVien;

    public TheLoaiSidebar(DatabaseConnection dbConnection, ActionListener closeListener, NhanVien currentNhanVien) {
        this.dbConnection = dbConnection;
        this.closeListener = closeListener;
        this.currentNhanVien = currentNhanVien;
        this.theLoaiRepository = new TheLoaiRepository(dbConnection);
        initUI();
        loadTheLoai();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Reduced padding

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIConstants.PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(8, 10, 8, 10)); // Reduced padding

        JLabel titleLabel = new JLabel("Quản lý thể loại", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16)); // Smaller font
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        btnClose = ModernUIApplier.createUnicodeButton("\u00D7", UIConstants.ERROR_COLOR, Color.WHITE);
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Smaller font
        btnClose.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        btnClose.addActionListener(closeListener);
        headerPanel.add(btnClose, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        JPanel contentPanel = ModernUIApplier.createModernPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); // Reduced padding

        JPanel formPanel = new JPanel();
        formPanel.setOpaque(false);
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "Thông tin thể loại"));

        JPanel maTheLoaiPanel = new JPanel(new BorderLayout(5, 0)); // Reduced gap
        maTheLoaiPanel.setOpaque(false);
        JLabel lblMaTheLoai = new JLabel("Mã thể loại:");
        lblMaTheLoai.setPreferredSize(new Dimension(80, 25)); // Smaller height
        maTheLoaiPanel.add(lblMaTheLoai, BorderLayout.WEST);

        txtMaTheLoai = createStyledUnderlineTextField();
        txtMaTheLoai.setEditable(false);
        txtMaTheLoai.setBackground(new Color(245, 245, 245));
        maTheLoaiPanel.add(txtMaTheLoai, BorderLayout.CENTER);
        formPanel.add(maTheLoaiPanel);
        formPanel.add(Box.createVerticalStrut(5)); // Reduced strut

        JPanel tenTheLoaiPanel = new JPanel(new BorderLayout(5, 0)); // Reduced gap
        tenTheLoaiPanel.setOpaque(false);
        JLabel lblTenTheLoai = new JLabel("Tên thể loại:");
        lblTenTheLoai.setPreferredSize(new Dimension(80, 25)); // Smaller height
        tenTheLoaiPanel.add(lblTenTheLoai, BorderLayout.WEST);

        txtTenTheLoai = createStyledUnderlineTextField();
        tenTheLoaiPanel.add(txtTenTheLoai, BorderLayout.CENTER);
        formPanel.add(tenTheLoaiPanel);
        formPanel.add(Box.createVerticalStrut(3)); // Reduced strut

        lblTenTheLoaiError = new JLabel("");
        lblTenTheLoaiError.setForeground(UIConstants.ERROR_COLOR);
        lblTenTheLoaiError.setFont(new Font("Segoe UI", Font.ITALIC, 11)); // Smaller font
        lblTenTheLoaiError.setVisible(false);
        JPanel errorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        errorPanel.setOpaque(false);
        errorPanel.add(Box.createHorizontalStrut(85));
        errorPanel.add(lblTenTheLoaiError);
        formPanel.add(errorPanel);
        formPanel.add(Box.createVerticalStrut(5)); // Reduced strut

        contentPanel.add(formPanel);
        contentPanel.add(Box.createVerticalStrut(5)); // Reduced strut

        JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 5, 0)); // Reduced gap
        buttonPanel.setOpaque(false);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35)); // Smaller height

        btnThem = ModernUIApplier.createModernButton("Thêm", UIConstants.PRIMARY_COLOR, Color.WHITE);
        btnSua = ModernUIApplier.createModernButton("Sửa", UIConstants.PRIMARY_COLOR, Color.WHITE);
        btnXoa = ModernUIApplier.createModernButton("Xóa", UIConstants.ERROR_COLOR, Color.WHITE);
        btnClear = ModernUIApplier.createModernButton("Clear", UIConstants.SECONDARY_COLOR, Color.WHITE);

        buttonPanel.add(btnThem);
        buttonPanel.add(btnSua);
        buttonPanel.add(btnXoa);
        buttonPanel.add(btnClear);

        contentPanel.add(buttonPanel);
        contentPanel.add(Box.createVerticalStrut(10)); // Reduced strut

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
        table.setRowHeight(25); // Smaller row height
        table.setPreferredScrollableViewportSize(new Dimension(350, 200)); // Fixed table height

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);

        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(tableScrollPane, BorderLayout.CENTER);

        contentPanel.add(tablePanel);

        // Wrap contentPanel in a JScrollPane for overall scrolling
        JScrollPane contentScrollPane = new JScrollPane(contentPanel);
        contentScrollPane.setBorder(BorderFactory.createEmptyBorder());
        contentScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        contentScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(contentScrollPane, BorderLayout.CENTER);

        setupListeners();
    }

    private UnderlineTextField createStyledUnderlineTextField() {
        UnderlineTextField field = new UnderlineTextField(15);
        field.setFont(UIConstants.BODY_FONT.deriveFont(12f)); // Smaller font
        field.setUnderlineColor(new Color(200, 200, 200));
        field.setFocusColor(UIConstants.PRIMARY_COLOR);
        field.setErrorColor(UIConstants.ERROR_COLOR);
        return field;
    }

    private void setupListeners() {
        txtTenTheLoai.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            validateTenTheLoai();
            updateButtonState();
        }));

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                txtMaTheLoai.setText(table.getValueAt(row, 0).toString());
                txtTenTheLoai.setText(table.getValueAt(row, 1).toString());
                validateTenTheLoai();
            }
        });

        btnThem.addActionListener(e -> addTheLoai());
        btnSua.addActionListener(e -> updateTheLoai());
        btnXoa.addActionListener(e -> deleteTheLoai());
        btnClear.addActionListener(e -> clearForm());

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
            for (TheLoaiPhim phim : theLoaiList) {
                tableModel.addRow(new Object[]{
                    phim.getMaTheLoai(),
                    phim.getTenTheLoai()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu thể loại: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addTheLoai() {
        try {
            String tenTheLoai = txtTenTheLoai.getText().trim();
            TheLoaiPhim theLoai = new TheLoaiPhim();
            theLoai.setTenTheLoai(tenTheLoai);
            TheLoaiPhim savedTheLoai = theLoaiRepository.save(theLoai);
            int maTheLoai = savedTheLoai.getMaTheLoai();
            JOptionPane.showMessageDialog(this, "Thêm thể loại thành công", "Thông báo", JOptionPane.INFORMATION_MESSAGE);

            String moTa = String.format("Thêm thể loại: %s (ID: %d)", tenTheLoai, maTheLoai);
            LogUtils.logThemTheLoai(maTheLoai, moTa, getCurrentUserId());

            clearForm();
            loadTheLoai();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi thêm thể loại: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(TheLoaiSidebar.class.getName()).severe("Lỗi khi thêm thể loại: " + e.getMessage());
        }
    }

    private void updateTheLoai() {
        try {
            int maTheLoai = Integer.parseInt(txtMaTheLoai.getText());
            String tenTheLoai = txtTenTheLoai.getText().trim();
            TheLoaiPhim theLoai = new TheLoaiPhim(maTheLoai, tenTheLoai);
            theLoaiRepository.update(theLoai);
            JOptionPane.showMessageDialog(this, "Cập nhật thể loại thành công", "Thông báo", JOptionPane.INFORMATION_MESSAGE);

            String moTa = String.format("Cập nhật thể loại: %s (ID: %d)", tenTheLoai, maTheLoai);
            LogUtils.logSuaTheLoai(maTheLoai, moTa, getCurrentUserId());

            clearForm();
            loadTheLoai();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật thể loại: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(TheLoaiSidebar.class.getName()).severe("Lỗi khi cập nhật thể loại: " + e.getMessage());
        }
    }

    private void deleteTheLoai() {
        if (JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa thể loại này?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                int maTheLoai = Integer.parseInt(txtMaTheLoai.getText());
                String tenTheLoai = txtTenTheLoai.getText().trim();
                theLoaiRepository.delete(maTheLoai);
                JOptionPane.showMessageDialog(this, "Xóa thể loại thành công", "Thông báo", JOptionPane.INFORMATION_MESSAGE);

                String moTa = String.format("Xóa thể loại: %s (ID: %d)", tenTheLoai, maTheLoai);
                LogUtils.logXoaTheLoai(maTheLoai, moTa, getCurrentUserId());

                clearForm();
                loadTheLoai();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa thể loại: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                Logger.getLogger(TheLoaiSidebar.class.getName()).severe("Lỗi khi xóa thể loại: " + e.getMessage());
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

    private int getCurrentUserId() {
        if (currentNhanVien == null) {
            Logger.getLogger(TheLoaiSidebar.class.getName()).warning("currentNhanVien is null, using default user ID 0");
            return 0;
        }
        return currentNhanVien.getMaNguoiDung();
    }

    public void refreshData() {
        loadTheLoai();
    }
}