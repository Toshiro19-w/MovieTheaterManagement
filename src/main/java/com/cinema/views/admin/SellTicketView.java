package com.cinema.views.admin;

import com.cinema.components.ModernUIApplier;
import com.cinema.components.ModernUIComponents.PlaceholderTextField;
import com.cinema.components.UIConstants;
import com.cinema.controllers.DatVeController;
import com.cinema.controllers.KhachHangController;
import com.cinema.controllers.PaymentController;
import com.cinema.controllers.SimplePhimController;
import com.cinema.enums.TrangThaiVe;
import com.cinema.models.*;
import com.cinema.models.dto.BookingResultDTO;
import com.cinema.models.repositories.HoaDonRepository;
import com.cinema.services.*;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.SnackbarUtil;
import com.cinema.utils.VePdfExporter;
import com.cinema.views.booking.BookingView;
import com.cinema.utils.HoaDonPdfExporter;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.AutoCompleteSupport;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class SellTicketView extends JPanel {
    // Controllers & Data
    private final KhachHangController khachHangController;
    private final SimplePhimController phimController;
    private final DatVeController datVeController;
    private final PaymentController paymentController;
    private final DatabaseConnection databaseConnection;
    private final HoaDonRepository hoaDonRepo;
    private NhanVien currentNhanVien;
    private List<KhachHang> customers;
    private List<Phim> movies;
    private ResourceBundle messages;
    private EventList<String> customerNameList;
    private final Timer searchTimer;

    // UI Components
    private JComboBox<String> customerComboBox;
    private DefaultComboBoxModel<String> comboBoxModel;
    private JLabel nameLabel, phoneLabel, emailLabel, idLabel;
    private JTable movieTable;
    private DefaultTableModel tableModel;
    private JLabel loadingLabel;
    private JButton bookButton, buyButton, printButton;
    private JTextField cashField; // Moved to instance variable for access

    // Data
    private List<Ve> selectedTickets = new ArrayList<>();
    private HoaDon hoaDonThanhToan = null;

    public SellTicketView(NhanVien nhanVien) throws IOException, SQLException {
        if (nhanVien == null) throw new IllegalArgumentException("NhanVien không thể là null");

        // Initialize controllers
        databaseConnection = new DatabaseConnection();
        khachHangController = new KhachHangController(new KhachHangService(databaseConnection));
        phimController = new SimplePhimController(new PhimService(databaseConnection));
        datVeController = new DatVeController(
                new SuatChieuService(databaseConnection),
                new GheService(databaseConnection),
                new VeService(databaseConnection)
        );
        paymentController = new PaymentController();
        hoaDonRepo = new HoaDonRepository(databaseConnection);

        // Initialize data
        this.currentNhanVien = nhanVien;
        customers = new ArrayList<>();
        movies = new ArrayList<>();
        messages = ResourceBundle.getBundle("Messages");
        customerNameList = new BasicEventList<>();
        searchTimer = new Timer(300, _ -> performSearch());
        searchTimer.setRepeats(false);

        // Initialize UI
        SwingUtilities.invokeLater(this::initUI);
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Center: Movie panel fills all space
        JPanel moviePanel = createMoviePanel();
        add(moviePanel, BorderLayout.CENTER);

        // Right: Customer panel fixed width
        JPanel customerPanel = createCustomerPanel();
        add(customerPanel, BorderLayout.EAST);

        loadCustomers();
        loadMovies();
    }

    private JPanel createMoviePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(UIConstants.CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1, true),
                new EmptyBorder(10, 10, 10, 10)));

        JLabel title = new JLabel("Danh Sách Phim Đang Chiếu");
        title.setFont(UIConstants.HEADER_FONT);
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setOpaque(false);
        headerPanel.add(title);
        panel.add(headerPanel, BorderLayout.NORTH);

        String[] columnNames = {"Mã phim", "Tên phim", "Thể loại", "Thời lượng", "Ngày khởi chiếu", "Nước sản xuất"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        movieTable = new JTable(tableModel);
        movieTable.setFont(UIConstants.BODY_FONT);
        movieTable.setRowHeight(UIConstants.ROW_HEIGHT);
        movieTable.getTableHeader().setFont(UIConstants.SUBHEADER_FONT);
        movieTable.getTableHeader().setBackground(UIConstants.PRIMARY_COLOR);
        movieTable.getTableHeader().setForeground(UIConstants.BUTTON_TEXT_COLOR);
        movieTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(movieTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        movieTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < columnNames.length; i++) {
            movieTable.getColumnModel().getColumn(i).setPreferredWidth(140);
        }
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCustomerPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(UIConstants.CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1, true),
                new EmptyBorder(10, 10, 10, 10)));
        panel.setPreferredSize(new Dimension(340, 0)); // Fixed width

        JLabel title = new JLabel("Thông Tin Khách Hàng");
        title.setFont(UIConstants.HEADER_FONT);
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setOpaque(false);
        headerPanel.add(title);
        panel.add(headerPanel, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JPanel searchPanel = createSearchPanel();
        infoPanel.add(searchPanel);
        infoPanel.add(Box.createVerticalStrut(10));

        nameLabel = new JLabel("Chưa chọn khách hàng");
        nameLabel.setFont(UIConstants.SUBHEADER_FONT);
        phoneLabel = new JLabel(""); phoneLabel.setFont(UIConstants.BODY_FONT);
        emailLabel = new JLabel(""); emailLabel.setFont(UIConstants.BODY_FONT);
        idLabel = new JLabel(""); idLabel.setFont(UIConstants.BODY_FONT);

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 2, 4, 2);
        gbc.fill = GridBagConstraints.NONE;

        int iconSize = 16;
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        fieldsPanel.add(new JLabel(getIcon("/images/Icon/user.png", iconSize, iconSize)), gbc);
        gbc.gridx = 1; gbc.weightx = 0;
        fieldsPanel.add(new JLabel("Họ và tên:"), gbc);
        gbc.gridx = 2; gbc.weightx = 1.0;
        nameLabel.setPreferredSize(new Dimension(170, 22));
        fieldsPanel.add(nameLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        fieldsPanel.add(new JLabel(getIcon("/images/Icon/phone.png", iconSize, iconSize)), gbc);
        gbc.gridx = 1; fieldsPanel.add(new JLabel("Số điện thoại:"), gbc);
        gbc.gridx = 2;
        phoneLabel.setPreferredSize(new Dimension(170, 22));
        fieldsPanel.add(phoneLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        fieldsPanel.add(new JLabel(getIcon("/images/Icon/email.png", iconSize, iconSize)), gbc);
        gbc.gridx = 1; fieldsPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 2;
        emailLabel.setPreferredSize(new Dimension(170, 22));
        fieldsPanel.add(emailLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        fieldsPanel.add(new JLabel(getIcon("/images/Icon/account.png", iconSize, iconSize)), gbc);
        gbc.gridx = 1; fieldsPanel.add(new JLabel("Tài khoản:"), gbc);
        gbc.gridx = 2;
        idLabel.setPreferredSize(new Dimension(170, 22));
        fieldsPanel.add(idLabel, gbc);

        infoPanel.add(fieldsPanel);
        infoPanel.add(Box.createVerticalStrut(20));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        bookButton = ModernUIApplier.createModernButton("Đặt vé", UIConstants.SECONDARY_COLOR, UIConstants.PRIMARY_COLOR);
        bookButton.setPreferredSize(new Dimension(120, 40));
        bookButton.addActionListener(_ -> bookTicket());

        buyButton = ModernUIApplier.createModernButton("Thanh toán", UIConstants.PRIMARY_COLOR, Color.WHITE);
        buyButton.setPreferredSize(new Dimension(120, 40));
        buyButton.addActionListener(_ -> buyTicket());

        printButton = ModernUIApplier.createModernButton("In hóa đơn & vé", UIConstants.SECONDARY_COLOR, UIConstants.PRIMARY_COLOR);
        printButton.setPreferredSize(new Dimension(150, 40));
        printButton.addActionListener(_ -> printInvoiceAndTickets());

        buttonPanel.add(bookButton);
        buttonPanel.add(buyButton);
        buttonPanel.add(printButton);

        infoPanel.add(buttonPanel);
        panel.add(infoPanel, BorderLayout.CENTER);

        return panel;
    }

    private ImageIcon getIcon(String path, int w, int h) {
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(path));
            Image img = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception e) {
            return new ImageIcon();
        }
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JPanel searchInputPanel = new JPanel(new BorderLayout(5, 0));
        searchInputPanel.setOpaque(false);

        comboBoxModel = new DefaultComboBoxModel<>();
        customerComboBox = new JComboBox<>(comboBoxModel);
        customerComboBox.setFont(UIConstants.BODY_FONT);
        customerComboBox.setEditable(true);
        customerComboBox.setBackground(Color.WHITE);

        PlaceholderTextField placeholderField = new PlaceholderTextField("Nhập tên, số điện thoại hoặc email...");
        placeholderField.setPreferredSize(new Dimension(100, 28));
        placeholderField.setFont(UIConstants.BODY_FONT);
        customerComboBox.setEditor(new BasicComboBoxEditor() {
            @Override
            public Component getEditorComponent() {
                return placeholderField;
            }

            @Override
            public void setItem(Object anObject) {
                placeholderField.setText(anObject != null ? anObject.toString() : "");
            }

            @Override
            public Object getItem() {
                return placeholderField.getText();
            }
        });

        searchInputPanel.add(customerComboBox, BorderLayout.CENTER);
        panel.add(searchInputPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout(5, 0));
        rightPanel.setOpaque(false);

        loadingLabel = new JLabel(new ImageIcon(getClass().getResource("/images/Icon/loading.gif")));
        loadingLabel.setVisible(false);
        rightPanel.add(loadingLabel, BorderLayout.WEST);

        setupAutoComplete();
        return panel;
    }

    private void setupAutoComplete() {
        JTextField editor = (JTextField) customerComboBox.getEditor().getEditorComponent();
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
                validateAndUpdateSelection();
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
            loadCustomers();
            customerComboBox.setSelectedItem(null);
            clearCustomerInfo();
            customerComboBox.hidePopup();
            return;
        }

        try {
            customers = khachHangController.searchKhachHang(searchText);
            if (customers.isEmpty()) {
                comboBoxModel.removeAllElements();
                customerComboBox.hidePopup();
                clearCustomerInfo();
                SnackbarUtil.showSnackbar(this, "Không tìm thấy khách hàng", false);
                return;
            }

            updateComboBoxModel(customers);
            updateCustomerNameList();

            boolean exactMatch = customers.stream()
                    .anyMatch(kh -> kh.getHoTen().equalsIgnoreCase(searchText) ||
                            kh.getSoDienThoai().equals(searchText) ||
                            (kh.getEmail() != null && kh.getEmail().equalsIgnoreCase(searchText)));

            if (exactMatch) {
                customerComboBox.setSelectedItem(searchText);
                customerComboBox.hidePopup();
                updateCustomerInfo();
            } else {
                customerComboBox.setSelectedItem(null);
                customerComboBox.showPopup();
            }
        } catch (SQLException ex) {
            comboBoxModel.removeAllElements();
            customerComboBox.hidePopup();
            clearCustomerInfo();
            SnackbarUtil.showSnackbar(this, messages.getString("dbError") + ex.getMessage(), false);
        }
    }

    private void updateComboBoxModel(List<KhachHang> customerList) {
        SwingUtilities.invokeLater(() -> {
            String currentText = ((JTextField) customerComboBox.getEditor().getEditorComponent()).getText().trim();
            comboBoxModel.removeAllElements();
            for (KhachHang kh : customerList) {
                comboBoxModel.addElement(kh.getHoTen());
            }
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
            SnackbarUtil.showSnackbar(this, "Khách hàng không tồn tại", false);
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
            phoneLabel.setText(selectedCustomer.getSoDienThoai());
            emailLabel.setText(selectedCustomer.getEmail() != null ? selectedCustomer.getEmail() : "");
            idLabel.setText(String.valueOf(selectedCustomer.getMaNguoiDung()));
        } else {
            clearCustomerInfo();
        }
    }

    private void clearCustomerInfo() {
        nameLabel.setText("Chưa chọn khách hàng");
        phoneLabel.setText("");
        emailLabel.setText("");
        idLabel.setText("");
    }

    private void loadCustomers() {
        try {
            customers = khachHangController.findRecentKhachHang(10);
            updateComboBoxModel(customers);
            updateCustomerNameList();
            if (customers.isEmpty()) {
                SnackbarUtil.showSnackbar(this, "Không tìm thấy khách hàng nào", false);
            }
        } catch (SQLException e) {
            SnackbarUtil.showSnackbar(this, messages.getString("dbError") + e.getMessage(), false);
        }
    }

    private void loadMovies() {
        try {
            movies = phimController.getPhimDangChieu();
            tableModel.setRowCount(0);
            if (movies.isEmpty()) {
                SnackbarUtil.showSnackbar(this, "Không có phim nào đang chiếu", false);
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
        if (selectedRow == -1) {
            SnackbarUtil.showSnackbar(this, "Vui lòng chọn một phim", false);
            return;
        }

        String selectedName = (String) customerComboBox.getSelectedItem();
        KhachHang selectedCustomer = customers.stream()
                .filter(kh -> selectedName != null && kh.getHoTen().equalsIgnoreCase(selectedName))
                .findFirst()
                .orElse(null);

        int maPhim = (int) tableModel.getValueAt(selectedRow, 0);
        int maKhachHang = selectedCustomer != null ? selectedCustomer.getMaNguoiDung() : 0;
        int maNhanVien = currentNhanVien.getMaNguoiDung();

        try {
            BookingView bookingView = new BookingView(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                datVeController,
                paymentController,
                maPhim,
                maKhachHang,
                maNhanVien,
                bookingResult -> {
                    if (bookingResult instanceof BookingResultDTO) {
                        selectedTickets.clear();

                        BookingResultDTO bookingDTO = (BookingResultDTO) bookingResult;
                        Ve ve = new Ve();
                        ve.setSuatChieu(bookingDTO.suatChieu());
                        ve.setGhe(bookingDTO.ghe());
                        ve.setGiaVeSauGiam(bookingDTO.giaVe());
                        ve.setTenPhim(
                            movies.stream()
                                  .filter(p -> p.getMaPhim() == maPhim)
                                  .findFirst()
                                  .map(Phim::getTenPhim)
                                  .orElse("Unknown")
                        );
                        ve.setNgayGioChieu(bookingDTO.suatChieu().getNgayGioChieu());
                        ve.setMaGhe(bookingDTO.ghe().getMaGhe());
                        ve.setTrangThai(TrangThaiVe.PENDING);

                        selectedTickets.add(ve);
                        loadMovies();
                        SnackbarUtil.showSnackbar(this, "Đặt vé thành công", true);
                    }
                }
            );
            bookingView.setVisible(true);

        } catch (Exception e) {
            SnackbarUtil.showSnackbar(this, messages.getString("dbError") + e.getMessage(), false);
        }
    }

    private void buyTicket() {
        if (selectedTickets.isEmpty()) {
            SnackbarUtil.showSnackbar(this, "Vui lòng đặt vé trước khi thanh toán", false);
            return;
        }

        JDialog paymentDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Thanh Toán", true);
        paymentDialog.setLayout(new GridBagLayout());
        paymentDialog.setSize(400, 300);
        paymentDialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        BigDecimal total = calculateTotal();
        JLabel totalLabel = new JLabel("Tổng tiền: " + total.toString() + " VNĐ");
        totalLabel.setFont(UIConstants.BODY_FONT);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        paymentDialog.add(totalLabel, gbc);

        JLabel paymentMethodLabel = new JLabel("Phương thức thanh toán:");
        paymentMethodLabel.setFont(UIConstants.BODY_FONT);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        paymentDialog.add(paymentMethodLabel, gbc);

        JComboBox<String> paymentMethodCombo = new JComboBox<>(new String[]{"Tiền mặt", "Chuyển khoản"});
        paymentMethodCombo.setFont(UIConstants.BODY_FONT);
        gbc.gridx = 1; gbc.gridy = 1;
        paymentDialog.add(paymentMethodCombo, gbc);

        JLabel cashLabel = new JLabel("Tiền khách đưa:");
        cashLabel.setFont(UIConstants.BODY_FONT);
        gbc.gridx = 0; gbc.gridy = 2;
        paymentDialog.add(cashLabel, gbc);

        cashField = new JTextField(10);
        cashField.setFont(UIConstants.BODY_FONT);
        cashField.setEnabled(true);
        gbc.gridx = 1; gbc.gridy = 2;
        paymentDialog.add(cashField, gbc);

        JLabel changeLabel = new JLabel("Tiền thối lại: 0 VNĐ");
        changeLabel.setFont(UIConstants.BODY_FONT);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        paymentDialog.add(changeLabel, gbc);

        JButton confirmButton = ModernUIApplier.createModernButton("Xác nhận", UIConstants.PRIMARY_COLOR, Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        paymentDialog.add(confirmButton, gbc);

        paymentMethodCombo.addActionListener(e -> {
            boolean isCash = paymentMethodCombo.getSelectedItem().equals("Tiền mặt");
            cashField.setEnabled(isCash);
            cashField.setText("");
            changeLabel.setText("Tiền thối lại: 0 VNĐ");
        });

        cashField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateChange(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateChange(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateChange(); }

            private void updateChange() {
                if (!cashField.isEnabled()) {
                    changeLabel.setText("Tiền thối lại: 0 VNĐ");
                    return;
                }
                try {
                    BigDecimal cash = new BigDecimal(cashField.getText().trim());
                    BigDecimal change = cash.subtract(total);
                    changeLabel.setText("Tiền thối lại: " + (change.compareTo(BigDecimal.ZERO) >= 0 ? change.toString() : "0") + " VNĐ");
                } catch (NumberFormatException ex) {
                    changeLabel.setText("Tiền thối lại: 0 VNĐ");
                }
            }
        });

        confirmButton.addActionListener(e -> {
            String method = (String) paymentMethodCombo.getSelectedItem();
            BigDecimal cash = null, change = null;

            if (method.equals("Tiền mặt")) {
                try {
                    cash = new BigDecimal(cashField.getText().trim());
                    if (cash.compareTo(total) < 0) {
                        JOptionPane.showMessageDialog(paymentDialog, "Tiền khách đưa không đủ", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    change = cash.subtract(total);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(paymentDialog, "Vui lòng nhập số tiền hợp lệ", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            String selectedName = (String) customerComboBox.getSelectedItem();
            KhachHang selectedCustomer = customers.stream()
                    .filter(kh -> selectedName != null && kh.getHoTen().equalsIgnoreCase(selectedName))
                    .findFirst()
                    .orElse(null);

            HoaDon hoaDon = new HoaDon();
            hoaDon.setMaNhanVien(currentNhanVien.getMaNguoiDung());
            hoaDon.setNgayLap(LocalDateTime.now());

            // QUAN TRỌNG: chỉ truyền null nếu khách vãng lai
            if (selectedCustomer == null) {
                hoaDon.setMaKhachHang(null);
                hoaDon.setTenKhachHang("Khách vãng lai");
            } else {
                hoaDon.setMaKhachHang(selectedCustomer.getMaNguoiDung());
                hoaDon.setTenKhachHang(selectedCustomer.getHoTen());
            }

            boolean insertSuccess = false;
            try {
                insertSuccess = hoaDonRepo.insert(hoaDon);
            } catch (SQLException e1) {
                SnackbarUtil.showSnackbar(this, "Lỗi hệ thống: " + e1.getMessage(), false);
                e1.printStackTrace();	
                return;
            }
            if (!insertSuccess) {
                SnackbarUtil.showSnackbar(this, "Lưu hóa đơn thất bại!", false);
                return;
            }
            hoaDonThanhToan = new HoaDon();
            hoaDonThanhToan.setMaHoaDon(hoaDon.getMaHoaDon());
            hoaDonThanhToan.setMaNhanVien(currentNhanVien.getMaNguoiDung());
            hoaDonThanhToan.setTenNhanVien(currentNhanVien.getHoTen());
            hoaDonThanhToan.setMaKhachHang(selectedCustomer != null ? selectedCustomer.getMaNguoiDung() : null);
            hoaDonThanhToan.setTenKhachHang(selectedCustomer != null ? selectedCustomer.getHoTen() : "Khách vãng lai");
            hoaDonThanhToan.setNgayLap(hoaDon.getNgayLap());
            hoaDonThanhToan.setTongTien(total);
            hoaDonThanhToan.setTienKhachDua(cash);
            hoaDonThanhToan.setTienThoiLai(change);
            hoaDonThanhToan.setPhuongThucThanhToan(method);
            hoaDonThanhToan.setDanhSachVe(new ArrayList<>(selectedTickets));

            try {
                for (Ve ve : selectedTickets) {
                    ve.setTrangThai(TrangThaiVe.PAID);
                    ve.setHoaDon(hoaDonThanhToan);
                }
                paymentDialog.dispose();
                SnackbarUtil.showSnackbar(this, "Thanh toán thành công", true);
            } catch (Exception ex) {
                SnackbarUtil.showSnackbar(this, "Lỗi khi xử lý thanh toán: " + ex.getMessage(), false);
                hoaDonThanhToan = null;
            }
        });

        paymentDialog.setVisible(true);
    }

    private BigDecimal calculateTotal() {
        return selectedTickets.stream()
                .map(ve -> {
                    if (ve.getGiaVeSauGiam() != null) return ve.getGiaVeSauGiam();
                    if (ve.getGiaVeGoc() != null) return ve.getGiaVeGoc();
                    if (ve.getGiaVe() != null && ve.getGiaVe().getGiaVe() != null) return ve.getGiaVe().getGiaVe();
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void printInvoiceAndTickets() {
        if (selectedTickets.isEmpty() || hoaDonThanhToan == null) {
            SnackbarUtil.showSnackbar(this, "Vui lòng thanh toán vé trước khi in", false);
            return;
        }

        try {
            String hoaDonPath = "HoaDon_" + hoaDonThanhToan.getNgayLap().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
            HoaDonPdfExporter.exportHoaDonToPdf(hoaDonThanhToan, hoaDonPath);

            int idx = 1;
            for (Ve ve : selectedTickets) {
                String vePath = "Ve_" + ve.getMaVe() + "_" + idx + ".pdf";
                VePdfExporter.exportVeToPdf(ve, vePath);
                idx++;
            }

            JOptionPane.showMessageDialog(this, "Đã xuất hóa đơn và vé ra PDF!", "Thành công", JOptionPane.INFORMATION_MESSAGE);

            selectedTickets.clear();
            hoaDonThanhToan = null;
            clearCustomerInfo();
            customerComboBox.setSelectedItem(null);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi in PDF: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}