package com.cinema.views.login;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
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
import javax.swing.Timer;

import org.mindrot.jbcrypt.BCrypt;

import com.cinema.components.UIConstants;
import com.cinema.controllers.TaiKhoanController;
import com.cinema.services.TaiKhoanService;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.GmailSender;
import com.cinema.utils.SimpleDocumentListener;
import com.cinema.utils.ValidationUtils;

public class ForgotPasswordView extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(ForgotPasswordView.class.getName());
    private final JTextField emailOrPhoneField = new JTextField(15);
    private final JTextField verificationCodeField = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);
    private final JPasswordField confirmPasswordField = new JPasswordField(15);
    
    private JLabel emailOrPhoneErrorLabel, verificationCodeErrorLabel, passwordErrorLabel, confirmPasswordErrorLabel;
    private JButton submitButton, sendCodeButton, resetPasswordButton;
    private TaiKhoanController taiKhoanController;
    private Connection conn;
    private ResourceBundle messages;
    private String verificationCode;
    private boolean isCodeVerified = false;
    private Timer resendTimer;
    private int countdown = 60;
    
    // Để lưu thông tin tài khoản sau khi xác minh
    private String foundUsername;
    private String userEmail;
    
    // Các panel cho từng bước
    private JPanel step1Panel; // Nhập email/số điện thoại
    private JPanel step2Panel; // Nhập mã xác nhận
    private JPanel step3Panel; // Đặt mật khẩu mới
    
    // Trạng thái hiện tại
    private int currentStep = 1;

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
        setSize(700, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Tạo các panel cho từng bước
        createStep1Panel();
        createStep2Panel();
        createStep3Panel();
        
        // Hiển thị bước 1 đầu tiên
        switchToStep(1);
    }
    
    private void createStep1Panel() {
        step1Panel = new JPanel(new GridBagLayout());
        step1Panel.setOpaque(false);
        
        GridBagConstraints gbc = createGBC();
        
        // Email hoặc số điện thoại
        JLabel emailOrPhoneLabel = new JLabel("Email hoặc số điện thoại:");
        emailOrPhoneLabel.setFont(UIConstants.LABEL_FONT);
        step1Panel.add(emailOrPhoneLabel, updateGBC(gbc, 0, 0, 1, GridBagConstraints.WEST, 0.0));
        
        emailOrPhoneField.setFont(UIConstants.LABEL_FONT);
        step1Panel.add(emailOrPhoneField, updateGBC(gbc, 0, 1, 1, GridBagConstraints.HORIZONTAL, 1.0));
        
        emailOrPhoneErrorLabel = ValidationUtils.createErrorLabel();
        step1Panel.add(emailOrPhoneErrorLabel, updateGBC(gbc, 0, 2, 1, GridBagConstraints.HORIZONTAL, 1.0));
        
        // Nút tiếp tục
        JButton continueButton = new JButton("Tiếp tục");
        continueButton.setFont(UIConstants.BUTTON_FONT);
        continueButton.setBackground(UIConstants.BUTTON_COLOR);
        continueButton.setForeground(UIConstants.BUTTON_TEXT_COLOR);
        continueButton.setFocusPainted(false);
        continueButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        continueButton.addActionListener(_ -> verifyEmailOrPhone());
        step1Panel.add(continueButton, updateGBC(gbc, 0, 3, 1, GridBagConstraints.CENTER, 0.0));
        
        // Thêm listener cho validation
        emailOrPhoneField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            validateEmailOrPhone();
        }));
    }
    
    private void createStep2Panel() {
        step2Panel = new JPanel(new GridBagLayout());
        step2Panel.setOpaque(false);
        
        GridBagConstraints gbc = createGBC();
        
        // Thông báo
        JLabel infoLabel = new JLabel("Mã xác nhận đã được gửi đến email của bạn");
        infoLabel.setFont(UIConstants.LABEL_FONT);
        step2Panel.add(infoLabel, updateGBC(gbc, 0, 0, 2, GridBagConstraints.WEST, 0.0));
        
        // Mã xác nhận
        JLabel verificationCodeLabel = new JLabel("Mã xác nhận:");
        verificationCodeLabel.setFont(UIConstants.LABEL_FONT);
        step2Panel.add(verificationCodeLabel, updateGBC(gbc, 0, 1, 1, GridBagConstraints.WEST, 0.0));
        
        verificationCodeField.setFont(UIConstants.LABEL_FONT);
        step2Panel.add(verificationCodeField, updateGBC(gbc, 0, 2, 1, GridBagConstraints.HORIZONTAL, 1.0));
        
        // Nút gửi lại
        sendCodeButton = new JButton("Gửi lại");
        sendCodeButton.setFont(UIConstants.BUTTON_FONT);
        sendCodeButton.setBackground(UIConstants.BUTTON_COLOR);
        sendCodeButton.setForeground(UIConstants.BUTTON_TEXT_COLOR);
        sendCodeButton.setFocusPainted(false);
        sendCodeButton.addActionListener(_ -> sendVerificationCode());
        step2Panel.add(sendCodeButton, updateGBC(gbc, 1, 2, 1, GridBagConstraints.WEST, 0.0));
        
        verificationCodeErrorLabel = ValidationUtils.createErrorLabel();
        step2Panel.add(verificationCodeErrorLabel, updateGBC(gbc, 0, 3, 2, GridBagConstraints.HORIZONTAL, 1.0));
    }
    
    // Thêm biến instance để lưu trữ label hiển thị tên đăng nhập
    private JLabel usernameValueLabel;
    
    private void createStep3Panel() {
        step3Panel = new JPanel(new GridBagLayout());
        step3Panel.setOpaque(false);
        
        GridBagConstraints gbc = createGBC();
        
        // Thông báo
        JLabel infoLabel = new JLabel("Đặt mật khẩu mới cho tài khoản");
        infoLabel.setFont(UIConstants.LABEL_FONT);
        step3Panel.add(infoLabel, updateGBC(gbc, 0, 0, 2, GridBagConstraints.WEST, 0.0));
        
        // Tên đăng nhập
        JLabel usernameLabel = new JLabel("Tên đăng nhập:");
        usernameLabel.setFont(UIConstants.LABEL_FONT);
        step3Panel.add(usernameLabel, updateGBC(gbc, 0, 1, 1, GridBagConstraints.WEST, 0.0));
        
        // Label hiển thị tên đăng nhập - sẽ được cập nhật sau
        usernameValueLabel = new JLabel("");
        usernameValueLabel.setFont(UIConstants.LABEL_FONT.deriveFont(Font.BOLD));
        step3Panel.add(usernameValueLabel, updateGBC(gbc, 1, 1, 1, GridBagConstraints.WEST, 1.0));
        
        // Mật khẩu mới
        JLabel passwordLabel = new JLabel("Mật khẩu mới:");
        passwordLabel.setFont(UIConstants.LABEL_FONT);
        step3Panel.add(passwordLabel, updateGBC(gbc, 0, 2, 1, GridBagConstraints.WEST, 0.0));
        
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        step3Panel.add(passwordField, updateGBC(gbc, 1, 2, 1, GridBagConstraints.HORIZONTAL, 1.0));
        
        passwordErrorLabel = ValidationUtils.createErrorLabel();
        step3Panel.add(passwordErrorLabel, updateGBC(gbc, 0, 3, 2, GridBagConstraints.HORIZONTAL, 1.0));
        
        // Xác nhận mật khẩu
        JLabel confirmPasswordLabel = new JLabel("Xác nhận mật khẩu:");
        confirmPasswordLabel.setFont(UIConstants.LABEL_FONT);
        step3Panel.add(confirmPasswordLabel, updateGBC(gbc, 0, 4, 1, GridBagConstraints.WEST, 0.0));
        
        confirmPasswordField.setFont(new Font("Arial", Font.PLAIN, 14));
        step3Panel.add(confirmPasswordField, updateGBC(gbc, 1, 4, 1, GridBagConstraints.HORIZONTAL, 1.0));
        
        confirmPasswordErrorLabel = ValidationUtils.createErrorLabel();
        step3Panel.add(confirmPasswordErrorLabel, updateGBC(gbc, 0, 5, 2, GridBagConstraints.HORIZONTAL, 1.0));
        
        // Nút đặt lại mật khẩu
        resetPasswordButton = new JButton("Đặt lại mật khẩu");
        resetPasswordButton.setFont(UIConstants.BUTTON_FONT);
        resetPasswordButton.setBackground(UIConstants.BUTTON_COLOR);
        resetPasswordButton.setForeground(UIConstants.BUTTON_TEXT_COLOR);
        resetPasswordButton.setFocusPainted(false);
        resetPasswordButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        resetPasswordButton.addActionListener(_ -> handlePasswordReset());
        step3Panel.add(resetPasswordButton, updateGBC(gbc, 0, 6, 2, GridBagConstraints.CENTER, 0.0));
        
        // Thêm listener cho validation
        passwordField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            ValidationUtils.validatePasswordField(passwordField, passwordErrorLabel, messages);
            validateConfirmPassword();
            updateResetButtonState();
        }));
        
        confirmPasswordField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            validateConfirmPassword();
            updateResetButtonState();
        }));
    }

    private GridBagConstraints createGBC() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
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
    
    private void validateEmailOrPhone() {
        String input = emailOrPhoneField.getText().trim();
        
        if (input.isEmpty()) {
            emailOrPhoneErrorLabel.setText("Vui lòng nhập email hoặc số điện thoại");
            emailOrPhoneErrorLabel.setVisible(true);
            return;
        }
        
        // Kiểm tra nếu là email
        if (input.contains("@")) {
            if (!ValidationUtils.isValidEmail(input)) {
                emailOrPhoneErrorLabel.setText("Email không hợp lệ");
                emailOrPhoneErrorLabel.setVisible(true);
                return;
            }
        } 
        // Kiểm tra nếu là số điện thoại
        else {
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
    
    private void validateVerificationCode() {
        String code = verificationCodeField.getText().trim();
        
        if (code.isEmpty()) {
            verificationCodeErrorLabel.setText("Vui lòng nhập mã xác nhận");
            verificationCodeErrorLabel.setVisible(true);
            isCodeVerified = false;
            return;
        }
        
        if (verificationCode != null && code.equals(verificationCode)) {
            verificationCodeErrorLabel.setVisible(false);
            isCodeVerified = true;
        } else {
            verificationCodeErrorLabel.setText("Mã xác nhận không đúng");
            verificationCodeErrorLabel.setVisible(true);
            isCodeVerified = false;
        }
    }
    
    private void updateResetButtonState() {
        boolean isValid = !passwordErrorLabel.isVisible() &&
                         !confirmPasswordErrorLabel.isVisible() &&
                         new String(passwordField.getPassword()).trim().length() > 0 &&
                         new String(confirmPasswordField.getPassword()).trim().length() > 0;
        resetPasswordButton.setEnabled(isValid);
    }
    
    private void verifyEmailOrPhone() {
        String input = emailOrPhoneField.getText().trim();
        
        if (input.isEmpty()) {
            showError("Vui lòng nhập email hoặc số điện thoại", false);
            return;
        }
        
        try {
            // Kiểm tra xem email hoặc số điện thoại có tồn tại trong hệ thống không
            foundUsername = taiKhoanController.findUsernameByEmailOrPhone(input);
            
            if (foundUsername != null) {
                // Lấy email của tài khoản để gửi mã xác nhận
                userEmail = taiKhoanController.getEmailByUsername(foundUsername);
                
                if (userEmail != null) {
                    // Chuyển sang bước 2
                    switchToStep(2);
                    
                    // Gửi mã xác nhận
                    sendVerificationCode();
                } else {
                    showError("Không thể lấy thông tin email của tài khoản", false);
                }
            } else {
                showError("Không tìm thấy tài khoản nào liên kết với thông tin này", false);
            }
        } catch (SQLException e) {
            showError("Lỗi cơ sở dữ liệu: " + e.getMessage(), false);
        }
    }
    
    private void sendVerificationCode() {
        try {
            // Sử dụng VerificationCodeGenerator để tạo mã xác nhận
            verificationCode = com.cinema.utils.VerificationCodeGenerator.generateCode();
            
            // Sử dụng GmailSender thay vì EmailSender
            boolean emailSent = GmailSender.sendVerificationCode(userEmail, foundUsername, verificationCode);
            
            if (emailSent) {
                JOptionPane.showMessageDialog(this, 
                    "Mã xác nhận đã được gửi đến email " + maskEmail(userEmail), 
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                
                startResendTimer();
            } else {
                showError("Không thể gửi email. Vui lòng thử lại sau.", false);
            }
            
        } catch (Exception e) {
            showError("Lỗi gửi mã xác nhận: " + e.getMessage(), false);
        }
    }
    
    private void verifyCode() {
        String code = verificationCodeField.getText().trim();
        
        if (code.isEmpty()) {
            showError("Vui lòng nhập mã xác nhận", false);
            return;
        }
        
        if (verificationCode != null && code.equals(verificationCode)) {
            // Chuyển sang bước 3
            switchToStep(3);
        } else {
            showError("Mã xác nhận không đúng", false);
        }
    }
    
    private void switchToStep(int step) {
        currentStep = step;
        
        // Xóa tất cả các component hiện tại
        Container contentPane = getContentPane();
        contentPane.removeAll();
        
        // Tạo main panel với GridBagLayout
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(245, 245, 245));
        mainPanel.setLayout(new GridBagLayout());
        
        // Tạo content panel
        JPanel contentPanel = new JPanel() {
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
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setOpaque(false);
        contentPanel.setPreferredSize(new Dimension(450, 500));
        
        // Thêm content panel vào main panel
        GridBagConstraints panelGbc = new GridBagConstraints();
        panelGbc.gridx = 0;
        panelGbc.gridy = 0;
        panelGbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(contentPanel, panelGbc);
        
        // Thêm tiêu đề
        JLabel titleLabel = new JLabel("Quên Mật Khẩu", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(0, 102, 204));
        
        GridBagConstraints gbc = createGBC();
        contentPanel.add(titleLabel, updateGBC(gbc, 0, 0, 3, GridBagConstraints.CENTER, 0.0));
        
        // Thêm panel tương ứng với bước hiện tại
        switch (step) {
            case 1 -> contentPanel.add(step1Panel, updateGBC(gbc, 0, 1, 3, GridBagConstraints.HORIZONTAL, 1.0));
            case 2 -> contentPanel.add(step2Panel, updateGBC(gbc, 0, 1, 3, GridBagConstraints.HORIZONTAL, 1.0));
            case 3 -> {
                // Cập nhật tên đăng nhập trực tiếp thông qua biến instance
                if (usernameValueLabel != null) {
                    usernameValueLabel.setText(foundUsername);
                    System.out.println("Đã cập nhật tên đăng nhập: " + foundUsername); // Debug
                }
                contentPanel.add(step3Panel, updateGBC(gbc, 0, 1, 3, GridBagConstraints.HORIZONTAL, 1.0));
                break;
            }
        }
        
        // Thêm nút quay lại
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
        contentPanel.add(backButton, updateGBC(gbc, 0, 2, 3, GridBagConstraints.CENTER, 0.0));
        
        // Thêm main panel vào content pane (chỉ định vị trí CENTER cho BorderLayout)
        contentPane.add(mainPanel, BorderLayout.CENTER);
        
        contentPane.revalidate();
        contentPane.repaint();
    }
    

    private String maskEmail(String email) {
        if (email == null || email.isEmpty() || !email.contains("@")) {
            return email;
        }
        
        String[] parts = email.split("@");
        String name = parts[0];
        String domain = parts[1];
        
        if (name.length() <= 2) {
            return name.charAt(0) + "***@" + domain;
        } else {
            return name.charAt(0) + "***" + name.charAt(name.length()-1) + "@" + domain;
        }
    }
    
    private void startResendTimer() {
        countdown = 60;
        sendCodeButton.setEnabled(false);
        sendCodeButton.setText("Gửi lại (" + countdown + ")");
        
        if (resendTimer != null && resendTimer.isRunning()) {
            resendTimer.stop();
        }
        
        resendTimer = new Timer(1000, e -> {
            countdown--;
            sendCodeButton.setText("Gửi lại (" + countdown + ")");
            
            if (countdown <= 0) {
                resendTimer.stop();
                sendCodeButton.setText("Gửi lại");
                sendCodeButton.setEnabled(true);
            }
        });
        
        resendTimer.start();
    }

    // Xử lý đặt lại mật khẩu
    private void handlePasswordReset() {
        String password = new String(passwordField.getPassword()).trim();
        String confirmPassword = new String(confirmPasswordField.getPassword()).trim();

        if (!password.equals(confirmPassword)) {
            showError("Mật khẩu xác nhận không khớp!", false);
            return;
        }

        try {
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