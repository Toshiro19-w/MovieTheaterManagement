package com.cinema.views;

import com.cinema.models.LoaiTaiKhoan;
import com.cinema.utils.DatabaseConnection;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginView extends JFrame {
    private final Connection conn;
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginView() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("không thể khởi tạo flatLaf");
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
        setSize(400, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Panel nền với ảnh
        JPanel backgroundPanel = new JPanel() {
            Image background = new ImageIcon(getClass().getResource("/img/nen1.jpg")).getImage();

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
            }
        };
        backgroundPanel.setLayout(new BorderLayout());
        setContentPane(backgroundPanel);

        // Icon
        ImageIcon icon = new ImageIcon(getClass().getResource("/img/nen1.jpg"));
        setIconImage(icon.getImage());

        // Panel đăng nhập với nền trong suốt
        JPanel loginPanel = new JPanel();
        loginPanel.setOpaque(false); // Trong suốt hoàn toàn
        loginPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        loginPanel.setLayout(new GridBagLayout());
        
        // Thêm một panel con với nền trắng trong suốt nhẹ
        JPanel innerPanel = new JPanel();
        innerPanel.setOpaque(true);
        innerPanel.setBackground(new Color(255, 255, 255, 100)); // Trắng với độ trong suốt cao
        innerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        innerPanel.setLayout(new GridBagLayout());
        
        // Đặt kích thước cho panel đăng nhập
        innerPanel.setPreferredSize(new Dimension(320, 400));
        
        // Thêm innerPanel vào loginPanel
        loginPanel.add(innerPanel, new GridBagConstraints());
        
        // Thêm loginPanel vào background
        backgroundPanel.add(loginPanel, BorderLayout.CENTER);

        // Set bố cục cho innerPanel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        // Tiêu đề
        JLabel titleLabel = new JLabel("Đăng nhập", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Montserrat", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        titleLabel.setForeground(new Color(255, 215, 0));
        innerPanel.add(titleLabel, gbc);

        // Username
        gbc.gridwidth = 1;
        gbc.gridy++;
        JLabel usernameLabel = new JLabel("Tài khoản:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        innerPanel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        usernameField = new JTextField(15);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        innerPanel.add(usernameField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel passwordLabel = new JLabel("Mật khẩu:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        innerPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        innerPanel.add(passwordField, gbc);

        // Nút đăng nhập
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        JButton loginBtn = new JButton("Đăng nhập");
        loginBtn.setFont(new Font("Arial", Font.BOLD, 14));
        loginBtn.setBackground(new Color(0, 102, 204));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        loginBtn.addActionListener(_ -> handleLogin());
        innerPanel.add(loginBtn, gbc);

        // Liên kết Forgot Password
        gbc.gridy++;
        JLabel forgotLabel = new JLabel("Quên mật khẩu?", SwingConstants.CENTER);
        forgotLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        forgotLabel.setForeground(new Color(0, 102, 204));
        forgotLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgotLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleForgotPassword();
            }
        });
        innerPanel.add(forgotLabel, gbc);

        // Liên kết Register
        gbc.gridy++;
        JLabel registerLabel = new JLabel("Không có tài khoản? Đăng ký", SwingConstants.CENTER);
        registerLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        registerLabel.setForeground(new Color(0, 102, 204));
        registerLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleRegister();
            }
        });
        innerPanel.add(registerLabel, gbc);
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ tài khoản và mật khẩu!");
            return;
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT loaiTaiKhoan FROM TaiKhoan WHERE tenDangNhap = ? AND matKhau = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("loaiTaiKhoan");
                JOptionPane.showMessageDialog(this, "Đăng nhập thành công!");

                if ("admin".equalsIgnoreCase(role)) openMainView(username, LoaiTaiKhoan.admin);
                else openMainView(username, LoaiTaiKhoan.user);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Sai tài khoản hoặc mật khẩu!");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi truy vấn cơ sở dữ liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
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

    private String hashPassword(String password) {
        return password;
    }
}