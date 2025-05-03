package com.cinema.views.login;

import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.ValidationUtils;
import com.formdev.flatlaf.FlatLightLaf;
import org.mindrot.jbcrypt.BCrypt;

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
import java.sql.Statement;

public class RegisterView extends JFrame {
    private JTextField usernameField, emailField, fullNameField, phoneField;
    private JPasswordField passwordField, confirmPasswordField;

    public RegisterView() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }
        initUI();
    }

    private void initUI() {
        setTitle("Đăng ký - Hệ thống quản lý rạp chiếu phim");
        setSize(1280, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Panel nền với gradient
        JPanel backgroundPanel = getBackgroundPanel();
        add(backgroundPanel);

        // Panel đăng ký với viền bo góc
        JPanel registerPanel = getJPanel();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        // Tiêu đề
        JLabel titleLabel = new JLabel("Đăng ký", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        registerPanel.add(titleLabel, gbc);

        // Username
        gbc.gridwidth = 1;
        gbc.gridy++;
        JLabel usernameLabel = new JLabel("Tài khoản:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        registerPanel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        registerPanel.add(usernameField, gbc);

        // Name
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel fullNameLabel = new JLabel("Tên:");
        fullNameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        registerPanel.add(fullNameLabel, gbc);

        gbc.gridx = 1;
        fullNameField = new JTextField(20);
        fullNameField.setFont(new Font("Arial", Font.PLAIN, 14));
        registerPanel.add(fullNameField, gbc);

        // Số điện thoại
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel phoneLabel = new JLabel("Số điện thoại:");
        phoneLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        registerPanel.add(phoneLabel, gbc);

        gbc.gridx = 1;
        phoneField = new JTextField(20);
        phoneField.setFont(new Font("Arial", Font.PLAIN, 14));
        registerPanel.add(phoneField, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        registerPanel.add(emailLabel, gbc);

        gbc.gridx = 1;
        emailField = new JTextField(20);
        emailField.setFont(new Font("Arial", Font.PLAIN, 14));
        registerPanel.add(emailField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel passwordLabel = new JLabel("Mật khẩu:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        registerPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        registerPanel.add(passwordField, gbc);

        // Confirm Password
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel confirmPasswordLabel = new JLabel("Xác nhận mật khẩu:");
        confirmPasswordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        registerPanel.add(confirmPasswordLabel, gbc);

        gbc.gridx = 1;
        confirmPasswordField = new JPasswordField(20);
        confirmPasswordField.setFont(new Font("Arial", Font.PLAIN, 14));
        registerPanel.add(confirmPasswordField, gbc);

        // Nút đăng ký
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        JButton registerBtn = new JButton("Đăng ký");
        registerBtn.setFont(new Font("Arial", Font.BOLD, 14));
        registerBtn.setBackground(new Color(0, 102, 204));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setFocusPainted(false);
        registerBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        registerBtn.addActionListener(_ -> handleRegister());
        registerPanel.add(registerBtn, gbc);

        // Liên kết Back to Login
        gbc.gridy++;
        JLabel loginLabel = new JLabel("Đã có tài khoản? Đăng nhập", SwingConstants.CENTER);
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
        registerPanel.add(loginLabel, gbc);

        backgroundPanel.add(registerPanel);
    }

    private static JPanel getBackgroundPanel() {
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0, 102, 204), 0, getHeight(), new Color(0, 204, 255));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        backgroundPanel.setLayout(new GridBagLayout());
        return backgroundPanel;
    }

    private static JPanel getJPanel() {
        JPanel registerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        registerPanel.setOpaque(false);
        registerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        registerPanel.setLayout(new GridBagLayout());
        registerPanel.setPreferredSize(new Dimension(700, 500));
        return registerPanel;
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String confirmPassword = new String(confirmPasswordField.getPassword()).trim();

        // Kiểm tra các trường bắt buộc
        if (username.isEmpty() || fullName.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Kiểm tra mật khẩu khớp
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Kiểm tra định dạng email
        if (!ValidationUtils.isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Email không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Kiểm tra độ dài mật khẩu
        if (password.length() < 8) {
            JOptionPane.showMessageDialog(this, "Mật khẩu phải có ít nhất 8 ký tự!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Kiểm tra định dạng số điện thoại (10-15 số)
        if (!ValidationUtils.isValidPhoneNumber(phone)) {
            JOptionPane.showMessageDialog(this, "Số điện thoại không hợp lệ! Phải có 10-15 số.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }


        DatabaseConnection db;
        Connection connection = null;
        PreparedStatement checkStmt = null;
        PreparedStatement nguoiDungStmt = null;
        PreparedStatement taiKhoanStmt = null;
        PreparedStatement khachHangStmt = null;
        ResultSet rs = null;
        ResultSet generatedKeys = null;

        try {
            db = new DatabaseConnection();
            connection = db.getConnection();
            connection.setAutoCommit(false); // Bắt đầu transaction

            // Kiểm tra username, email, và số điện thoại đã tồn tại
            String checkSQL = "SELECT COUNT(*) FROM TaiKhoan WHERE tenDangNhap = ? " +
                    "UNION SELECT COUNT(*) FROM NguoiDung WHERE email = ? " +
                    "UNION SELECT COUNT(*) FROM NguoiDung WHERE soDienThoai = ?";
            checkStmt = connection.prepareStatement(checkSQL);
            checkStmt.setString(1, username);
            checkStmt.setString(2, email);
            checkStmt.setString(3, phone);
            rs = checkStmt.executeQuery();
            while (rs.next()) {
                if (rs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this, "Tên đăng nhập, email hoặc số điện thoại đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    connection.rollback();
                    return;
                }
            }

            // Thêm vào bảng NguoiDung
            String insertNguoiDung = "INSERT INTO NguoiDung (hoTen, soDienThoai, email, loaiNguoiDung) VALUES (?, ?, ?, ?)";
            nguoiDungStmt = connection.prepareStatement(insertNguoiDung, Statement.RETURN_GENERATED_KEYS);
            nguoiDungStmt.setString(1, fullName);
            nguoiDungStmt.setString(2, phone);
            nguoiDungStmt.setString(3, email);
            nguoiDungStmt.setString(4, "KhachHang");
            nguoiDungStmt.executeUpdate();

            // Lấy mã người dùng được sinh tự động
            generatedKeys = nguoiDungStmt.getGeneratedKeys();
            int maNguoiDung = -1;
            if (generatedKeys.next()) {
                maNguoiDung = generatedKeys.getInt(1);
            } else {
                throw new SQLException("Không thể lấy mã người dùng!");
            }

            // Thêm vào bảng TaiKhoan
            String insertTaiKhoan = "INSERT INTO TaiKhoan (tenDangNhap, matKhau, loaiTaiKhoan, maNguoiDung) VALUES (?, ?, ?, ?)";
            taiKhoanStmt = connection.prepareStatement(insertTaiKhoan);
            taiKhoanStmt.setString(1, username);
            taiKhoanStmt.setString(2, hashPassword(password));
            taiKhoanStmt.setString(3, "User");
            taiKhoanStmt.setInt(4, maNguoiDung);
            taiKhoanStmt.executeUpdate();

            // Thêm vào bảng KhachHang
            String insertKhachHang = "INSERT INTO KhachHang (maNguoiDung, diemTichLuy) VALUES (?, ?)";
            khachHangStmt = connection.prepareStatement(insertKhachHang);
            khachHangStmt.setInt(1, maNguoiDung);
            khachHangStmt.setInt(2, 0); // diemTichLuy defaults to 0
            khachHangStmt.executeUpdate();

            connection.commit();
            JOptionPane.showMessageDialog(this, "Đăng ký thành công! Vui lòng đăng nhập.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            LoginView loginView = new LoginView();
            loginView.setVisible(true);
            dispose();

        } catch (SQLException ex) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            JOptionPane.showMessageDialog(this, "Lỗi kết nối CSDL: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            // Đóng các tài nguyên
            try {
                if (rs != null) rs.close();
                if (generatedKeys != null) generatedKeys.close();
                if (checkStmt != null) checkStmt.close();
                if (nguoiDungStmt != null) nguoiDungStmt.close();
                if (taiKhoanStmt != null) taiKhoanStmt.close();
                if (khachHangStmt != null) khachHangStmt.close();
                if (connection != null && !connection.isClosed()) connection.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Hàm mã hóa mật khẩu bằng bcrypt
    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12)); // 12 là work factor
    }
}