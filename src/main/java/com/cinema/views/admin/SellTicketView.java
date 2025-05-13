package com.cinema.views.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.cinema.controllers.DatVeController;
import com.cinema.controllers.KhachHangController;
import com.cinema.controllers.PaymentController;
import com.cinema.controllers.SimplePhimController;
import com.cinema.models.KhachHang;
import com.cinema.models.Phim;
import com.cinema.services.GheService;
import com.cinema.services.KhachHangService;
import com.cinema.services.PhimService;
import com.cinema.services.SuatChieuService;
import com.cinema.services.VeService;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.ValidationUtils;
import com.cinema.views.BookingView;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.AutoCompleteSupport;

public class SellTicketView extends JPanel {
    // Constants
    private static final Color CINESTAR_BLUE = new Color(0, 51, 102);
    private static final Color CINESTAR_YELLOW = new Color(255, 204, 0);
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Color SHADOW_COLOR = new Color(0, 0, 0, 50);
    private static final Font LABEL_FONT = new Font("Roboto", Font.PLAIN, 14);
    private static final Font HEADER_FONT = new Font("Roboto", Font.BOLD, 16);
    private static final int SEARCH_DELAY = 300; // milliseconds

    // Controllers
    private final KhachHangController khachHangController;
    private final SimplePhimController phimController;
    private final DatVeController datVeController;
    private final PaymentController paymentController;
    private final DatabaseConnection databaseConnection;

    // UI Components
    private JComboBox<String> customerComboBox;
    private DefaultComboBoxModel<String> comboBoxModel;
    private JLabel customerIdLabel, customerNameLabel, customerPhoneLabel, customerEmailLabel;
    private JTable movieTable;
    private DefaultTableModel tableModel;
    private JLabel searchErrorLabel;
    private JLabel loadingLabel;
    private JPanel snackbarPanel;
    private JPanel searchPanel;

    // Data
    private List<KhachHang> customers;
    private List<Phim> movies;
    private ResourceBundle messages;
    private EventList<String> customerNameList;
    private final Timer searchTimer;

    public SellTicketView() throws IOException, SQLException {
        // Initialize controllers
        databaseConnection = new DatabaseConnection();
        KhachHangService khachHangService = new KhachHangService(databaseConnection);
        PhimService phimService = new PhimService(databaseConnection);
        SuatChieuService suatChieuService = new SuatChieuService(databaseConnection);
        GheService gheService = new GheService(databaseConnection);
        VeService veService = new VeService(databaseConnection);

        khachHangController = new KhachHangController(khachHangService);
        phimController = new SimplePhimController(phimService);
        datVeController = new DatVeController(suatChieuService, gheService, veService);
        paymentController = new PaymentController();

        // Initialize data
        customers = new ArrayList<>();
        movies = new ArrayList<>();
        messages = ResourceBundle.getBundle("Messages");
        customerNameList = new BasicEventList<>();
        searchTimer = new Timer(SEARCH_DELAY, _ -> performSearch());
        searchTimer.setRepeats(false);

        // Setup UI
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(BACKGROUND_COLOR);

        SwingUtilities.invokeLater(() -> {
            try {
                initUI();
                loadCustomers();
                loadMovies();
                JTextField editor = (JTextField) customerComboBox.getEditor().getEditorComponent();
                editor.requestFocusInWindow();
            } catch (Exception e) {
                showSnackbar(messages.getString("error") + ": " + e.getMessage(), false);
            }
        });
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(25, 25));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Add customer panel (left side)
        mainPanel.add(createCustomerPanel(), BorderLayout.WEST);

        // Add movie panel (right side)
        mainPanel.add(createMoviePanel(), BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createCustomerPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 20, 20);
                g2d.setColor(SHADOW_COLOR);
                g2d.drawRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 20, 20);
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setPreferredSize(new Dimension(350, 0));
        panel.setOpaque(false);

        // Title
        JLabel titleLabel = new JLabel("Thông tin khách hàng");
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(CINESTAR_BLUE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));

        // Search panel
        searchPanel = createSearchPanel();
        panel.add(searchPanel);
        panel.add(Box.createVerticalStrut(10));

        // Error label
        searchErrorLabel = ValidationUtils.createErrorLabel();
        searchErrorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(searchErrorLabel);
        panel.add(Box.createVerticalStrut(15));

        // Customer info panel
        JPanel infoPanel = createCustomerInfoPanel();
        panel.add(infoPanel);
        panel.add(Box.createVerticalGlue());

        setupAutoComplete();
        return panel;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setMaximumSize(new Dimension(1000, 40));

        // ComboBox
        comboBoxModel = new DefaultComboBoxModel<>();
        customerComboBox = new JComboBox<>(comboBoxModel);
        customerComboBox.setFont(LABEL_FONT);
        customerComboBox.setEditable(true);
        customerComboBox.setBackground(Color.WHITE);
        customerComboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 5, 5, 5),
            new CustomSearchBorder(getScaledIcon("/icons/search.png", 20, 20))
        ));

        // Sử dụng PlaceholderTextField làm editor
        PlaceholderTextField placeholderField = new PlaceholderTextField("Nhập tên, số điện thoại hoặc email...");
        placeholderField.setFont(LABEL_FONT);
        placeholderField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        customerComboBox.setEditor(new BasicComboBoxEditor() {
            @Override
            public Component getEditorComponent() {
                return placeholderField;
            }
            @Override
            public void setItem(Object anObject) {
                if (anObject != null) {
                    placeholderField.setText(anObject.toString());
                } else {
                    placeholderField.setText("");
                }
            }
            @Override
            public Object getItem() {
                return placeholderField.getText();
            }
        });

        // Loading indicator
        loadingLabel = new JLabel(getScaledIcon("/icons/loading.gif", 20, 20));
        loadingLabel.setVisible(false);
        panel.add(loadingLabel, BorderLayout.EAST);
        panel.add(customerComboBox, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCustomerInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(CINESTAR_BLUE), 
            "Chi tiết khách hàng", 0, 0, HEADER_FONT, CINESTAR_BLUE
        ));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Customer info labels
        customerIdLabel = new JLabel("🆔 " + messages.getString("usernameLabel"));
        customerNameLabel = new JLabel("👤 " + messages.getString("fullNameLabel"));
        customerPhoneLabel = new JLabel("📞 " + messages.getString("phoneLabel"));
        customerEmailLabel = new JLabel("✉️ " + messages.getString("emailLabel"));

        // Set fonts
        customerIdLabel.setFont(LABEL_FONT);
        customerNameLabel.setFont(LABEL_FONT);
        customerPhoneLabel.setFont(LABEL_FONT);
        customerEmailLabel.setFont(LABEL_FONT);

        // Add labels with spacing
        panel.add(customerIdLabel);
        panel.add(Box.createVerticalStrut(12));
        panel.add(customerNameLabel);
        panel.add(Box.createVerticalStrut(12));
        panel.add(customerPhoneLabel);
        panel.add(Box.createVerticalStrut(12));
        panel.add(customerEmailLabel);

        return panel;
    }

    private void setupAutoComplete() {
        JTextField editor = (JTextField) customerComboBox.getEditor().getEditorComponent();
        editor.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        AutoCompleteSupport.install(customerComboBox, customerNameList);
        customerComboBox.setEditable(true);

        editor.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (editor.getText().trim().isEmpty() && !customers.isEmpty()) {
                    updateComboBoxModel(customers);
                    customerComboBox.showPopup();
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                String currentText = editor.getText().trim();
                if (currentText.isEmpty()) {
                    ValidationUtils.hideError(searchErrorLabel);
                    editor.setForeground(Color.BLACK);
                    loadCustomers();
                    customerComboBox.setSelectedItem(null);
                    clearCustomerInfo();
                    customerComboBox.hidePopup();
                } else {
                    validateAndUpdateSelection();
                }
            }
        });

        editor.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                scheduleSearch();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                scheduleSearch();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                scheduleSearch();
            }

            private void scheduleSearch() {
                searchTimer.restart();
                loadingLabel.setVisible(true);
            }
        });

        customerComboBox.addActionListener(e -> {
            String selectedName = (String) customerComboBox.getSelectedItem();
            if (selectedName != null && !selectedName.trim().isEmpty()) {
                ValidationUtils.hideError(searchErrorLabel);
                updateCustomerInfo();
                customerComboBox.hidePopup();
            }
        });
    }

    private void performSearch() {
        JTextField editor = (JTextField) customerComboBox.getEditor().getEditorComponent();
        String searchText = editor.getText().trim();
        loadingLabel.setVisible(false);

        if (searchText.isEmpty()) {
            ValidationUtils.hideError(searchErrorLabel);
            editor.setForeground(Color.BLACK);
            loadCustomers();
            customerComboBox.setSelectedItem(null);
            clearCustomerInfo();
            if (editor.hasFocus()) {
                customerComboBox.showPopup();
            } else {
                customerComboBox.hidePopup();
            }
            return;
        }

        try {
            List<KhachHang> searchResults;
            if (searchText.length() < 2) {
                searchResults = khachHangController.findRecentKhachHang(10);
            } else {
                // Search by name, phone or email
                searchResults = khachHangController.searchKhachHang(searchText);
            }

            customers = searchResults;

            if (searchResults.isEmpty()) {
                comboBoxModel.removeAllElements();
                customerComboBox.hidePopup();
                clearCustomerInfo();
                ValidationUtils.showError(searchErrorLabel, "Không tìm thấy khách hàng");
                return;
            }

            updateComboBoxModel(searchResults);
            updateCustomerNameList();

            boolean exactMatch = searchResults.stream()
                .anyMatch(kh -> kh.getHoTen().equalsIgnoreCase(searchText) ||
                              kh.getSoDienThoai().equals(searchText) ||
                              (kh.getEmail() != null && kh.getEmail().equalsIgnoreCase(searchText)));

            if (exactMatch) {
                customerComboBox.setSelectedItem(searchText);
                customerComboBox.hidePopup();
                updateCustomerInfo();
            } else if (searchText.length() >= 2) {
                customerComboBox.setSelectedItem(null);
                customerComboBox.showPopup();
            } else {
                customerComboBox.hidePopup();
                clearCustomerInfo();
            }

        } catch (SQLException ex) {
            ValidationUtils.showError(searchErrorLabel, messages.getString("dbError") + ex.getMessage());
            comboBoxModel.removeAllElements();
            customerComboBox.hidePopup();
            clearCustomerInfo();
        }
    }

    private void updateComboBoxModel(List<KhachHang> customerList) {
        SwingUtilities.invokeLater(() -> {
            String currentText = ((JTextField) customerComboBox.getEditor().getEditorComponent()).getText().trim();
            comboBoxModel.removeAllElements();
            for (KhachHang kh : customerList) {
                comboBoxModel.addElement(kh.getHoTen());
            }
            // Keep selected item if it matches
            if (!currentText.isEmpty() && customerList.stream().anyMatch(kh -> kh.getHoTen().equalsIgnoreCase(currentText))) {
                customerComboBox.setSelectedItem(currentText);
            } else {
                customerComboBox.setSelectedItem(null);
            }
        });
    }

    private void updateCustomerNameList() {
        SwingUtilities.invokeLater(() -> {
            customerNameList.clear();
            for (KhachHang kh : customers) {
                customerNameList.add(kh.getHoTen());
            }
        });
    }

    private void validateAndUpdateSelection() {
        String currentText = ((JTextField) customerComboBox.getEditor().getEditorComponent()).getText().trim();
        if (currentText.isEmpty()) {
            clearCustomerInfo();
            customerComboBox.setSelectedItem(null);
            loadCustomers();
            customerComboBox.hidePopup();
            return;
        }

        boolean found = customers.stream()
            .anyMatch(kh -> kh.getHoTen().equalsIgnoreCase(currentText) ||
                          kh.getSoDienThoai().equals(currentText) ||
                          (kh.getEmail() != null && kh.getEmail().equalsIgnoreCase(currentText)));

        if (!found) {
            ValidationUtils.showError(searchErrorLabel, "Khách hàng không tồn tại");
            ((JTextField) customerComboBox.getEditor().getEditorComponent()).setText("");
            customerComboBox.setSelectedItem(null);
            clearCustomerInfo();
            customerComboBox.hidePopup();
        } else {
            customerComboBox.setSelectedItem(currentText);
            updateCustomerInfo();
            customerComboBox.hidePopup();
        }
    }

    private void updateCustomerInfo() {
        String selectedName = (String) customerComboBox.getSelectedItem();
        if (selectedName == null || selectedName.trim().isEmpty()) {
            clearCustomerInfo();
            return;
        }

        KhachHang selectedCustomer = customers.stream()
            .filter(kh -> kh.getHoTen().equalsIgnoreCase(selectedName))
            .findFirst()
            .orElse(null);

        if (selectedCustomer != null) {
            customerIdLabel.setText("🆔 " + messages.getString("usernameLabel") + selectedCustomer.getMaNguoiDung());
            customerNameLabel.setText("👤 " + messages.getString("fullNameLabel") + selectedCustomer.getHoTen());
            customerPhoneLabel.setText("📞 " + messages.getString("phoneLabel") + selectedCustomer.getSoDienThoai());
            customerEmailLabel.setText("✉️ " + messages.getString("emailLabel") + (selectedCustomer.getEmail() != null ? selectedCustomer.getEmail() : ""));
        } else {
            clearCustomerInfo();
        }
    }

    private void clearCustomerInfo() {
        customerIdLabel.setText("🆔 " + messages.getString("usernameLabel"));
        customerNameLabel.setText("👤 " + messages.getString("fullNameLabel"));
        customerPhoneLabel.setText("📞 " + messages.getString("phoneLabel"));
        customerEmailLabel.setText("✉️ " + messages.getString("emailLabel"));
    }

    private void loadCustomers() {
        try {
            customers = khachHangController.findRecentKhachHang(10);
            updateComboBoxModel(customers);
            updateCustomerNameList();
            if (customers.isEmpty()) {
                showSnackbar("Không tìm thấy khách hàng nào trong hệ thống!", false);
            }
        } catch (SQLException e) {
            showSnackbar(messages.getString("dbError") + e.getMessage(), false);
        }
    }

    private void loadMovies() {
        try {
            movies = phimController.getAllPhim();
            tableModel.setRowCount(0);
            if (movies.isEmpty()) {
                showSnackbar("Không có phim nào đang chiếu!", false);
            }
            for (Phim phim : movies) {
                tableModel.addRow(new Object[]{
                    phim.getMaPhim(),
                    phim.getTenPhim(),
                    phim.getTenTheLoai(),
                    phim.getThoiLuong() + " phút",
                    phim.getNgayKhoiChieu() != null ? phim.getNgayKhoiChieu().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "",
                    phim.getNuocSanXuat()
                });
            }
        } catch (SQLException e) {
            showSnackbar(messages.getString("dbError") + e.getMessage(), false);
        }
    }

    private void bookTicket() {
        int selectedRow = movieTable.getSelectedRow();
        String selectedName = (String) customerComboBox.getSelectedItem();

        if (selectedRow == -1) {
            showSnackbar("Vui lòng chọn một phim để đặt vé!", false);
            return;
        }

        if (selectedName == null || selectedName.trim().isEmpty()) {
            showSnackbar("Vui lòng chọn hoặc nhập thông tin khách hàng!", false);
            return;
        }

        KhachHang selectedCustomer = customers.stream()
            .filter(kh -> kh.getHoTen().equalsIgnoreCase(selectedName))
            .findFirst()
            .orElse(null);

        if (selectedCustomer == null) {
            showSnackbar("Khách hàng không tồn tại! Vui lòng kiểm tra lại.", false);
            return;
        }

        int maPhim = (int) tableModel.getValueAt(selectedRow, 0);
        int maKhachHang = selectedCustomer.getMaNguoiDung();

        try {
            BookingView bookingView = new BookingView(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                datVeController,
                paymentController,
                maPhim,
                maKhachHang,
                _ -> {
                    loadMovies();
                    showSnackbar(messages.getString("success"), true);
                }
            );
            bookingView.setVisible(true);
        } catch (Exception e) {
            showSnackbar(messages.getString("dbError") + e.getMessage(), false);
        }
    }

    private void showSnackbar(String message, boolean success) {
        if (snackbarPanel == null) {
            snackbarPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(getBackground());
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                }
            };
            snackbarPanel.setOpaque(false);
            snackbarPanel.setLayout(new BorderLayout());
            snackbarPanel.setBounds(50, 30, 300, 40);
            snackbarPanel.setBackground(success ? new Color(46, 204, 113, 200) : new Color(231, 76, 60, 200));

            JLabel messageLabel = new JLabel(message, JLabel.CENTER);
            messageLabel.setFont(new Font("Roboto", Font.BOLD, 14));
            messageLabel.setForeground(Color.WHITE);
            snackbarPanel.add(messageLabel, BorderLayout.CENTER);

            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            frame.getLayeredPane().add(snackbarPanel, Integer.valueOf(JLayeredPane.POPUP_LAYER));
        } else {
            JLabel messageLabel = (JLabel) snackbarPanel.getComponent(0);
            messageLabel.setText(message);
            snackbarPanel.setBackground(success ? new Color(46, 204, 113, 200) : new Color(231, 76, 60, 200));
        }

        snackbarPanel.setVisible(true);
        Timer timer = new Timer(3000, e -> {
            snackbarPanel.setVisible(false);
        });
        timer.setRepeats(false);
        timer.start();
    }

    private ImageIcon getScaledIcon(String path, int width, int height) {
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(path));
            Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception e) {
            return new ImageIcon();
        }
    }

    // Custom border for search box
    private static class CustomSearchBorder extends javax.swing.border.AbstractBorder {
        private final ImageIcon icon;

        public CustomSearchBorder(ImageIcon icon) {
            this.icon = icon;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            super.paintBorder(c, g, x, y, width, height);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int iconX = x + 5;
            int iconY = y + (height - icon.getIconHeight()) / 2;
            icon.paintIcon(c, g, iconX, iconY);
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(5, 30, 5, 5);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = 30;
            insets.top = 5;
            insets.right = 5;
            insets.bottom = 5;
            return insets;
        }
    }

    private JPanel createMoviePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 20, 20);
                g2d.setColor(SHADOW_COLOR);
                g2d.drawRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 20, 20);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel movieTitle = new JLabel("Danh sách phim đang chiếu");
        movieTitle.setFont(HEADER_FONT);
        movieTitle.setForeground(CINESTAR_BLUE);
        panel.add(movieTitle, BorderLayout.NORTH);

        // Movie table
        String[] columnNames = {"Mã phim", "Tên phim", "Thể loại", "Thời lượng", "Ngày khởi chiếu", "Nước sản xuất"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        movieTable = new JTable(tableModel);
        movieTable.setFont(LABEL_FONT);
        movieTable.setRowHeight(35);
        movieTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        movieTable.getTableHeader().setFont(HEADER_FONT);
        movieTable.getTableHeader().setBackground(CINESTAR_BLUE);
        movieTable.getTableHeader().setForeground(Color.WHITE);
        movieTable.setGridColor(new Color(200, 200, 200));
        movieTable.setShowGrid(true);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < movieTable.getColumnCount(); i++) {
            movieTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(movieTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Book ticket button
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);
        JButton bookButton = new JButton("Đặt vé");
        bookButton.setFont(HEADER_FONT);
        bookButton.setBackground(CINESTAR_YELLOW);
        bookButton.setForeground(CINESTAR_BLUE);
        bookButton.setFocusPainted(false);
        bookButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        bookButton.setIcon(getScaledIcon("/icons/ticket.png", 20, 20));
        bookButton.setToolTipText("Đặt vé cho khách hàng đã chọn");
        bookButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                bookButton.setBackground(CINESTAR_YELLOW.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                bookButton.setBackground(CINESTAR_YELLOW);
            }
        });
        bookButton.addActionListener(_ -> bookTicket());
        buttonPanel.add(bookButton, BorderLayout.EAST);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    // Custom JTextField with placeholder support
    class PlaceholderTextField extends JTextField {
        private String placeholder;
        private Color placeholderColor = new Color(150, 150, 150);

        public PlaceholderTextField(String placeholder) {
            this.placeholder = placeholder;
        }

        public void setPlaceholder(String placeholder) {
            this.placeholder = placeholder;
            repaint();
        }

        public void setPlaceholderColor(Color color) {
            this.placeholderColor = color;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && placeholder != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setFont(getFont());
                g2.setColor(placeholderColor);
                Insets insets = getInsets();
                int padding = 2;
                g2.drawString(placeholder, insets.left + padding, getHeight() / 2 + getFont().getSize() / 2 - 2);
                g2.dispose();
            }
        }
    }
}