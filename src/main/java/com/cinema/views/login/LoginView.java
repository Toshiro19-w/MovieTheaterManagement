package com.cinema.views.login;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.cinema.components.UIConstants;
import com.cinema.controllers.ActivityLogController;
import com.cinema.controllers.TaiKhoanController;
import com.cinema.enums.LoaiTaiKhoan;
import com.cinema.services.TaiKhoanService;
import com.cinema.utils.AppIconUtils;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.PasswordHasher;
import com.cinema.utils.SimpleDocumentListener;
import com.cinema.utils.ValidationUtils;
import com.cinema.views.MainView;
import com.formdev.flatlaf.FlatLightLaf;

public class LoginView extends JFrame {
    private final Connection conn;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel errorLabel, titleLabel;
    private ResourceBundle messages;
    private JButton loginBtn;
    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private int loginAttempts = 0;
    private static final Logger LOGGER = Logger.getLogger(LoginView.class.getName());
    private final TaiKhoanController taiKhoanController;

    @SuppressWarnings("UseSpecificCatch")
    public LoginView() {
        messages = ResourceBundle.getBundle("Messages");
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }

        try {
            DatabaseConnection databaseConnection = new DatabaseConnection();
            conn = databaseConnection.getConnection();
            taiKhoanController = new TaiKhoanController(new TaiKhoanService(databaseConnection));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi khởi tạo kết nối cơ sở dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lấy kết nối cơ sở dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e);
        }
        
        // Đặt biểu tượng cho cửa sổ ứng dụng
        AppIconUtils.setAppIcon(this);
        
        initUI();
    }

    private void initUI() {
        setTitle("CinemaHub");
        setSize(700, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main panel with solid color background
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        mainPanel.setLayout(new BorderLayout());
        setContentPane(mainPanel);

        // Login panel
        JPanel loginPanel = new JPanel();
        loginPanel.setOpaque(false);
        loginPanel.setBorder(new EmptyBorder(40, 40, 40, 40));
        loginPanel.setLayout(new GridBagLayout());

        // Content panel with white background and shadow effect
        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(UIConstants.CARD_BACKGROUND);
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, UIConstants.BORDER_RADIUS, UIConstants.BORDER_RADIUS);
                g2d.setColor(UIConstants.SHADOW_COLOR);
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, UIConstants.BORDER_RADIUS, UIConstants.BORDER_RADIUS);
                g2d.dispose();
            }
        };
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setPreferredSize(new Dimension(450, 500));

        // Add panels to frame
        loginPanel.add(contentPanel, new GridBagConstraints());
        mainPanel.add(loginPanel, BorderLayout.CENTER);

        // Rest of UI components with updated styling
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Logo
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        JLabel logoLabel = AppIconUtils.getAppLogo(60, 60);
        contentPanel.add(logoLabel, gbc);       

        // Title
        gbc.gridy++;
        titleLabel = new JLabel(messages.getString("loginTitle"), SwingConstants.CENTER);
        titleLabel.setFont(UIConstants.TITLE_FONT);
        titleLabel.setForeground(UIConstants.TEXT_COLOR);
        contentPanel.add(titleLabel, gbc);

        // Username
        gbc.gridwidth = 1;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel usernameLabel = new JLabel(messages.getString("usernameLabel"));
        usernameLabel.setFont(UIConstants.LABEL_FONT);
        contentPanel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        usernameField = createStyledTextField();
        contentPanel.add(usernameField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel passwordLabel = new JLabel("Mật khẩu:");
        passwordLabel.setFont(UIConstants.LABEL_FONT);
        contentPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        passwordField.setFont(UIConstants.LABEL_FONT);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        // Thiết lập kích thước tối thiểu và ưu tiên giống với TextField
        Dimension size = new Dimension(300, 40);
        passwordField.setPreferredSize(size);
        passwordField.setMinimumSize(size);
        contentPanel.add(passwordField, gbc);

        // Error label
        gbc.gridy++;
        errorLabel = ValidationUtils.createErrorLabel();
        contentPanel.add(errorLabel, gbc);

        // Login button
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        loginBtn = createStyledButton("Đăng nhập");
        loginBtn.addActionListener(_ -> handleLogin());
        contentPanel.add(loginBtn, gbc);

        // Forgot password link
        gbc.gridy++;
        JLabel forgotLabel = new JLabel("Quên mật khẩu?", SwingConstants.CENTER);
        forgotLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        forgotLabel.setForeground(new Color(0, 102, 204));
        forgotLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgotLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleForgotPassword();
            }
        });
        contentPanel.add(forgotLabel, gbc);

        // Register link
        gbc.gridy++;
        JLabel registerLabel = new JLabel("Không có tài khoản? Đăng ký", SwingConstants.CENTER);
        registerLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        registerLabel.setForeground(new Color(0, 102, 204));
        registerLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleRegister();
            }
        });
        contentPanel.add(registerLabel, gbc);

        // Add document listeners for real-time validation
        usernameField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            ValidationUtils.validateLoginFields(usernameField, passwordField, errorLabel, messages);
            updateLoginButtonState();
        }));

        passwordField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            ValidationUtils.validateLoginFields(usernameField, passwordField, errorLabel, messages);
            updateLoginButtonState();
        }));
    }

    private void updateLoginButtonState() {
        loginBtn.setEnabled(!errorLabel.isVisible() && 
                          !usernameField.getText().trim().isEmpty() && 
                          passwordField.getPassword().length > 0);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(UIConstants.BUTTON_FONT);
        button.setBackground(UIConstants.BUTTON_COLOR);
        button.setForeground(UIConstants.BUTTON_TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField(20);
        field.setFont(UIConstants.LABEL_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Thiết lập kích thước tối thiểu và ưu tiên
        Dimension size = new Dimension(300, 40);
        field.setPreferredSize(size);
        field.setMinimumSize(size);
        
        return field;
    }

    private void handleLogin() {
        if (loginAttempts >= MAX_LOGIN_ATTEMPTS) {
            JOptionPane.showMessageDialog(this, messages.getString("accountLocked"));
            return;
        }

        String username = usernameField.getText().trim();
        String plainPassword = new String(passwordField.getPassword());

        String validationError = ValidationUtils.validateLoginInput(username, plainPassword);
        if (validationError != null) {
            JOptionPane.showMessageDialog(this, validationError);
            return;
        }

        try (PreparedStatement stmt = conn.prepareStatement("SELECT loaiTaiKhoan, matKhau FROM TaiKhoan WHERE tenDangNhap = ?")) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("matKhau");
                    String role = rs.getString("loaiTaiKhoan");

                    // Sử dụng PasswordHasher thay vì BCrypt trực tiếp
                    if (PasswordHasher.verifyPassword(plainPassword, storedHash)) {
                        LOGGER.info("Đăng nhập thành công cho người dùng: " + username);
                        handleSuccessfulLogin(username, role);
                    } else {
                        handleFailedLogin();
                    }
                } else {
                    handleFailedLogin();
                }
            }
        } catch (SQLException | IOException ex) {
            LOGGER.severe("Lỗi đăng nhập: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi truy vấn CSDL: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleSuccessfulLogin(String username, String role) throws IOException, SQLException {
        // Lấy mã người dùng từ tên đăng nhập
        int maNguoiDung = taiKhoanController.getUserIdFromUsername(username);
        // Hiển thị thông báo đăng nhập thành công
        JOptionPane.showMessageDialog(this, messages.getString("loginSuccess"),
                "Đăng nhập thành công", JOptionPane.INFORMATION_MESSAGE);
        // Khởi tạo phiên làm việc đồng bộ
        if (maNguoiDung > 0) {
            boolean isNhanVien = !role.equalsIgnoreCase("user");
            com.cinema.App.getSyncController().initializeSession(maNguoiDung, isNhanVien);
            
            // Thêm log đăng nhập
            try {
                ActivityLogController activityLogController = new ActivityLogController();
                activityLogController.addLog("Đăng nhập", "Đăng nhập vào hệ thống", maNguoiDung);
            } catch (Exception e) {
                LOGGER.warning("Không thể ghi log đăng nhập: " + e.getMessage());
            }
        }
        
        switch (role.toLowerCase()) {
            case "admin":
                openMainView(username, LoaiTaiKhoan.ADMIN);
                break;
            case "quanlyphim":
                openMainView(username, LoaiTaiKhoan.QUANLYPHIM);
                break;
            case "thungan":
                openMainView(username, LoaiTaiKhoan.THUNGAN);
                break;
            case "banve":
                openMainView(username, LoaiTaiKhoan.BANVE);
                break;
            case "user":
            default:
                openMainView(username, LoaiTaiKhoan.USER);
                break;
        }
        dispose(); // Đóng cửa sổ đăng nhập
    }

    private void handleFailedLogin() {
        JOptionPane.showMessageDialog(this, "Sai tài khoản hoặc mật khẩu!");
        loginAttempts++;
    }

    private void openMainView(String username, LoaiTaiKhoan loaiTaiKhoan) throws IOException, SQLException {
        MainView mainView = new MainView(username, loaiTaiKhoan);
        mainView.setVisible(true);
    }

    private void handleRegister() {
        RegisterView registerView = new RegisterView();
        registerView.setVisible(true);
        dispose();
    }

    private void handleForgotPassword() {
        ForgotPasswordView forgotPasswordView = new ForgotPasswordView();
        forgotPasswordView.setVisible(true);
        dispose();
    }
}