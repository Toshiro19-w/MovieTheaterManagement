package com.cinema.views;

import com.cinema.utils.ValidationUtils;
import com.cinema.views.LoginView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

// Class cho màn hình quên mật khẩu
public class ForgotPasswordView extends JFrame {
    private final JTextField emailField;

    public ForgotPasswordView() {
        // Thiết lập cơ bản cho JFrame
        setTitle("Quên Mật Khẩu");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Tạo panel chính
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Label hướng dẫn
        JLabel instructionLabel = new JLabel("Nhập email để khôi phục mật khẩu:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(instructionLabel, gbc);

        // Text field cho email
        emailField = new JTextField(20);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        panel.add(emailField, gbc);

        // Nút gửi
        JButton submitButton = new JButton("Gửi Yêu Cầu");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        panel.add(submitButton, gbc);

        // Nút quay lại
        JButton backButton = new JButton("Quay Lại");
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(backButton, gbc);

        // Thêm panel vào frame
        add(panel);

        // Xử lý sự kiện cho nút Gửi
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handlePasswordResetRequest();
            }
        });

        // Xử lý sự kiện cho nút Quay lại
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Quay lại màn hình đăng nhập
                new LoginView().setVisible(true); // Giả sử bạn có LoginView
                dispose();
            }
        });
    }

    private void handlePasswordResetRequest() {
        String email = emailField.getText().trim();

        if (ValidationUtils.isValidString(email)) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập email!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            JOptionPane.showMessageDialog(this,
                    "Email không hợp lệ!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Logic xử lý gửi yêu cầu reset mật khẩu
        // Ở đây bạn có thể:
        // 1. Gửi email với link reset (cần tích hợp với mail server)
        // 2. Tạo mã xác nhận
        // 3. Lưu thông tin vào database
        try {
            // Ví dụ: Gọi phương thức gửi email (cần implement riêng)
            sendResetPasswordEmail(email);

            JOptionPane.showMessageDialog(this,
                    "Yêu cầu đã được gửi! Vui lòng kiểm tra email.",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);

            // Quay lại màn hình đăng nhập
            new LoginView().setVisible(true);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Có lỗi xảy ra: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Phương thức giả lập gửi email (cần implement thực tế)
    private void sendResetPasswordEmail(String email) {
        // Ở đây bạn cần tích hợp với một mail server
        // Ví dụ: dùng JavaMail API
        System.out.println("Đang gửi email reset tới: " + email);
        // Thực tế sẽ là:
        // 1. Tạo token reset
        // 2. Lưu token vào database
        // 3. Gửi email chứa link reset với token
    }
}