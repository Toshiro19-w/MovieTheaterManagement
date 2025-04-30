package com.cinema.views;

import com.cinema.enums.LoaiTaiKhoan;
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
import java.util.Objects;

public class LoginView extends JFrame {
    private final Connection conn;
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginView() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
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
        setTitle("Đăng nhập - Hệ thống quản lý rạp chiếu phim");
        setSize(1280, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setResizable(true);
        
        ImageIcon icon = new ImageIcon(getClass().getResource("/img/133864911312596807.jpg"));
        setIconImage(icon.getImage());

        setResizable(false);


        // Panel nền với gradient
        JPanel backgroundPanel = getBackgroundPanel();
        add(backgroundPanel);

        // Panel đăng nhập với viền bo góc
        JPanel loginPanel = getJPanel();

        // Set bố cục
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        // Tiêu đề
        JLabel titleLabel = new JLabel("Đăng nhập", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        loginPanel.add(titleLabel, gbc);

        // Username
        gbc.gridwidth = 1;
        gbc.gridy++;
        JLabel usernameLabel = new JLabel("Tài khoản:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        loginPanel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        usernameField = new JTextField(15);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        loginPanel.add(usernameField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        gbc.gridy++;
        JLabel passwordLabel = new JLabel("Mật khẩu:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        loginPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        loginPanel.add(passwordField, gbc);

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
        loginPanel.add(loginBtn, gbc);

        // Liên kết Forgot Password và Register
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
        loginPanel.add(forgotLabel, gbc);

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
        loginPanel.add(registerLabel, gbc);
        backgroundPanel.add(loginPanel);
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
        JPanel loginPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        loginPanel.setOpaque(false);
        loginPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        loginPanel.setLayout(new GridBagLayout());
        loginPanel.setPreferredSize(new Dimension(700, 500));
        return loginPanel;
    }

    private void handleLogin() {
        // Lấy thông tin từ trường nhập liệu
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        // Kiểm tra xem người dùng đã nhập đủ thông tin chưa
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ tài khoản và mật khẩu!");
            return;
        }

        // Kết nối cơ sở dữ liệu và kiểm tra thông tin đăng nhập
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT loaiTaiKhoan FROM TaiKhoan WHERE tenDangNhap = ? AND matKhau = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password); // Mã hoá mật khẩu (nên mã hóa trước khi lưu vào DB)
            rs = stmt.executeQuery();

            // Nếu đăng nhập thành công
            if (rs.next()) {
                String role = rs.getString("loaiTaiKhoan"); // Lấy vai trò từ cơ sở dữ liệu
                JOptionPane.showMessageDialog(this, "Đăng nhập thành công!");

                // Chuyển hướng dựa trên vai trò
                switch (role.toLowerCase()) {
                    case "admin":
                        openMainView(username, LoaiTaiKhoan.ADMIN);
                        break;
                    case "quanlyphim":
                        openMainView(username, LoaiTaiKhoan.QUANLYPHIM);
                        break;
                    case "thungan":
                        openMainView(username, LoaiTaiKhoan.THUNGAN);
                        break;
                    case "banve":
                        openMainView(username, LoaiTaiKhoan.BANVE);
                        break;
                    case "user":
                    default:
                        openMainView(username, LoaiTaiKhoan.USER); // Mặc định là khách hàng
                        break;
                }
                dispose(); // Đóng cửa sổ đăng nhập
            } else {
                JOptionPane.showMessageDialog(this, "Sai tài khoản hoặc mật khẩu!");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi truy vấn cơ sở dữ liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            // Đóng PreparedStatement và ResultSet trong khối finally để đảm bảo chúng luôn được đóng
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

    // Phương thức mở giao diện admin
    private void openMainView(String username, LoaiTaiKhoan loaiTaiKhoan) throws IOException, SQLException {
        MainView mainView = new MainView(username, loaiTaiKhoan);
        mainView.setVisible(true);
    }

    // Phương thức mở giao diện đăng ký
    private void handleRegister() {
        RegisterView registerView = new RegisterView();
        registerView.setVisible(true);
        dispose();
    }

    // Phương thức mở giao diện quên mật khẩu
    private void handleForgotPassword(){
        ForgotPasswordView forgotPasswordView = new ForgotPasswordView();
        forgotPasswordView.setVisible(true);
        dispose();
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