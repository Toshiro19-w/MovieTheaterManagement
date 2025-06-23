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

import com.cinema.components.ModernUIApplier;
import com.cinema.components.ModernUIComponents.PlaceholderTextField;
import static com.cinema.components.ModernUIComponents.getIcon;
import com.cinema.components.UIConstants;
import com.cinema.controllers.DatVeController;
import com.cinema.controllers.KhachHangController;
import com.cinema.controllers.PaymentController;
import com.cinema.controllers.SimplePhimController;
import com.cinema.models.KhachHang;
import com.cinema.models.NhanVien;
import com.cinema.models.Phim;
import com.cinema.services.GheService;
import com.cinema.services.KhachHangService;
import com.cinema.services.PhimService;
import com.cinema.services.SuatChieuService;
import com.cinema.services.VeService;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.SnackbarUtil;
import com.cinema.utils.ValidationUtils;
import com.cinema.views.booking.BookingView;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import com.cinema.components.CustomerInfoRow;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.FlowLayout;

public class SellTicketView extends JPanel implements com.cinema.views.common.ResizableView {
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
    private CustomerInfoRow idRow, phoneRow, emailRow;
    private JLabel nameLabel;
    private JTable movieTable;
    private DefaultTableModel tableModel;
    private JLabel searchErrorLabel;
    private JLabel loadingLabel;
    private JPanel snackbarPanel;
    private JPanel searchPanel;

    // Data
    private NhanVien currentNhanVien;
    private List<KhachHang> customers;
    private List<Phim> movies;
    private ResourceBundle messages;
    private EventList<String> customerNameList;
    private final Timer searchTimer;
        
    @SuppressWarnings("static-access")
    public SellTicketView(NhanVien nhanVien) throws IOException, SQLException {
        if (nhanVien == null) {
            throw new IllegalArgumentException("NhanVien không thể là null");
        }
        
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
        this.currentNhanVien = nhanVien;
        customers = new ArrayList<>();
        movies = new ArrayList<>();
        messages = ResourceBundle.getBundle("Messages");
        customerNameList = new BasicEventList<>();
        searchTimer = new Timer(SEARCH_DELAY, _ -> performSearch());
        searchTimer.setRepeats(false);

        SwingUtilities.invokeLater(() -> {
            try {
                initUI();
                loadCustomers();
                loadMovies();
                JTextField editor = (JTextField) customerComboBox.getEditor().getEditorComponent();
                editor.requestFocusInWindow();
            } catch (Exception e) {
                SnackbarUtil.showSnackbar(this, messages.getString("error") + ": " + e.getMessage(), false);
            }
        });
    }

    private void initUI() {
        // Khởi tạo các thành phần trước
        nameLabel = new JLabel("Chưa chọn khách hàng");
        nameLabel.setFont(UIConstants.HEADER_FONT);
        phoneRow = new CustomerInfoRow(getIcon("/images/Icon/ticket.png", 16, 16), "", "");
        emailRow = new CustomerInfoRow(getIcon("/images/Icon/invoice.png", 16, 16), "", "");
        idRow = new CustomerInfoRow(getIcon("/images/Icon/user.png", 16, 16), "", "");

        // Khởi tạo tableModel và movieTable trước
        String[] columnNames = {"Mã phim", "Tên phim", "Thể loại", "Thời lượng", "Ngày khởi chiếu", "Nước sản xuất"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        movieTable = new JTable(tableModel);
        movieTable.setFont(UIConstants.BODY_FONT);
        movieTable.setRowHeight(UIConstants.ROW_HEIGHT);
        movieTable.getTableHeader().setFont(UIConstants.SUBHEADER_FONT);
        movieTable.getTableHeader().setBackground(UIConstants.PRIMARY_COLOR);
        movieTable.getTableHeader().setForeground(UIConstants.BUTTON_TEXT_COLOR);

        // Setup UI
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(UIConstants.BACKGROUND_COLOR);

        // Card khách hàng
        JPanel customerCard = createCardPanel();
        customerCard.setLayout(new BoxLayout(customerCard, BoxLayout.Y_AXIS));
        customerCard.add(createCustomerHeaderPanel());
        customerCard.add(Box.createVerticalStrut(UIConstants.PADDING_LARGE));
        customerCard.add(createCustomerSearchPanel());
        customerCard.add(Box.createVerticalStrut(UIConstants.PADDING_LARGE));
        customerCard.add(createCustomerInfoFieldsPanel());
        add(customerCard);
        add(Box.createVerticalStrut(UIConstants.PADDING_LARGE + 4));

        // Card danh sách phim
        JPanel movieCard = createCardPanel();
        movieCard.setLayout(new BoxLayout(movieCard, BoxLayout.Y_AXIS));
        movieCard.add(createMovieHeaderPanel());
        movieCard.add(Box.createVerticalStrut(UIConstants.PADDING_LARGE));
        movieCard.add(createMovieTablePanel());
        movieCard.add(Box.createVerticalStrut(UIConstants.PADDING_LARGE));
        movieCard.add(createBookButtonPanel());
        add(movieCard);

        // Sử dụng PlaceholderTextField từ ModernUIComponents
        PlaceholderTextField placeholderField = new PlaceholderTextField("Nhập tên, số điện thoại hoặc email...");
        placeholderField.setFont(UIConstants.BODY_FONT);
        placeholderField.setBorder(BorderFactory.createEmptyBorder(UIConstants.PADDING_SMALL, UIConstants.PADDING_SMALL, UIConstants.PADDING_SMALL, UIConstants.PADDING_SMALL));
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

        setupAutoComplete();
    }

    private JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(UIConstants.CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(
                UIConstants.PADDING_LARGE, UIConstants.PADDING_LARGE, UIConstants.PADDING_LARGE, UIConstants.PADDING_LARGE)
        ));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    private JPanel createCustomerHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel title = new JLabel("Thông Tin Khách Hàng");
        title.setFont(UIConstants.HEADER_FONT);
        panel.add(title, BorderLayout.WEST);
        return panel;
    }

    private JPanel createCustomerSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(UIConstants.PADDING_SMALL, 0));
        panel.setOpaque(false);
        panel.add(createSearchPanel(), BorderLayout.CENTER);
        // Có thể thêm nút "Thêm khách hàng" ở đây nếu muốn
        return panel;
    }

    private JPanel createCustomerInfoFieldsPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(UIConstants.PADDING_SMALL, UIConstants.PADDING_MEDIUM, UIConstants.PADDING_SMALL, UIConstants.PADDING_MEDIUM);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Họ tên
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel nameLabelLabel = new JLabel("Họ và tên:");
        nameLabelLabel.setFont(UIConstants.LABEL_FONT);
        panel.add(nameLabelLabel, gbc);
        gbc.gridx = 1;
        panel.add(nameLabel, gbc);

        // Số điện thoại
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel phoneLabel = new JLabel("Số điện thoại:");
        phoneLabel.setFont(UIConstants.LABEL_FONT);
        panel.add(phoneLabel, gbc);
        gbc.gridx = 1;
        panel.add(phoneRow, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(UIConstants.LABEL_FONT);
        panel.add(emailLabel, gbc);
        gbc.gridx = 1;
        panel.add(emailRow, gbc);

        // Tài khoản (ID)
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel idLabel = new JLabel("Tài khoản:");
        idLabel.setFont(UIConstants.LABEL_FONT);
        panel.add(idLabel, gbc);
        gbc.gridx = 1;
        panel.add(idRow, gbc);

        return panel;
    }

    private JPanel createMovieHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel title = new JLabel("Danh Sách Phim Đang Chiếu");
        title.setFont(UIConstants.HEADER_FONT);
        panel.add(title, BorderLayout.WEST);
        return panel;
    }

    private JPanel createMovieTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JScrollPane scrollPane = new JScrollPane(movieTable);
        scrollPane.setPreferredSize(new Dimension(0, UIConstants.ROW_HEIGHT * 10)); // Chỉ đủ 8-10 phim
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBookButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setOpaque(false);
        JButton bookButton = ModernUIApplier.createModernButton("Đặt vé", UIConstants.SECONDARY_COLOR, UIConstants.PRIMARY_COLOR);
        bookButton.setFont(UIConstants.BUTTON_FONT);
        bookButton.setPreferredSize(new Dimension(120, 40));
        bookButton.addActionListener(_ -> bookTicket());
        panel.add(bookButton);
        return panel;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setMaximumSize(new Dimension(1000, 40));

        // Tạo panel chứa icon tìm kiếm và combobox
        JPanel searchInputPanel = new JPanel(new BorderLayout(5, 0));
        searchInputPanel.setOpaque(false);

        // Icon tìm kiếm
        JLabel searchIcon = new JLabel(getIcon("/images/Icon/search.png", 16, 16));
        searchIcon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        searchInputPanel.add(searchIcon, BorderLayout.WEST);

        // ComboBox
        comboBoxModel = new DefaultComboBoxModel<>();
        customerComboBox = new JComboBox<>(comboBoxModel);
        customerComboBox.setFont(UIConstants.BODY_FONT);
        customerComboBox.setEditable(true);
        customerComboBox.setBackground(Color.WHITE);
        customerComboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 5, 5, 5),
            ModernUIApplier.createShadowBorder()
        ));

        // Sử dụng PlaceholderTextField từ ModernUIComponents
        PlaceholderTextField placeholderField = new PlaceholderTextField("Nhập tên, số điện thoại hoặc email...");
        placeholderField.setFont(UIConstants.BODY_FONT);
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

        searchInputPanel.add(customerComboBox, BorderLayout.CENTER);
        panel.add(searchInputPanel, BorderLayout.CENTER);

        // Loading indicator và nút làm mới
        JPanel rightPanel = new JPanel(new BorderLayout(5, 0));
        rightPanel.setOpaque(false);

        loadingLabel = new JLabel(getIcon("/images/Icon/loading.gif", 20, 20));
        loadingLabel.setVisible(false);
        rightPanel.add(loadingLabel, BorderLayout.WEST);

        // Nút làm mới
        JButton refreshButton = new JButton(getIcon("/images/Icon/refresh-button.png", 16, 16));
        refreshButton.setBorderPainted(false);
        refreshButton.setContentAreaFilled(false);
        refreshButton.setFocusPainted(false);
        refreshButton.setToolTipText("Làm mới danh sách khách hàng");
        refreshButton.addActionListener(e -> {
            loadCustomers();
            JTextField editor = (JTextField) customerComboBox.getEditor().getEditorComponent();
            editor.setText("");
            clearCustomerInfo();
        });
        rightPanel.add(refreshButton, BorderLayout.EAST);

        panel.add(rightPanel, BorderLayout.EAST);

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
                    editor.setForeground(UIConstants.TEXT_COLOR);
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
            editor.setForeground(UIConstants.TEXT_COLOR);
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
            nameLabel.setText(selectedCustomer.getHoTen());
            idRow.setValue(String.valueOf(selectedCustomer.getMaNguoiDung()));
            phoneRow.setValue(selectedCustomer.getSoDienThoai());
            emailRow.setValue(selectedCustomer.getEmail() != null ? selectedCustomer.getEmail() : "");
        } else {
            clearCustomerInfo();
        }
    }

    private void clearCustomerInfo() {
        nameLabel.setText("Chưa chọn khách hàng");
        idRow.setValue("");
        phoneRow.setValue("");
        emailRow.setValue("");
    }

    private void loadCustomers() {
        try {
            customers = khachHangController.findRecentKhachHang(10);
            updateComboBoxModel(customers);
            updateCustomerNameList();
            if (customers.isEmpty()) {
                SnackbarUtil.showSnackbar(this, "Không tìm thấy khách hàng nào trong hệ thống!", false);
            }
        } catch (SQLException e) {
            SnackbarUtil.showSnackbar(this, messages.getString("dbError") + e.getMessage(), false);
            e.printStackTrace();
        }
    }

    private void loadMovies() {
        try {
            // Chỉ lấy phim đang chiếu thay vì tất cả phim
            movies = phimController.getPhimDangChieu();
            tableModel.setRowCount(0);
            if (movies.isEmpty()) {
                SnackbarUtil.showSnackbar(this, "Không có phim nào đang chiếu!", false);
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
            SnackbarUtil.showSnackbar(this, messages.getString("dbError") + e.getMessage(), false);
        }
    }

    private void bookTicket() {
        int selectedRow = movieTable.getSelectedRow();
        String selectedName = (String) customerComboBox.getSelectedItem();

        if (selectedRow == -1) {
            SnackbarUtil.showSnackbar(this, "Vui lòng chọn một phim để đặt vé!", false);
            return;
        }

        if (selectedName == null || selectedName.trim().isEmpty()) {
            SnackbarUtil.showSnackbar(this, "Vui lòng chọn hoặc nhập thông tin khách hàng!", false);
            return;
        }

        KhachHang selectedCustomer = customers.stream()
            .filter(kh -> kh.getHoTen().equalsIgnoreCase(selectedName))
            .findFirst()
            .orElse(null);

        if (selectedCustomer == null) {
            SnackbarUtil.showSnackbar(this, "Khách hàng không tồn tại! Vui lòng kiểm tra lại.", false);
            return;
        }          int maPhim = (int) tableModel.getValueAt(selectedRow, 0);
        int maKhachHang = selectedCustomer.getMaNguoiDung();
        // Không cho phép đặt null cho mã nhân viên, sử dụng mã khách hàng khi tự đặt vé
        int maNhanVien = (currentNhanVien != null && currentNhanVien.getMaNguoiDung() > 0) 
                        ? currentNhanVien.getMaNguoiDung() 
                        : maKhachHang; // Sử dụng chính mã của khách hàng khi họ tự đặt vé
        
        try {
            BookingView bookingView = new BookingView(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                datVeController,
                paymentController,
                maPhim,
                maKhachHang,
                maNhanVien,
                _ -> {
                    loadMovies();
                    SnackbarUtil.showSnackbar(this, messages.getString("success"), true);
                }
            );
            bookingView.setVisible(true);
        } catch (Exception e) {
            SnackbarUtil.showSnackbar(this, messages.getString("dbError") + e.getMessage(), false);
            e.printStackTrace();
        }
    }
}

