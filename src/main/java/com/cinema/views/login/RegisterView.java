package com.cinema.views.login;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
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
import java.sql.SQLException;
import java.util.ResourceBundle;

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
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import com.cinema.models.repositories.TaiKhoanRepository;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.SimpleDocumentListener;
import com.cinema.utils.ValidationUtils;
import com.formdev.flatlaf.FlatLightLaf;

public class RegisterView extends JFrame {
    private JTextField usernameField, emailField, fullNameField, phoneField;
    private JPasswordField passwordField, confirmPasswordField;
    private JLabel usernameErrorLabel, emailErrorLabel, fullNameErrorLabel, phoneErrorLabel, passwordErrorLabel, confirmPasswordErrorLabel;
    private JButton registerBtn;
    private TaiKhoanRepository taiKhoanRepository;
    private ResourceBundle messages;

    // Hằng số giao diện
    private static final Color PRIMARY_COLOR = new Color(59, 130, 246); // Xanh dương
    private static final Font LABEL_FONT = new Font("Inter", Font.PLAIN, 14);
    private static final Font TITLE_FONT = new Font("Inter", Font.BOLD, 24);
    private static final Font BUTTON_FONT = new Font("Inter", Font.BOLD, 14);

    public RegisterView() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            System.err.println("Failed to initialize FlatLaf");
        }
        messages = ResourceBundle.getBundle("Messages");
        initRepository();
        initUI();
    }

    private void initRepository() {
        try {
            DatabaseConnection db = new DatabaseConnection();
            taiKhoanRepository = new TaiKhoanRepository(db);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, messages.getString("dbConnectionError"), messages.getString("error"), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void initUI() {
        setTitle(messages.getString("appTitle"));
        setSize(700, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main panel with solid color background (not gradient)
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(245, 245, 245));
        mainPanel.setLayout(new BorderLayout());
        setContentPane(mainPanel);

        // Content panel with white background and shadow
        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw shadow
                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.fillRoundRect(5, 5, getWidth() - 6, getHeight() - 6, 20, 20);

                // Draw panel background
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth() - 5, getHeight() - 5, 20, 20);
            }
        };
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        contentPanel.setLayout(new GridBagLayout());
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Bố cục cho contentPanel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Tiêu đề
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        JLabel titleLabel = new JLabel(messages.getString("registerTitle"), SwingConstants.CENTER);
        titleLabel.setFont(TITLE_FONT);
        contentPanel.add(titleLabel, gbc);

        // Username
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel usernameLabel = new JLabel(messages.getString("usernameLabel"));
        usernameLabel.setFont(LABEL_FONT);
        contentPanel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        usernameField = createStyledTextField();
        contentPanel.add(usernameField, gbc);

        gbc.gridy++;
        usernameErrorLabel = ValidationUtils.createErrorLabel();
        contentPanel.add(usernameErrorLabel, gbc);

        // Full Name
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel fullNameLabel = new JLabel(messages.getString("fullNameLabel"));
        fullNameLabel.setFont(LABEL_FONT);
        contentPanel.add(fullNameLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        fullNameField = createStyledTextField();
        contentPanel.add(fullNameField, gbc);

        gbc.gridy++;
        fullNameErrorLabel = ValidationUtils.createErrorLabel();
        contentPanel.add(fullNameErrorLabel, gbc);

        // Phone
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel phoneLabel = new JLabel(messages.getString("phoneLabel"));
        phoneLabel.setFont(LABEL_FONT);
        contentPanel.add(phoneLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        phoneField = createStyledTextField();
        contentPanel.add(phoneField, gbc);

        gbc.gridy++;
        phoneErrorLabel = ValidationUtils.createErrorLabel();
        contentPanel.add(phoneErrorLabel, gbc);

        // Email
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel emailLabel = new JLabel(messages.getString("emailLabel"));
        emailLabel.setFont(LABEL_FONT);
        contentPanel.add(emailLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        emailField = createStyledTextField();
        contentPanel.add(emailField, gbc);

        gbc.gridy++;
        emailErrorLabel = ValidationUtils.createErrorLabel();
        contentPanel.add(emailErrorLabel, gbc);

        // Password
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel passwordLabel = new JLabel(messages.getString("passwordLabel"));
        passwordLabel.setFont(LABEL_FONT);
        contentPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        passwordField = createStyledPasswordField();
        contentPanel.add(passwordField, gbc);

        gbc.gridy++;
        passwordErrorLabel = ValidationUtils.createErrorLabel();
        contentPanel.add(passwordErrorLabel, gbc);

        // Confirm Password
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel confirmPasswordLabel = new JLabel(messages.getString("confirmPasswordLabel"));
        confirmPasswordLabel.setFont(LABEL_FONT);
        contentPanel.add(confirmPasswordLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        confirmPasswordField = createStyledPasswordField();
        contentPanel.add(confirmPasswordField, gbc);

        gbc.gridy++;
        confirmPasswordErrorLabel = ValidationUtils.createErrorLabel();
        contentPanel.add(confirmPasswordErrorLabel, gbc);

        // Nút đăng ký
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        registerBtn = createStyledButton(messages.getString("registerButton"));
        registerBtn.addActionListener(_ -> handleRegister());
        contentPanel.add(registerBtn, gbc);

        // Liên kết đăng nhập
        gbc.gridy++;
        JLabel loginLabel = new JLabel(messages.getString("loginLink"), SwingConstants.CENTER);
        loginLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        loginLabel.setForeground(PRIMARY_COLOR);
        loginLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                LoginView loginView = new LoginView();
                loginView.setVisible(true);
                dispose();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                loginLabel.setText("<html><u>" + messages.getString("loginLink") + "</u></html>");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                loginLabel.setText(messages.getString("loginLink"));
            }
        });
        contentPanel.add(loginLabel, gbc);

        // Thêm DocumentListener
        addValidationListeners();
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField(20);
        field.setFont(LABEL_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        return field;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField(20);
        field.setFont(LABEL_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        return field;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Inter", Font.BOLD, 16));
        button.setBackground(new Color(0, 102, 204));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void addValidationListeners() {
        usernameField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            validateUsername();
            updateRegisterButtonState();
        }));

        fullNameField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            validateFullName();
            updateRegisterButtonState();
        }));

        phoneField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            validatePhone();
            updateRegisterButtonState();
        }));

        emailField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            validateEmail();
            updateRegisterButtonState();
        }));

        passwordField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            validatePassword();
            validateConfirmPassword();
            updateRegisterButtonState();
        }));

        confirmPasswordField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            validateConfirmPassword();
            updateRegisterButtonState();
        }));
    }

    private void validateUsername() {
        ValidationUtils.validateUsernameField(usernameField, usernameErrorLabel, messages);
    }

    private void validateFullName() {
        ValidationUtils.validateFullNameField(fullNameField, fullNameErrorLabel, messages);
    }

    private void validatePhone() {
        ValidationUtils.validatePhoneField(phoneField, phoneErrorLabel, messages);
    }

    private void validateEmail() {
        ValidationUtils.validateEmailField(emailField, emailErrorLabel, messages);
    }

    private void validatePassword() {
        ValidationUtils.validatePasswordField(passwordField, passwordErrorLabel, messages);
    }

    private void validateConfirmPassword() {
        ValidationUtils.validateConfirmPasswordField(passwordField, confirmPasswordField, confirmPasswordErrorLabel, messages);
    }

    private void updateRegisterButtonState() {
        boolean isValid = !usernameErrorLabel.isVisible() &&
                         !fullNameErrorLabel.isVisible() &&
                         !phoneErrorLabel.isVisible() &&
                         !emailErrorLabel.isVisible() &&
                         !passwordErrorLabel.isVisible() &&
                         !confirmPasswordErrorLabel.isVisible() &&
                         !usernameField.getText().trim().isEmpty() &&
                         !fullNameField.getText().trim().isEmpty() &&
                         !phoneField.getText().trim().isEmpty() &&
                         !emailField.getText().trim().isEmpty() &&
                         passwordField.getPassword().length > 0 &&
                         confirmPasswordField.getPassword().length > 0;
        registerBtn.setEnabled(isValid);
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String confirmPassword = new String(confirmPasswordField.getPassword()).trim();

        String errorMessage = ValidationUtils.validateUserInput(username, fullName, phone, email, password, confirmPassword);
        if (errorMessage != null) {
            JOptionPane.showMessageDialog(this, errorMessage, messages.getString("error"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int maNguoiDung = taiKhoanRepository.registerUser(username, fullName, phone, email, password);
            if (maNguoiDung > 0) {
                JOptionPane.showMessageDialog(this, messages.getString("registerSuccess"), messages.getString("success"), JOptionPane.INFORMATION_MESSAGE);
                LoginView loginView = new LoginView();
                loginView.setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, messages.getString("registerFailed"), messages.getString("error"), JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, messages.getString("dbError") + ex.getMessage(), messages.getString("error"), JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}