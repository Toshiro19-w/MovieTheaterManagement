package com.cinema.views.login;

import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.mindrot.jbcrypt.BCrypt;

import com.cinema.components.UIConstants;
import com.cinema.controllers.TaiKhoanController;
import com.cinema.services.TaiKhoanService;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.SimpleDocumentListener;
import com.cinema.utils.ValidationUtils;

public class ForgotPasswordView extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(ForgotPasswordView.class.getName());
    private final JTextField emailOrPhoneField = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);
    private final JPasswordField confirmPasswordField = new JPasswordField(15);

    private JLabel emailOrPhoneErrorLabel, passwordErrorLabel, confirmPasswordErrorLabel;
    private JButton resetPasswordButton;
    private TaiKhoanController taiKhoanController;
    private Connection conn;
    private ResourceBundle messages;

    private String foundUsername;

    public ForgotPasswordView() {
        messages = ResourceBundle.getBundle("Messages");
        initController();
        initUI();
    }

    @SuppressWarnings("LoggerStringConcat")
    private void initController() {
        try {
            DatabaseConnection databaseConnection = new DatabaseConnection();
            conn = databaseConnection.getConnection();
            taiKhoanController = new TaiKhoanController(new TaiKhoanService(databaseConnection));
        } catch (IOException e) {
            LOGGER.severe("Cannot read database configuration: " + e.getMessage());
            showError("Không thể đọc cấu hình cơ sở dữ liệu: " + e.getMessage(), true);
        } catch (SQLException e) {
            LOGGER.severe("Cannot connect to database: " + e.getMessage());
            showError("Không thể kết nối cơ sở dữ liệu: " + e.getMessage(), true);
        } catch (Exception e) {
            LOGGER.severe("Initialization error: " + e.getMessage());
            showError("Lỗi khởi tạo kết nối: " + e.getMessage(), true);
        }
    }

    private void initUI() {
        setTitle("Quên Mật Khẩu");
        setSize(480, 420);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(245, 245, 245));
        mainPanel.setBorder(new EmptyBorder(24, 24, 24, 24));
        contentPane.add(mainPanel, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Title
        JLabel titleLabel = new JLabel("Quên Mật Khẩu", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel.setForeground(new Color(0, 102, 204));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        // Email/SĐT
        gbc.gridy++;
        gbc.gridwidth = 1;
        JLabel emailOrPhoneLabel = new JLabel("Email hoặc số điện thoại:");
        emailOrPhoneLabel.setFont(UIConstants.LABEL_FONT);
        mainPanel.add(emailOrPhoneLabel, gbc);

        gbc.gridx = 1;
        emailOrPhoneField.setFont(UIConstants.LABEL_FONT);
        mainPanel.add(emailOrPhoneField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        emailOrPhoneErrorLabel = ValidationUtils.createErrorLabel();
        mainPanel.add(emailOrPhoneErrorLabel, gbc);

        // Password
        gbc.gridy++;
        gbc.gridwidth = 1;
        JLabel passwordLabel = new JLabel("Mật khẩu mới:");
        passwordLabel.setFont(UIConstants.LABEL_FONT);
        mainPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        mainPanel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        passwordErrorLabel = ValidationUtils.createErrorLabel();
        mainPanel.add(passwordErrorLabel, gbc);

        // Confirm Password
        gbc.gridy++;
        gbc.gridwidth = 1;
        JLabel confirmPasswordLabel = new JLabel("Xác nhận mật khẩu:");
        confirmPasswordLabel.setFont(UIConstants.LABEL_FONT);
        mainPanel.add(confirmPasswordLabel, gbc);

        gbc.gridx = 1;
        confirmPasswordField.setFont(new Font("Arial", Font.PLAIN, 14));
        mainPanel.add(confirmPasswordField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        confirmPasswordErrorLabel = ValidationUtils.createErrorLabel();
        mainPanel.add(confirmPasswordErrorLabel, gbc);

        // Reset Button
        gbc.gridy++;
        gbc.gridwidth = 2;
        resetPasswordButton = new JButton("Đặt lại mật khẩu");
        resetPasswordButton.setFont(UIConstants.BUTTON_FONT);
        resetPasswordButton.setBackground(UIConstants.BUTTON_COLOR);
        resetPasswordButton.setForeground(UIConstants.BUTTON_TEXT_COLOR);
        resetPasswordButton.setFocusPainted(false);
        resetPasswordButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        mainPanel.add(resetPasswordButton, gbc);

        // Back Button
        gbc.gridy++;
        JButton backButton = new JButton("Quay Lại");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setBackground(new Color(0, 102, 204));
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        mainPanel.add(backButton, gbc);

        // Listeners
        emailOrPhoneField.getDocument().addDocumentListener(new SimpleDocumentListener(this::validateEmailOrPhone));
        passwordField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            ValidationUtils.validatePasswordField(passwordField, passwordErrorLabel, messages);
            validateConfirmPassword();
            updateResetButtonState();
        }));
        confirmPasswordField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            validateConfirmPassword();
            updateResetButtonState();
        }));

        resetPasswordButton.addActionListener(e -> handleResetPassword());
        backButton.addActionListener(e -> {
            new LoginView().setVisible(true);
            dispose();
        });
    }

    private void validateEmailOrPhone() {
        String input = emailOrPhoneField.getText().trim();
        if (input.isEmpty()) {
            emailOrPhoneErrorLabel.setText("Vui lòng nhập email hoặc số điện thoại");
            emailOrPhoneErrorLabel.setVisible(true);
            return;
        }
        if (input.contains("@")) {
            if (!ValidationUtils.isValidEmail(input)) {
                emailOrPhoneErrorLabel.setText("Email không hợp lệ");
                emailOrPhoneErrorLabel.setVisible(true);
                return;
            }
        } else {
            if (!ValidationUtils.isValidPhoneNumber(input)) {
                emailOrPhoneErrorLabel.setText("Số điện thoại không hợp lệ");
                emailOrPhoneErrorLabel.setVisible(true);
                return;
            }
        }
        emailOrPhoneErrorLabel.setVisible(false);
    }

    private void validateConfirmPassword() {
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        if (confirmPassword.isEmpty()) {
            confirmPasswordErrorLabel.setText("Vui lòng xác nhận mật khẩu");
            confirmPasswordErrorLabel.setVisible(true);
            return;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordErrorLabel.setText("Mật khẩu xác nhận không khớp");
            confirmPasswordErrorLabel.setVisible(true);
        } else {
            confirmPasswordErrorLabel.setVisible(false);
        }
    }

    private void updateResetButtonState() {
        boolean isValid = !emailOrPhoneErrorLabel.isVisible()
                && !passwordErrorLabel.isVisible()
                && !confirmPasswordErrorLabel.isVisible()
                && !emailOrPhoneField.getText().trim().isEmpty()
                && passwordField.getPassword().length > 0
                && confirmPasswordField.getPassword().length > 0;
        resetPasswordButton.setEnabled(isValid);
    }

    private void handleResetPassword() {
        String input = emailOrPhoneField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String confirmPassword = new String(confirmPasswordField.getPassword()).trim();

        if (input.isEmpty()) {
            showError("Vui lòng nhập email hoặc số điện thoại", false);
            return;
        }
        if (!password.equals(confirmPassword)) {
            showError("Mật khẩu xác nhận không khớp!", false);
            return;
        }

        try {
            foundUsername = taiKhoanController.findUsernameByEmailOrPhone(input);
            if (foundUsername == null) {
                showError("Không tìm thấy tài khoản nào liên kết với thông tin này", false);
                return;
            }
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            boolean isUpdated = taiKhoanController.updatePassword(foundUsername, hashedPassword);
            if (isUpdated) {
                JOptionPane.showMessageDialog(this, "Mật khẩu đã được cập nhật thành công! Vui lòng đăng nhập.",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                new LoginView().setVisible(true);
                dispose();
            } else {
                showError("Không thể cập nhật mật khẩu!", false);
            }
        } catch (SQLException e) {
            showError("Lỗi cơ sở dữ liệu: " + e.getMessage(), false);
        }
    }

    @SuppressWarnings("LoggerStringConcat")
    private void showError(String msg, boolean exitAfter) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
        if (exitAfter) {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                LOGGER.severe("Error closing connection: " + e.getMessage());
            }
            System.exit(1);
        }
    }
}