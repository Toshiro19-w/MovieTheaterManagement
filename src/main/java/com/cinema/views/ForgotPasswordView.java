package com.cinema.views;

import com.cinema.controllers.TaiKhoanController;
import com.cinema.services.TaiKhoanService;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.ValidationUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.*;
import javax.mail.internet.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Base64;
import java.util.Properties;
import java.util.UUID;

public class ForgotPasswordView extends JFrame {
    private final JTextField emailField = new JTextField(20);
    private TaiKhoanController controller;
    private static String senderEmail;
    private static String senderPassword;
    private static final String ENCRYPTION_KEY = "MySecretKey12345"; // 16 ký tự

    public ForgotPasswordView() {
        initController();
        initUI();
        loadEmailConfig();

        if (senderEmail == null || senderPassword == null) {
            showEmailConfigDialog();
        }
    }

    private void initController() {
        try {
            controller = new TaiKhoanController(new TaiKhoanService(new DatabaseConnection()));
        } catch (IOException e) {
            showError("Không thể kết nối cơ sở dữ liệu!", true);
        }
    }

    private void initUI() {
        setTitle("Quên Mật Khẩu");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = createGBC();

        panel.add(new JLabel("Nhập email để khôi phục mật khẩu:"), updateGBC(gbc, 0, 0, 2));
        panel.add(emailField, updateGBC(gbc, 0, 1, 2));

        JButton submitButton = new JButton("Gửi Yêu Cầu");
        JButton backButton = new JButton("Quay Lại");

        panel.add(submitButton, updateGBC(gbc, 0, 2, 1));
        panel.add(backButton, updateGBC(gbc, 1, 2, 1));

        submitButton.addActionListener(e -> handlePasswordReset());
        backButton.addActionListener(e -> {
            new LoginView().setVisible(true);
            dispose();
        });

        add(panel);
    }

    private GridBagConstraints createGBC() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        return gbc;
    }

    private GridBagConstraints updateGBC(GridBagConstraints gbc, int x, int y, int width) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        return gbc;
    }

    private void handlePasswordReset() {
        String email = emailField.getText().trim();

        if (!ValidationUtils.isValidString(email)) {
            showError("Vui lòng nhập email!", false);
            return;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            showError("Email không hợp lệ!", false);
            return;
        }

        try {
            if (!controller.isEmailExists(email)) {
                showError("Email không tồn tại trong hệ thống!", false);
                return;
            }

            String token = UUID.randomUUID().toString();
            controller.saveResetTokenToDB(email, token);
            sendResetEmail(email, token);

            JOptionPane.showMessageDialog(this, "Yêu cầu đã được gửi! Vui lòng kiểm tra email.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            new LoginView().setVisible(true);
            dispose();

        } catch (Exception ex) {
            showError("Có lỗi xảy ra: " + ex.getMessage(), false);
        }
    }

    private void sendResetEmail(String recipientEmail, String token) {
        if (senderEmail == null || senderPassword == null) {
            throw new IllegalStateException("Chưa cấu hình email gửi");
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Yêu cầu đặt lại mật khẩu");

            String resetLink = "http://localhost/reset?token=" + token;
            String content = String.format("Chào bạn,\n\nBạn đã yêu cầu đặt lại mật khẩu. Vui lòng nhấn vào liên kết sau:\n%s\n\nLiên kết có hiệu lực trong 24 giờ.\nNếu không phải bạn yêu cầu, hãy bỏ qua email này.\n\nTrân trọng,\nCinema Team", resetLink);
            message.setText(content);

            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi email: " + e.getMessage());
        }
    }

    private void showEmailConfigDialog() {
        JDialog dialog = new JDialog(this, "Cấu hình Email Gửi", true);
        dialog.setSize(1280, 700);
        dialog.setLayout(new GridBagLayout());
        dialog.setLocationRelativeTo(this);

        JTextField emailInput = new JTextField(senderEmail != null ? senderEmail : "", 20);
        JPasswordField passInput = new JPasswordField(senderPassword != null ? senderPassword : "", 20);
        JButton saveButton = new JButton("Lưu");

        GridBagConstraints gbc = createGBC();
        dialog.add(new JLabel("Email gửi:"), updateGBC(gbc, 0, 0, 1));
        dialog.add(emailInput, updateGBC(gbc, 1, 0, 1));

        dialog.add(new JLabel("Mật khẩu ứng dụng:"), updateGBC(gbc, 0, 1, 1));
        dialog.add(passInput, updateGBC(gbc, 1, 1, 1));

        dialog.add(saveButton, updateGBC(gbc, 1, 2, 1));

        saveButton.addActionListener(_ -> {
            String email = emailInput.getText().trim();
            String pass = new String(passInput.getPassword()).trim();

            if (!ValidationUtils.isValidEmail(email) || pass.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Email hoặc mật khẩu không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            senderEmail = email;
            senderPassword = pass;
            saveEmailConfig(email, pass);
            dialog.dispose();
        });

        dialog.setVisible(true);
    }

    private void loadEmailConfig() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("mail.properties")) {
            if (input != null) {
                Properties props = new Properties();
                props.load(input);
                senderEmail = props.getProperty("mail.sender.email");
                String encryptedPass = props.getProperty("mail.sender.password");
                if (encryptedPass != null) senderPassword = decrypt(encryptedPass);
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tải cấu hình email: " + e.getMessage());
        }
    }

    private void saveEmailConfig(String email, String password) {
        try {
            Properties props = new Properties();
            props.setProperty("mail.sender.email", email);
            props.setProperty("mail.sender.password", encrypt(password));

            try (FileOutputStream out = new FileOutputStream("src/main/resources/mail.properties")) {
                props.store(out, "Email Configuration");
            }
        } catch (Exception e) {
            showError("Không thể lưu cấu hình: " + e.getMessage(), false);
        }
    }

    private String encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(ENCRYPTION_KEY.getBytes(), "AES"));
        return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
    }

    private String decrypt(String encrypted) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(ENCRYPTION_KEY.getBytes(), "AES"));
        return new String(cipher.doFinal(Base64.getDecoder().decode(encrypted)));
    }

    private void showError(String msg, boolean exitAfter) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
        if (exitAfter) System.exit(1);
    }
}