package com.cinema.views;

import com.cinema.models.LoaiTaiKhoan;
import com.cinema.utils.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginView extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginView() {
        initUI();
    }

    private void initUI() {
        setTitle("Đăng nhập - Hệ thống quản lý rạp chiếu phim");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Tài khoản:"));
        usernameField = new JTextField();
        panel.add(usernameField);
        panel.add(new JLabel("Mật khẩu:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        JButton loginBtn = new JButton("Đăng nhập");
        JButton registerBtn = new JButton("Đăng ký");
        loginBtn.setFocusPainted(false);
        registerBtn.setFocusPainted(false);

        loginBtn.addActionListener(e -> handleLogin());
        registerBtn.addActionListener(e -> handleRegister());

        panel.add(loginBtn);
        panel.add(registerBtn);

        add(panel);
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT loaiTaiKhoan FROM TaiKhoan WHERE tenDangNhap = ? AND matKhau = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password); // Trong thực tế, nên mã hóa mật khẩu
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("loaiTaiKhoan");
                JOptionPane.showMessageDialog(this, "Đăng nhập thành công! Vai trò: " + role);
                openQuanLyView(LoaiTaiKhoan.valueOf(role));
                dispose(); // Đóng LoginView
            } else {
                JOptionPane.showMessageDialog(this, "Sai tài khoản hoặc mật khẩu!");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi kết nối cơ sở dữ liệu!");
        }
    }

    private void handleRegister() {
        // Logic đăng ký (có thể mở một dialog hoặc JFrame khác để nhập thông tin)
        JOptionPane.showMessageDialog(this, "Chức năng đăng ký đang phát triển!");
    }

    private void openQuanLyView(LoaiTaiKhoan role) {
        QuanLyView quanLyView = new QuanLyView();
        quanLyView.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginView().setVisible(true));
    }
}