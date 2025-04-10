package com.cinema.views;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.io.IOException;

import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.ValidationUtils;
import com.cinema.views.LoginView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class ForgotPasswordView extends JFrame {
    private Connection connec;
    private JTextField usernameField, phoneField, emailFiled;
    private JPasswordField passwordField;

    public ForgotPasswordView() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Unable to set Flat Laf");
        }
        initUI();
    }


    private static JPanel getJPanel() {
        JPanel Forgot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.white);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        Forgot.setOpaque(false);
        Forgot.setBorder(new EmptyBorder(20, 20, 20, 20));
        Forgot.setLayout(new GridBagLayout());
        Forgot.setPreferredSize(new Dimension(700, 500));
        return Forgot;


    }

    private void initUI() {
        setTitle("Quên mật khẩu - Hệ thống quản lý rạp chiếu phim");
        setSize(1280, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel Forgot = getJPanel();

        JLabel usernameLabel = new JLabel("Tài khoản:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridwidth = 1;
        gbc.gridy++;
        Forgot.add(usernameLabel, gbc);

        gbc.gridx = 1;
        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        Forgot.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel passwordLabel = new JLabel("Mật khẩu mới:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        Forgot.add(passwordLabel, gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        Forgot.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JButton submitButton = new JButton("Gửi yêu cầu");
        submitButton.setFont(new Font("Arial", Font.BOLD, 14));
        submitButton.addActionListener(e -> handleForgotPassword());
        Forgot.add(submitButton, gbc);

        add(Forgot);
        setVisible(true);

        // Liên kết Back to Login
        gbc.gridy++;
        JLabel loginLabel = new JLabel("Quay trở lại Đăng nhập", SwingConstants.CENTER);
        loginLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        loginLabel.setForeground(new Color(0, 102, 204));
        loginLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                LoginView loginView = new LoginView();
                loginView.setVisible(true);
                dispose();
            }
        });
        Forgot.add(loginLabel, gbc);

    }

    private void handleForgotPassword() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        
        String passwordHash = hashPassword(password);

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin");
            return;
        }

        // Khởi tạo đối tượng DatabaseConnection để lấy kết nối
        DatabaseConnection db = null;
        Connection connec = null;

        try {
            // Lấy kết nối cơ sở dữ liệu
            db = new DatabaseConnection();
            connec = db.getConnection();

            // Kiểm tra tài khoản có tồn tại không
            if (!KiemTraCoTonTaiKhong(connec, username)) {
                JOptionPane.showMessageDialog(this, "Tài khoản không tồn tại.");
                return;
            }

            // Cập nhật mật khẩu mới
            if (CapNhatMatKhau(connec, username, passwordHash)) {
                JOptionPane.showMessageDialog(this, "Mật khẩu đã được cập nhật thành công.");
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật mật khẩu thất bại.");
            }
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi kết nối cơ sở dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (db != null) {
                db.closeConnection();
            }
        }
    }


    public boolean KiemTraCoTonTaiKhong(Connection connec, String username) {
        String sql = "SELECT COUNT(*) FROM TaiKhoan WHERE tenDangNhap = ?";
        try (PreparedStatement ps = connec.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean CapNhatMatKhau(Connection connec, String username, String newPassword) {
        String sql = "UPDATE TaiKhoan SET matKhau = ? WHERE tenDangNhap = ?";
        try (PreparedStatement ps = connec.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setString(2, username);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
 // Hàm mã hóa mật khẩu (sử dụng MD5)
    private String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(Integer.toHexString(0xFF & b));
            }
            return hexString.toString(); // Trả về mật khẩu đã mã hóa MD5
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}

