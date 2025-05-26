package com.cinema.views.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
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

// Import ModernUIComponents
import static com.cinema.views.admin.ModernUIComponents.*;

public class SellTicketView extends JPanel {
    // Constants
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
        // Sử dụng panel với hiệu ứng đổ bóng từ ModernUIComponents
        JPanel panel = createRoundedPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(350, 0));

        // Title - Sử dụng header label từ ModernUIComponents
        JLabel titleLabel = createHeaderLabel("Thông tin khách hàng");
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
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setMaximumSize(new Dimension(1000, 40));

        // ComboBox
        comboBoxModel = new DefaultComboBoxModel<>();
        customerComboBox = new JComboBox<>(comboBoxModel);
        customerComboBox.setFont(BODY_FONT);
        customerComboBox.setEditable(true);
        customerComboBox.setBackground(Color.WHITE);
        customerComboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 5, 5, 5),
            new ShadowBorder()
        ));

        // Sử dụng PlaceholderTextField từ ModernUIComponents
        PlaceholderTextField placeholderField = new PlaceholderTextField("Nhập tên, số điện thoại hoặc email...");
        placeholderField.setFont(BODY_FONT);
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
        loadingLabel = new JLabel(getIcon("/images/Icon/loading.gif", 20, 20));
        loadingLabel.setVisible(false);
        panel.add(loadingLabel, BorderLayout.EAST);
        panel.add(customerComboBox, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCustomerInfoPanel() {
        JPanel panel = createRoundedPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR), 
            "Chi tiết khách hàng", 0, 0, TITLE_FONT, PRIMARY_COLOR
        ));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Customer info labels - Sử dụng infoLabel từ ModernUIComponents
        customerIdLabel = createInfoLabel("🆔 " + messages.getString("usernameLabel"));
        customerNameLabel = createInfoLabel("👤 " + messages.getString("fullNameLabel"));
        customerPhoneLabel = createInfoLabel("📞 " + messages.getString("phoneLabel"));
        customerEmailLabel = createInfoLabel("✉️ " + messages.getString("emailLabel"));

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
                    editor.setForeground(TEXT_COLOR);
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
            editor.setForeground(TEXT_COLOR);
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
            snackbarPanel = createRoundedPanel();
            snackbarPanel.setLayout(new BorderLayout());
            snackbarPanel.setBounds(50, 30, 300, 40);
            snackbarPanel.setBackground(success ? SUCCESS_COLOR : ERROR_COLOR);

            JLabel messageLabel = new JLabel(message, JLabel.CENTER);
            messageLabel.setFont(BODY_FONT);
            messageLabel.setForeground(Color.WHITE);
            snackbarPanel.add(messageLabel, BorderLayout.CENTER);

            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            frame.getLayeredPane().add(snackbarPanel, Integer.valueOf(JLayeredPane.POPUP_LAYER));
        } else {
            JLabel messageLabel = (JLabel) snackbarPanel.getComponent(0);
            messageLabel.setText(message);
            snackbarPanel.setBackground(success ? SUCCESS_COLOR : ERROR_COLOR);
        }

        snackbarPanel.setVisible(true);
        Timer timer = new Timer(3000, e -> {
            snackbarPanel.setVisible(false);
        });
        timer.setRepeats(false);
        timer.start();
    }

    private JPanel createMoviePanel() {
        // Sử dụng panel với hiệu ứng đổ bóng từ ModernUIComponents
        JPanel panel = createRoundedPanel();
        panel.setLayout(new BorderLayout(0, 20));

        // Title
        JLabel movieTitle = createHeaderLabel("Danh sách phim đang chiếu");
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
        movieTable.setFont(BODY_FONT);
        movieTable.setRowHeight(35);
        movieTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        movieTable.getTableHeader().setFont(TITLE_FONT);
        movieTable.getTableHeader().setBackground(PRIMARY_COLOR);
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

        // Book ticket button - Sử dụng button từ ModernUIComponents
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);
        JButton bookButton = createIconButton("Đặt vé", "/images/Icon/ticket.png", SECONDARY_COLOR, PRIMARY_COLOR);
        bookButton.setToolTipText("Đặt vé cho khách hàng đã chọn");
        bookButton.addActionListener(_ -> bookTicket());
        buttonPanel.add(bookButton, BorderLayout.EAST);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }
}