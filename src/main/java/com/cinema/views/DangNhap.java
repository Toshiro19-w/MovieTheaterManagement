package com.cinema.views;

import com.cinema.repositories.KhachHangRepository;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class DangNhap extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;

    public DangNhap() {
        setTitle("Đăng Nhập");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitle = new JLabel("Đăng Nhập", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(lblTitle, gbc);

        JLabel lblUsername = new JLabel("Tài khoản:");
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(lblUsername, gbc);

        txtUsername = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(txtUsername, gbc);

        JLabel lblPassword = new JLabel("Mật khẩu:");
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(lblPassword, gbc);

        txtPassword = new JPasswordField(20);
        gbc.gridx = 1; gbc.gridy = 2;
        panel.add(txtPassword, gbc);

        JCheckBox chkRemember = new JCheckBox("Nhớ mật khẩu");
        gbc.gridx = 1; gbc.gridy = 3;
        panel.add(chkRemember, gbc);

        JButton btnLogin = new JButton("Đăng nhập");
        btnLogin.addActionListener(this::performLogin);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(btnLogin, gbc);

        JButton btnRegister = new JButton("Đăng ký");
        btnRegister.addActionListener(e -> openRegisterScreen());
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        panel.add(btnRegister, gbc);

        add(panel);
    }

    private void performLogin(ActionEvent e) {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (username.equals("admin") && password.equals("123")) {
            openMainScreen();
        } else if (KhachHangRepository.login(username, password)) {
            openMainScreen();
        } else {
            JOptionPane.showMessageDialog(this, "Sai tài khoản hoặc mật khẩu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openRegisterScreen() {
        new DangKy().setVisible(true);
        this.dispose();
    }

    private void openMainScreen() {
        //new PhimView().setVisible(true);
        this.dispose();
    }
}