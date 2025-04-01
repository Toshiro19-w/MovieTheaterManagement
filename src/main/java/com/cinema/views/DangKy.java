package com.cinema.views;

import javax.swing.*;
import com.cinema.models.TaiKhoan;
import com.cinema.repositories.TaiKhoanRepository;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DangKy extends JFrame {
    private final TaiKhoanRepository taiKhoanRepository = new TaiKhoanRepository();
    private JTextField txtTenDangNhap;
    private JPasswordField txtMatKhau, txtXacNhanMatKhau;

    public DangKy() {
        setTitle("Đăng Ký");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(6, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        JLabel lblTitle = new JLabel("Đăng Ký", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        panel.add(lblTitle);

        txtTenDangNhap = new JTextField();
        txtMatKhau = new JPasswordField();
        txtXacNhanMatKhau = new JPasswordField();

        panel.add(new JLabel("Tên đăng nhập:"));
        panel.add(txtTenDangNhap);
        panel.add(new JLabel("Mật khẩu:"));
        panel.add(txtMatKhau);
        panel.add(new JLabel("Xác nhận mật khẩu:"));
        panel.add(txtXacNhanMatKhau);

        JButton btnDangKy = new JButton("Đăng Ký");
        btnDangKy.setBackground(new Color(0, 102, 102));
        btnDangKy.setForeground(Color.WHITE);
        btnDangKy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkRegister();
            }
        });

        JButton btnDangNhap = new JButton("Đã có tài khoản? Đăng nhập");
        btnDangNhap.setBorderPainted(false);
        btnDangNhap.setContentAreaFilled(false);
        btnDangNhap.setForeground(new Color(0, 102, 102));
        btnDangNhap.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new DangNhap().setVisible(true);
                dispose();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 50, 20, 50));
        buttonPanel.add(btnDangKy);
        buttonPanel.add(btnDangNhap);

        add(panel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void checkRegister() {
        String tenDangNhap = txtTenDangNhap.getText().trim();
        String matKhau = new String(txtMatKhau.getPassword());
        String xacNhanMatKhau = new String(txtXacNhanMatKhau.getPassword());

        if (tenDangNhap.isEmpty() || matKhau.isEmpty() || xacNhanMatKhau.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        if (matKhau.length() < 6) {
            JOptionPane.showMessageDialog(this, "Mật khẩu phải có ít nhất 6 ký tự!");
            return;
        }

        if (!matKhau.equals(xacNhanMatKhau)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp!");
            return;
        }

        TaiKhoan tk = new TaiKhoan(tenDangNhap, matKhau, "user");
        if (taiKhoanRepository.dangKyTaiKhoan(tk)) {
            JOptionPane.showMessageDialog(this, "Đăng ký thành công!");
            new PhimView().setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Tên đăng nhập đã tồn tại!");
        }
    }
}