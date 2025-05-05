package com.cinema.views.login;

import com.cinema.controllers.TaiKhoanController;
import com.cinema.services.TaiKhoanService;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.ValidationUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Logger;
import org.mindrot.jbcrypt.BCrypt;

public class ForgotPasswordView extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(ForgotPasswordView.class.getName());
    private final JTextField usernameField = new JTextField(15);
    private final JTextField emailField = new JTextField(15);
    private final JTextField phoneField = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);
    private TaiKhoanController taiKhoanController;
    private Connection conn;

    public ForgotPasswordView() {
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
        setSize(450, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Background panel
        JPanel backgroundPanel = new JPanel() {
            final Image background = new ImageIcon(Objects.requireNonNull(getClass().getResource("/images/Icon/nen1.jpg"))).getImage();

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
            }
        };
        backgroundPanel.setLayout(new GridBagLayout());
        setContentPane(backgroundPanel);

        // Inner panel
        JPanel innerPanel = new JPanel(new GridBagLayout());
        innerPanel.setOpaque(true);
        innerPanel.setBackground(new Color(255, 255, 255, 200));
        innerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        innerPanel.setPreferredSize(new Dimension(350, 450));

        GridBagConstraints panelGbc = new GridBagConstraints();
        panelGbc.gridx = 0;
        panelGbc.gridy = 0;
        panelGbc.anchor = GridBagConstraints.CENTER;
        backgroundPanel.add(innerPanel, panelGbc);

        GridBagConstraints gbc = createGBC();

        // Tiêu đề
        JLabel titleLabel = new JLabel("Quên Mật Khẩu", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(0, 102, 204));
        innerPanel.add(titleLabel, updateGBC(gbc, 0, 0, 3, GridBagConstraints.CENTER, 0.0));

        // Tên tài khoản
        JLabel usernameLabel = new JLabel("Tên tài khoản:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        innerPanel.add(usernameLabel, updateGBC(gbc, 0, 1, 1, GridBagConstraints.WEST, 0.0));
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        innerPanel.add(usernameField, updateGBC(gbc, 1, 1, 2, GridBagConstraints.HORIZONTAL, 1.0));

        // Email
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        innerPanel.add(emailLabel, updateGBC(gbc, 0, 2, 1, GridBagConstraints.WEST, 0.0));
        emailField.setFont(new Font("Arial", Font.PLAIN, 14));
        innerPanel.add(emailField, updateGBC(gbc, 1, 2, 2, GridBagConstraints.HORIZONTAL, 1.0));

        // Số điện thoại
        JLabel phoneLabel = new JLabel("Số điện thoại:");
        phoneLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        innerPanel.add(phoneLabel, updateGBC(gbc, 0, 3, 1, GridBagConstraints.WEST, 0.0));
        phoneField.setFont(new Font("Arial", Font.PLAIN, 14));
        innerPanel.add(phoneField, updateGBC(gbc, 1, 3, 2, GridBagConstraints.HORIZONTAL, 1.0));

        // Mật khẩu mới
        JLabel passwordLabel = new JLabel("Mật khẩu mới:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        innerPanel.add(passwordLabel, updateGBC(gbc, 0, 4, 1, GridBagConstraints.WEST, 0.0));
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        innerPanel.add(passwordField, updateGBC(gbc, 1, 4, 2, GridBagConstraints.HORIZONTAL, 1.0));

        // Nút Gửi Yêu Cầu
        JButton submitButton = new JButton("Gửi Yêu Cầu");
        submitButton.setFont(new Font("Arial", Font.BOLD, 14));
        submitButton.setBackground(new Color(0, 102, 204));
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);
        submitButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        submitButton.addActionListener(_ -> handlePasswordReset());
        innerPanel.add(submitButton, updateGBC(gbc, 0, 5, 1, GridBagConstraints.NONE, 0.0));

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
        innerPanel.add(backButton, updateGBC(gbc, 1, 5, 1, GridBagConstraints.NONE, 0.0));

        // Icon cho frame
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/images/Icon/nen1.jpg")));
        setIconImage(icon.getImage());
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

    //Xử lý quên mật khẩu
    private void handlePasswordReset() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (!ValidationUtils.isValidString(username) || !ValidationUtils.isValidString(email) ||
                !ValidationUtils.isValidString(phone) || !ValidationUtils.isValidString(password)) {
            showError("Vui lòng nhập đầy đủ thông tin!", false);
            return;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            showError("Email không hợp lệ!", false);
            return;
        }

        if (!ValidationUtils.isValidPhoneNumber(phone)) {
            showError("Số điện thoại không hợp lệ!", false);
            return;
        }

        if (password.length() < 8) {
            showError("Mật khẩu phải có ít nhất 8 ký tự!", false);
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