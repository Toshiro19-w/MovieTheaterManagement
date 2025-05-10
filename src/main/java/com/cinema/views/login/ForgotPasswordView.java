package com.cinema.views.login;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.mindrot.jbcrypt.BCrypt;

import com.cinema.controllers.TaiKhoanController;
import com.cinema.services.TaiKhoanService;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.SimpleDocumentListener;
import com.cinema.utils.ValidationUtils;

public class ForgotPasswordView extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(ForgotPasswordView.class.getName());
    private final JTextField usernameField = new JTextField(15);
    private final JTextField emailField = new JTextField(15);
    private final JTextField phoneField = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);
    private JLabel usernameErrorLabel, emailErrorLabel, phoneErrorLabel, passwordErrorLabel;
    private JButton submitButton;
    private TaiKhoanController taiKhoanController;
    private Connection conn;
    private ResourceBundle messages;

    public ForgotPasswordView() {
        messages = ResourceBundle.getBundle("Messages");
        initController();
        initUI();
    }

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
        setSize(600, 800); // Increased size
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main panel with solid color background
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(245, 245, 245));
        mainPanel.setLayout(new GridBagLayout());
        setContentPane(mainPanel);

        // Content panel
        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw shadow
                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.fillRoundRect(5, 5, getWidth() - 6, getHeight() - 6, 20, 20);

                // Draw white background
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth() - 5, getHeight() - 5, 20, 20);
            }
        };
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setOpaque(false);
        contentPanel.setPreferredSize(new Dimension(400, 600));

        GridBagConstraints panelGbc = new GridBagConstraints();
        panelGbc.gridx = 0;
        panelGbc.gridy = 0;
        panelGbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(contentPanel, panelGbc);

        GridBagConstraints gbc = createGBC();

        // Tiêu đề
        JLabel titleLabel = new JLabel("Quên Mật Khẩu", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(0, 102, 204));
        contentPanel.add(titleLabel, updateGBC(gbc, 0, 0, 3, GridBagConstraints.CENTER, 0.0));

        // Tên tài khoản
        JLabel usernameLabel = new JLabel("Tên tài khoản:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        contentPanel.add(usernameLabel, updateGBC(gbc, 0, 1, 1, GridBagConstraints.WEST, 0.0));
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        contentPanel.add(usernameField, updateGBC(gbc, 1, 1, 2, GridBagConstraints.HORIZONTAL, 1.0));
        contentPanel.add(usernameErrorLabel = ValidationUtils.createErrorLabel(),
            updateGBC(gbc, 1, 2, 2, GridBagConstraints.HORIZONTAL, 1.0));

        // Email
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        contentPanel.add(emailLabel, updateGBC(gbc, 0, 3, 1, GridBagConstraints.WEST, 0.0));
        emailField.setFont(new Font("Arial", Font.PLAIN, 14));
        contentPanel.add(emailField, updateGBC(gbc, 1, 3, 2, GridBagConstraints.HORIZONTAL, 1.0));
        contentPanel.add(emailErrorLabel = ValidationUtils.createErrorLabel(),
            updateGBC(gbc, 1, 4, 2, GridBagConstraints.HORIZONTAL, 1.0));

        // Số điện thoại
        JLabel phoneLabel = new JLabel("Số điện thoại:");
        phoneLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        contentPanel.add(phoneLabel, updateGBC(gbc, 0, 5, 1, GridBagConstraints.WEST, 0.0));
        phoneField.setFont(new Font("Arial", Font.PLAIN, 14));
        contentPanel.add(phoneField, updateGBC(gbc, 1, 5, 2, GridBagConstraints.HORIZONTAL, 1.0));
        contentPanel.add(phoneErrorLabel = ValidationUtils.createErrorLabel(),
            updateGBC(gbc, 1, 6, 2, GridBagConstraints.HORIZONTAL, 1.0));

        // Mật khẩu mới
        JLabel passwordLabel = new JLabel("Mật khẩu mới:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        contentPanel.add(passwordLabel, updateGBC(gbc, 0, 7, 1, GridBagConstraints.WEST, 0.0));
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        contentPanel.add(passwordField, updateGBC(gbc, 1, 7, 2, GridBagConstraints.HORIZONTAL, 1.0));
        contentPanel.add(passwordErrorLabel = ValidationUtils.createErrorLabel(),
            updateGBC(gbc, 1, 8, 2, GridBagConstraints.HORIZONTAL, 1.0));

        // Nút Gửi Yêu Cầu
        submitButton = new JButton("Gửi Yêu Cầu");
        submitButton.setFont(new Font("Arial", Font.BOLD, 14));
        submitButton.setBackground(new Color(0, 102, 204));
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);
        submitButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        submitButton.addActionListener(_ -> handlePasswordReset());
        contentPanel.add(submitButton, updateGBC(gbc, 1, 9, 1, GridBagConstraints.NONE, 0.0));

        // Nút Quay Lại
        JButton backButton = new JButton("Quay Lại");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setBackground(new Color(0, 102, 204));
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        backButton.addActionListener(_ -> {
            new LoginView().setVisible(true);
            dispose();
        });
        contentPanel.add(backButton, updateGBC(gbc, 2, 9, 1, GridBagConstraints.NONE, 0.0));

        // Icon cho frame
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/images/Icon/nen1.jpg")));
        setIconImage(icon.getImage());

        // Add validation listeners
        addValidationListeners();
    }

    private GridBagConstraints createGBC() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Giảm khoảng cách để bố cục chặt chẽ hơn
        gbc.anchor = GridBagConstraints.CENTER;
        return gbc;
    }

    private GridBagConstraints updateGBC(GridBagConstraints gbc, int x, int y, int width, int fill, double weightx) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.fill = fill;
        gbc.weightx = weightx;
        gbc.weighty = 0.1;
        return gbc;
    }

    private void addValidationListeners() {
        usernameField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            ValidationUtils.validateUsernameField(usernameField, usernameErrorLabel, messages);
            updateSubmitButtonState();
        }));

        emailField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            ValidationUtils.validateEmailField(emailField, emailErrorLabel, messages);
            updateSubmitButtonState();
        }));

        phoneField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            ValidationUtils.validatePhoneField(phoneField, phoneErrorLabel, messages);
            updateSubmitButtonState();
        }));

        passwordField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            ValidationUtils.validatePasswordField(passwordField, passwordErrorLabel, messages);
            updateSubmitButtonState();
        }));
    }

    private void updateSubmitButtonState() {
        boolean isValid = !usernameErrorLabel.isVisible() &&
                         !emailErrorLabel.isVisible() &&
                         !phoneErrorLabel.isVisible() &&
                         !passwordErrorLabel.isVisible() &&
                         !usernameField.getText().trim().isEmpty() &&
                         !emailField.getText().trim().isEmpty() &&
                         !phoneField.getText().trim().isEmpty() &&
                         new String(passwordField.getPassword()).trim().length() > 0;
        submitButton.setEnabled(isValid);
    }

    // Xử lý quên mật khẩu
    private void handlePasswordReset() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        String validationError = ValidationUtils.validateForgotPasswordInput(username, email, phone, password);
        if (validationError != null) {
            showError(validationError, false);
            return;
        }

        try {
            boolean isValidUser = taiKhoanController.verifyUserForPasswordReset(username, email, phone);
            if (!isValidUser) {
                showError("Thông tin tài khoản, email hoặc số điện thoại không đúng!", false);
                return;
            }

            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            boolean isUpdated = taiKhoanController.updatePassword(username, hashedPassword);

            if (isUpdated) {
                JOptionPane.showMessageDialog(this, "Mật khẩu đã được cập nhật! Vui lòng đăng nhập.",
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