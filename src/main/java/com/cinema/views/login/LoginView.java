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

import org.mindrot.jbcrypt.BCrypt;

import com.cinema.enums.LoaiTaiKhoan;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.SimpleDocumentListener;
import com.cinema.utils.ValidationUtils;
import com.cinema.views.MainView;
import com.formdev.flatlaf.FlatLightLaf;

public class LoginView extends JFrame {
    private final Connection conn;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel errorLabel;
    private ResourceBundle messages;
    private JButton loginBtn;
    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private int loginAttempts = 0;

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
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi khởi tạo kết nối cơ sở dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lấy kết nối cơ sở dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e);
        }
        initUI();
    }

    private void initUI() {
        setTitle("KSL-CINEMA");
        setSize(600, 800); // Increased size
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main panel with solid color background
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(245, 245, 245));
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

        // Title
        JLabel titleLabel = new JLabel("Đăng nhập", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Inter", Font.BOLD, 32));
        titleLabel.setForeground(new Color(51, 51, 51));
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        contentPanel.add(titleLabel, gbc);

        // Username
        gbc.gridwidth = 1;
        gbc.gridy++;
        JLabel usernameLabel = new JLabel("Tài khoản:");
        usernameLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        contentPanel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        usernameField = createStyledTextField();
        contentPanel.add(usernameField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel passwordLabel = new JLabel("Mật khẩu:");
        passwordLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        contentPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Inter", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
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
        button.setFont(new Font("Inter", Font.BOLD, 16));
        button.setBackground(new Color(0, 102, 204));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField(20);
        field.setFont(new Font("Inter", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        return field;
    }

    private void handleLogin() {
        if (loginAttempts >= MAX_LOGIN_ATTEMPTS) {
            JOptionPane.showMessageDialog(this, messages.getString("accountLocked"));
            return;
        }

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        String validationError = ValidationUtils.validateLoginInput(username, password);
        if (validationError != null) {
            JOptionPane.showMessageDialog(this, validationError);
            return;
        }

        // Kết nối cơ sở dữ liệu và kiểm tra thông tin đăng nhập
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT loaiTaiKhoan, matKhau FROM TaiKhoan WHERE tenDangNhap = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            rs = stmt.executeQuery();

            // Nếu đăng nhập thành công
            if (rs.next()) {
                String storedHash = rs.getString("matKhau");
                String role = rs.getString("loaiTaiKhoan");

                // Kiểm tra mật khẩu với bcrypt
                if (BCrypt.checkpw(password, storedHash)) {
                    JOptionPane.showMessageDialog(this, "Đăng nhập thành công!");

                    // Chuyển hướng dựa trên vai trò
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
                } else {
                    JOptionPane.showMessageDialog(this, "Sai tài khoản hoặc mật khẩu!");
                    loginAttempts++;
                }
            } else {
                JOptionPane.showMessageDialog(this, "Sai tài khoản hoặc mật khẩu!");
                loginAttempts++;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi truy vấn cơ sở dữ liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            // Đóng PreparedStatement và ResultSet
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
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