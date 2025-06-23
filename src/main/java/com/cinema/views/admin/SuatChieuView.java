package com.cinema.views.admin;

import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import com.cinema.models.dto.CustomPaginationPanel;
import com.cinema.components.DateTimePicker;
import com.cinema.components.UIConstants;
import com.cinema.components.UnderlineTextField;
import com.cinema.controllers.SuatChieuController;
import com.cinema.models.Phim;
import com.cinema.models.PhongChieu;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.SimpleDocumentListener;
import com.cinema.utils.SuatChieuValidation;

public class SuatChieuView extends JPanel {
    private final ResourceBundle messages;
    private final DatabaseConnection databaseConnection;
    private final SuatChieuController controller;
    private Integer selectedMaSuatChieu = null; // Mã suất chiếu được chọn
    private Integer selectedMaPhong = null; // Mã phòng chiếu được chọn

    // UI Components
    private UnderlineTextField txtNgayGioChieu, suatChieuSearchField;
    private JLabel txtMaSuatChieu;
    private JComboBox<Phim> cbMaPhim;
    private JComboBox<PhongChieu> cbMaPhong;
    private JTable suatChieuTable;
    private DefaultTableModel suatChieuTableModel;
    private JButton btnThemSuat, btnSuaSuat, btnXoaSuat, btnClearSuat;
    private TableRowSorter<DefaultTableModel> suatChieuSorter;
    private JTable phongChieuTable;
    private DefaultTableModel phongChieuTableModel;

    // Error labels
    private JLabel lblNgayGioChieuError, lblPhimError, lblPhongError;

    // UI Constants
    private static final Color PRIMARY_COLOR = new Color(59, 130, 246);
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Font LABEL_FONT = new Font("Inter", Font.PLAIN, 14);
    private static final Font TITLE_FONT = new Font("Inter", Font.BOLD, 24);
    private static final Font BUTTON_FONT = new Font("Inter", Font.BOLD, 14);    
    public SuatChieuView() throws SQLException, IOException {
        this.messages = ResourceBundle.getBundle("Messages");
        this.databaseConnection = new DatabaseConnection();
        initializeUI();
        this.controller = new SuatChieuController(this);
        addValidationListeners();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Title Panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("QUẢN LÝ SUẤT CHIẾU", SwingConstants.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);

        // Create split pane for side-by-side layout
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(0.5);
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(null);

        // PhongChieu panel phải
        JPanel phongChieuPanel = createPhongChieuPanel();
        // SuatChieu panel trái
        JPanel suatChieuPanel = createSuatChieuPanel();

        splitPane.setRightComponent(phongChieuPanel);
        splitPane.setLeftComponent(suatChieuPanel);

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createSuatChieuPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // Tạo panel trung gian với BoxLayout
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        // Info panel (form nhập liệu, bo góc, đổ bóng nhẹ)
        JPanel infoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.fillRoundRect(5, 5, getWidth() - 6, getHeight() - 6, 20, 20);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth() - 5, getHeight() - 5, 20, 20);
            }
        };
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        infoPanel.setLayout(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel("THÔNG TIN SUẤT CHIẾU", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Inter", Font.BOLD, 16));
        infoPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        initializeSuatChieuFields(fieldsPanel, gbc);
        infoPanel.add(fieldsPanel, BorderLayout.CENTER);

        // Button panel (đẩy lên trên bảng)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);
        btnThemSuat = createStyledButton("THÊM", PRIMARY_COLOR);
        btnSuaSuat = createStyledButton("SỬA", PRIMARY_COLOR);
        btnXoaSuat = createStyledButton("XÓA", PRIMARY_COLOR);
        btnClearSuat = createStyledButton("LÀM MỚI", PRIMARY_COLOR);
        buttonPanel.add(btnThemSuat);
        buttonPanel.add(btnSuaSuat);
        buttonPanel.add(btnXoaSuat);
        buttonPanel.add(btnClearSuat);

        // Table panel (bảng suất chiếu + CustomPaginationPanel)
        JPanel tablePanel = createSuatChieuTablePanel();

        contentPanel.add(infoPanel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(buttonPanel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(tablePanel);

        panel.add(contentPanel, BorderLayout.CENTER);
        return panel;
    }

    private void initializeSuatChieuFields(JPanel fieldsPanel, GridBagConstraints gbc) {
        txtMaSuatChieu = new JLabel();
        txtMaSuatChieu.setFont(LABEL_FONT);
        cbMaPhim = new JComboBox<>();
        cbMaPhong = new JComboBox<>();
        txtNgayGioChieu = new UnderlineTextField(20);
        suatChieuSearchField = new UnderlineTextField(20);

        styleComboBox(cbMaPhim);
        styleComboBox(cbMaPhong);
        styleTextField(txtNgayGioChieu);
        styleTextField(suatChieuSearchField);

        txtNgayGioChieu.setPlaceholder("dd/MM/yyyy HH:mm:ss");
        suatChieuSearchField.setPlaceholder("Tìm kiếm suất chiếu...");

        // Thêm error labels
        lblNgayGioChieuError = createErrorLabel();
        lblPhimError = createErrorLabel();
        lblPhongError = createErrorLabel();

        // Thêm các trường và error labels vào panel
        gbc.gridy = 0;
        addFormField(fieldsPanel, "Mã Suất Chiếu:", txtMaSuatChieu, null, gbc);
        gbc.gridy++;
        addFormField(fieldsPanel, "Phim:", cbMaPhim, lblPhimError, gbc);
        gbc.gridy++;
        addFormField(fieldsPanel, "Phòng chiếu:", cbMaPhong, lblPhongError, gbc);
        gbc.gridy++;
        
        // Tạo panel cho ngày giờ chiếu với DateTimePicker
        JPanel ngayGioChieuPanel = new JPanel(new BorderLayout(5, 0));
        ngayGioChieuPanel.setOpaque(false);
        ngayGioChieuPanel.add(txtNgayGioChieu, BorderLayout.CENTER);
        
        JButton btnDateTimePicker = new JButton("...");
        btnDateTimePicker.setPreferredSize(new Dimension(30, 30));
        btnDateTimePicker.addActionListener(e -> {
            DateTimePicker picker = new DateTimePicker(txtNgayGioChieu);
            picker.setLocationRelativeTo(SuatChieuView.this);
            picker.setVisible(true);
        });
        ngayGioChieuPanel.add(btnDateTimePicker, BorderLayout.EAST);
        
        addFormField(fieldsPanel, "Ngày giờ chiếu:", ngayGioChieuPanel, lblNgayGioChieuError, gbc);
        gbc.gridy++;
        addFormField(fieldsPanel, "Tìm kiếm:", suatChieuSearchField, null, gbc);

        // Add search functionality
        suatChieuSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateSearch(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateSearch(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateSearch(); }
        });

        // Add validation listeners
        addValidationListeners();
    }

    private void updateSearch() {
        String searchText = suatChieuSearchField.getText();
        if (searchText.trim().isEmpty()) {
            suatChieuSorter.setRowFilter(null);
        } else {
            suatChieuSorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
        }
    }

    private JPanel createSuatChieuTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Tìm kiếm:"));
        suatChieuSearchField = new UnderlineTextField(20);
        styleTextField(suatChieuSearchField);
        searchPanel.add(suatChieuSearchField);

        // Table
        suatChieuTableModel = new DefaultTableModel(
                new Object[]{"Mã Suất Chiếu", "Tên Phim", "Phòng Chiếu", "Ngày Giờ Chiếu"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        suatChieuTable = new JTable(suatChieuTableModel);
        suatChieuTable.setRowHeight(35);
        suatChieuTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suatChieuTable.getTableHeader().setReorderingAllowed(false);

        // Custom header renderer
        suatChieuTable.getTableHeader().setDefaultRenderer((table, value, isSelected, hasFocus, row, col) -> {
            JLabel label = new JLabel(value.toString());
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            label.setBackground(new Color(240, 240, 240));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setOpaque(true);
            return label;
        });

        // Custom cell renderer
        suatChieuTable.setDefaultRenderer(Object.class, (table, value, isSelected, hasFocus, row, col) -> {
            JLabel label = new JLabel(value != null ? value.toString() : "");
            label.setHorizontalAlignment(SwingConstants.CENTER);
            if (isSelected) {
                label.setBackground(UIManager.getColor("Table.selectionBackground"));
                label.setForeground(UIManager.getColor("Table.selectionForeground"));
            } else {
                label.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                label.setForeground(UIManager.getColor("Table.foreground"));
            }
            label.setOpaque(true);
            return label;
        });

        JScrollPane scrollPane = new JScrollPane(suatChieuTable);
        scrollPane.setPreferredSize(new Dimension(800, 300));        

        // Thêm phân trang với thiết kế hiện đại
        CustomPaginationPanel paginationPanel = new CustomPaginationPanel();
        paginationPanel.setName("paginationPanel"); // Đặt tên để dễ tìm kiếm
        paginationPanel.setBackground(UIConstants.CARD_BACKGROUND);
        paginationPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        paginationPanel.setPageChangeListener(page -> {
            try {
                controller.loadSuatChieuPaginated(page, 10);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Lỗi khi tải dữ liệu suất chiếu: " + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Layout
        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(paginationPanel, BorderLayout.SOUTH);

        // Search functionality
        suatChieuSearchField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            String searchText = suatChieuSearchField.getText().toLowerCase();
            suatChieuSorter.setRowFilter(searchText.isEmpty() ? null : new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(RowFilter.Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    for (int i = 0; i < entry.getValueCount(); i++) {
                        if (entry.getStringValue(i).toLowerCase().contains(searchText)) {
                            return true;
                        }
                    }
                    return false;
                }
            });
        }));
        
        // Setup sorter
        suatChieuSorter = new TableRowSorter<>(suatChieuTableModel);
        suatChieuTable.setRowSorter(suatChieuSorter);

        return panel;
    }

    private JPanel createPhongChieuPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        JPanel tablePanel = createPhongChieuTablePanel();
        panel.add(tablePanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPhongChieuTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        String[] columns = {"Mã Phòng", "Tên Phòng", "Số Lượng Ghế", "Loại Phòng"};
        phongChieuTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        phongChieuTable = new JTable(phongChieuTableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (isRowSelected(row)) {
                    c.setBackground(new Color(255, 204, 0, 100));
                } else if (row % 2 == 0) {
                    c.setBackground(Color.WHITE);
                } else {
                    c.setBackground(new Color(245, 245, 245));
                }
                return c;
            }
        };

        phongChieuTable.setFont(LABEL_FONT);
        phongChieuTable.getTableHeader().setBackground(PRIMARY_COLOR);
        phongChieuTable.getTableHeader().setForeground(Color.WHITE);
        phongChieuTable.getTableHeader().setFont(BUTTON_FONT);
        phongChieuTable.setRowHeight(30);
        phongChieuTable.setGridColor(Color.LIGHT_GRAY);
        phongChieuTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        TableRowSorter<DefaultTableModel> phongChieuSorter = new TableRowSorter<>(phongChieuTableModel);
        phongChieuTable.setRowSorter(phongChieuSorter);

        phongChieuTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = phongChieuTable.getSelectedRow();
                if (selectedRow >= 0) {
                    selectedMaPhong = (Integer) phongChieuTableModel.getValueAt(selectedRow, 0);
                } else {
                    selectedMaPhong = null;
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(phongChieuTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("DANH SÁCH PHÒNG CHIẾU"));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void addFormField(JPanel panel, String labelText, JComponent field, JLabel errorLabel, GridBagConstraints gbc) {
        JLabel label = new JLabel(labelText);
        label.setFont(LABEL_FONT);
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        panel.add(label, gbc);

        JPanel fieldPanel = new JPanel(new BorderLayout(0, 2));
        fieldPanel.setOpaque(false);
        field.setPreferredSize(new Dimension(250, 30));
        fieldPanel.add(field, BorderLayout.CENTER);

        if (errorLabel != null) {
            errorLabel.setPreferredSize(new Dimension(250, 20));
            errorLabel.setVisible(false);
            fieldPanel.add(errorLabel, BorderLayout.SOUTH);
        }

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        panel.add(fieldPanel, gbc);
        gbc.weightx = 0.0;
    }

    private JLabel createErrorLabel() {
        JLabel label = new JLabel();
        label.setFont(new Font("Inter", Font.ITALIC, 12));
        label.setForeground(new Color(220, 53, 69));
        return label;
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return button;
    }

    private void styleComboBox(JComboBox comboBox) {
        comboBox.setFont(LABEL_FONT);
        comboBox.setPreferredSize(new Dimension(250, 30));
        comboBox.setBackground(Color.WHITE);
    }

    private void styleTextField(UnderlineTextField field) {
        field.setFont(LABEL_FONT);
        field.setPreferredSize(new Dimension(250, 30));
        field.setUnderlineColor(new Color(200, 200, 200));
        field.setFocusColor(PRIMARY_COLOR);
        field.setErrorColor(new Color(220, 53, 69));
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private void addValidationListeners() {
        // Validate thời gian chiếu
        txtNgayGioChieu.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            LocalDateTime releaseDate = getMovieReleaseDate(); // Lấy ngày khởi chiếu từ phim được chọn
            if (SuatChieuValidation.validateShowtime(
                    txtNgayGioChieu.getText(),
                    releaseDate,
                    lblNgayGioChieuError,
                    messages)) {
                txtNgayGioChieu.setError(false);
            } else {
                txtNgayGioChieu.setError(true);
            }
            updateButtonStates();
        }));
    }

    private boolean isFormValid() {
        LocalDateTime releaseDate = getMovieReleaseDate();
        return SuatChieuValidation.validateShowtime(txtNgayGioChieu.getText(), releaseDate, lblNgayGioChieuError, messages);
    }

    private void updateButtonStates() {
        boolean isValid = isFormValid();
        boolean hasSelection = suatChieuTable.getSelectedRow() != -1;
        
        btnThemSuat.setEnabled(isValid && !hasSelection);
        btnSuaSuat.setEnabled(isValid && hasSelection);
        btnXoaSuat.setEnabled(hasSelection);
        btnClearSuat.setEnabled(true);
        
        // Log trạng thái
        System.out.println("Form valid: " + isValid + ", Has selection: " + hasSelection);
    }

    // Thêm phương thức này vào SuatChieuView
    private LocalDateTime getMovieReleaseDate() {
        Object selectedItem = cbMaPhim.getSelectedItem();
        if (selectedItem == null) return LocalDateTime.now();
        
        try {
            // Kiểm tra xem item được chọn có phải là Phim không
            if (selectedItem instanceof Phim phim) {
                LocalDate releaseDate = phim.getNgayKhoiChieu();
                return releaseDate.atStartOfDay();
            } else {
                System.out.println("Lỗi: Item được chọn không phải là Phim - " + selectedItem.getClass());
                return LocalDateTime.now();
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi lấy ngày khởi chiếu: " + e.getMessage());
            e.printStackTrace();
            return LocalDateTime.now();
        }
    }

    // Getters
    public DatabaseConnection getDatabaseConnection() { return databaseConnection; }
    public UnderlineTextField getSuatChieuSearchField() { return suatChieuSearchField; }
    public JLabel getTxtMaSuatChieu() { return txtMaSuatChieu; }
    public UnderlineTextField getTxtNgayGioChieu() { return txtNgayGioChieu; }
    public JComboBox getCbMaPhim() { return cbMaPhim; }
    public JComboBox getCbMaPhong() { return cbMaPhong; }
    public JTable getSuatChieuTable() { return suatChieuTable; }
    public DefaultTableModel getSuatChieuTableModel() { return suatChieuTableModel; }
    public JButton getBtnThemSuat() { return btnThemSuat; }
    public JButton getBtnSuaSuat() { return btnSuaSuat; }
    public JButton getBtnXoaSuat() { return btnXoaSuat; }
    public JButton getBtnClearSuat() { return btnClearSuat; }
    public String getSuatChieuSearchText() { return suatChieuSearchField.getText(); }
    public Integer getSelectedMaSuatChieu() { return selectedMaSuatChieu; }
    public JTable getPhongChieuTable() { return phongChieuTable; }
    public DefaultTableModel getPhongChieuTableModel() { return phongChieuTableModel; }
    public Integer getSelectedMaPhong() { return selectedMaPhong; }
}