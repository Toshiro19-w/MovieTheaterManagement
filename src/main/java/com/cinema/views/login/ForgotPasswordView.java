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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class ForgotPasswordView extends JFrame {
    private final JTextField usernameField = new JTextField(20);
    private final JTextField emailField = new JTextField(20);
    private final JTextField phoneField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private Connection conn;

    public ForgotPasswordView() {
        initController();
        initUI();
    }

    private void initController() {
        try {
            DatabaseConnection databaseConnection = new DatabaseConnection();
            conn = databaseConnection.getConnection();
            new TaiKhoanController(new TaiKhoanService(databaseConnection));
        } catch (IOException e) {
            showError("Không thể đọc cấu hình cơ sở dữ liệu: " + e.getMessage(), true);
        } catch (SQLException e) {
            showError("Không thể kết nối cơ sở dữ liệu: " + e.getMessage(), true);
        } catch (Exception e) {
            showError("Lỗi khởi tạo kết nối: " + e.getMessage(), true);
        }
    }

    private void initUI() {
        setTitle("Quên Mật Khẩu");
        setSize(500, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Panel nền với ảnh nen1.jpg
        JPanel backgroundPanel = new JPanel() {
            final Image background = new ImageIcon(Objects.requireNonNull(getClass().getResource("/img/nen1.jpg"))).getImage();

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
            }
        };
        backgroundPanel.setLayout(new GridBagLayout());
        setContentPane(backgroundPanel);

        // Panel nội dung trong suốt
        JPanel innerPanel = new JPanel(new GridBagLayout());
        innerPanel.setOpaque(true);
        innerPanel.setBackground(new Color(255, 255, 255, 150));
        innerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        innerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        innerPanel.setPreferredSize(new Dimension(320, 500));

        GridBagConstraints panelGbc = new GridBagConstraints();
        panelGbc.gridx = 0;
        panelGbc.gridy = 0;
        panelGbc.anchor = GridBagConstraints.CENTER;
        backgroundPanel.add(innerPanel, panelGbc);

        GridBagConstraints gbc = createGBC();

        // Tiêu đề
        JLabel titleLabel = new JLabel("Quên Mật Khẩu", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        innerPanel.add(titleLabel, updateGBC(gbc, 0, 0, 2, GridBagConstraints.CENTER, 0));

        // Tên tài khoản
        innerPanel.add(new JLabel("Tên tài khoản:"), updateGBC(gbc, 0, 1, 2, GridBagConstraints.WEST, 0));
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        usernameField.setEditable(true);
        innerPanel.add(usernameField, updateGBC(gbc, 0, 2, 2, GridBagConstraints.HORIZONTAL, 1.0));

        // Email
        innerPanel.add(new JLabel("Email:"), updateGBC(gbc, 0, 3, 2, GridBagConstraints.WEST, 0));
        emailField.setFont(new Font("Arial", Font.PLAIN, 14));
        emailField.setEditable(true);
        innerPanel.add(emailField, updateGBC(gbc, 0, 4, 2, GridBagConstraints.HORIZONTAL, 1.0));

        // Số điện thoại
        innerPanel.add(new JLabel("Số điện thoại:"), updateGBC(gbc, 0, 5, 2, GridBagConstraints.WEST, 0));
        phoneField.setFont(new Font("Arial", Font.PLAIN, 14));
        phoneField.setEditable(true);
        innerPanel.add(phoneField, updateGBC(gbc, 0, 6, 2, GridBagConstraints.HORIZONTAL, 1.0));

        // Mật khẩu mới
        innerPanel.add(new JLabel("Mật khẩu mới:"), updateGBC(gbc, 0, 7, 2, GridBagConstraints.WEST, 0));
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setEditable(true);
        innerPanel.add(passwordField, updateGBC(gbc, 0, 8, 2, GridBagConstraints.HORIZONTAL, 1.0));

        // Nút Gửi Yêu Cầu
        JButton submitButton = new JButton("Gửi Yêu Cầu");
        submitButton.setFont(new Font("Arial", Font.BOLD, 14));
        submitButton.setBackground(new Color(0, 102, 204));
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);
        submitButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        submitButton.addActionListener(e -> handlePasswordReset());
        innerPanel.add(submitButton, updateGBC(gbc, 0, 9, 1, GridBagConstraints.NONE, 0));

        // Nút Quay Lại
        JButton backButton = new JButton("Quay Lại");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setBackground(new Color(0, 102, 204));
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        backButton.addActionListener(e -> {
            new LoginView().setVisible(true);
            dispose();
        });
        innerPanel.add(backButton, updateGBC(gbc, 1, 9, 1, GridBagConstraints.NONE, 0));

        // Đặt biểu tượng cho frame
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/img/nen1.jpg")));
        setIconImage(icon.getImage());
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
        return gbc;
    }

    private void handlePasswordReset() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        // Kiểm tra dữ liệu nhập
        if (!ValidationUtils.isValidString(username) || !ValidationUtils.isValidString(email) ||
            !ValidationUtils.isValidString(phone) || !ValidationUtils.isValidString(password)) {
            showError("Vui lòng nhập đầy đủ thông tin!", false);
            return;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            showError("Email không hợp lệ!", false);
            return;
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            // Kiểm tra thông tin khớp với cơ sở dữ liệu
            String sql = "SELECT t.tenDangNhap FROM TaiKhoan t " +
                        "JOIN NguoiDung n ON t.maNguoiDung = n.maNguoiDung " +
                        "WHERE t.tenDangNhap = ? AND n.email = ? AND n.soDienThoai = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, phone);
            rs = stmt.executeQuery();

            if (!rs.next()) {
                showError("Thông tin tài khoản, email hoặc số điện thoại không đúng!", false);
                return;
            }

            // Cập nhật mật khẩu mới
            String hashedPassword = hashPassword(password);
            String updateSql = "UPDATE TaiKhoan SET matKhau = ? WHERE tenDangNhap = ?";
            stmt = conn.prepareStatement(updateSql);
            stmt.setString(1, hashedPassword);
            stmt.setString(2, username);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Mật khẩu đã được cập nhật! Vui lòng đăng nhập.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                new LoginView().setVisible(true);
                dispose();
            } else {
                showError("Không thể cập nhật mật khẩu!", false);
            }

        } catch (SQLException ex) {
            showError("Lỗi cơ sở dữ liệu: " + ex.getMessage(), false);
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

    private void showError(String msg, boolean exitAfter) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
        if (exitAfter) System.exit(1);
    }

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