package com.cinema.views;

import com.cinema.enums.LoaiTaiKhoan;
import com.cinema.utils.PermissionManager;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.function.Consumer;

public class HeaderPanel extends JPanel {
    private final String username;
    private final LoaiTaiKhoan loaiTaiKhoan;
    private final Consumer<String> menuCallback;
    private final Consumer<String> searchCallback;
    private final Consumer<Void> logoutCallback;
    private final PermissionManager permissionManager;

    public HeaderPanel(String username, LoaiTaiKhoan loaiTaiKhoan, Consumer<String> menuCallback, 
                       Consumer<String> searchCallback, Consumer<Void> logoutCallback) {
        this.username = username;
        this.loaiTaiKhoan = loaiTaiKhoan;
        this.menuCallback = menuCallback;
        this.searchCallback = searchCallback;
        this.logoutCallback = logoutCallback;
        this.permissionManager = new PermissionManager(loaiTaiKhoan);

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1280, 80));
        setBackground(new Color(0, 48, 135)); // Màu xanh đậm của Cinestar

        initComponents();
    }

    private void initComponents() {
        // Trái: Logo Cinema
        JPanel logoPanel = new JPanel(new GridBagLayout());
        logoPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 10, 0, 10);

        JLabel logoLabel = new JLabel();
        try {
            ImageIcon labelIcon = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("images/Icon/Cinema.jpg")));
            Image logoImage = labelIcon.getImage().getScaledInstance(100, 60, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(logoImage));
        } catch (Exception e) {
            logoLabel.setText("Logo");
            logoLabel.setForeground(Color.WHITE);
            logoLabel.setFont(new Font("Roboto", Font.BOLD, 16));
        }
        gbc.gridx = 0;
        logoPanel.add(logoLabel, gbc);

        JButton homeButton = createStyledButton("Cinema App", new Font("Roboto", Font.BOLD, 28), Color.WHITE);
        homeButton.setToolTipText("Quay về trang chủ");
        homeButton.addActionListener(_ -> menuCallback.accept("Phim đang chiếu"));
        gbc.gridx = 1;
        logoPanel.add(homeButton, gbc);

        add(logoPanel, BorderLayout.WEST);

        // Giữa: Menu hoặc tìm kiếm
        if (loaiTaiKhoan == LoaiTaiKhoan.USER) {
            JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 20));
            searchPanel.setOpaque(false);

            JLabel searchLabel = new JLabel("Tìm kiếm phim:");
            searchLabel.setForeground(Color.WHITE);
            searchLabel.setFont(new Font("Roboto", Font.PLAIN, 14));

            JTextField searchField = new JTextField(30);
            searchField.setPreferredSize(new Dimension(400, 30));
            searchField.setFont(new Font("Roboto", Font.PLAIN, 14));
            searchField.setForeground(Color.BLACK);
            searchField.setBackground(Color.WHITE);
            searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 85, 211), 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
            // Debounce search
            Timer searchTimer = new Timer(300, _ -> {
                if (!searchField.getText().equals("Tìm phim...")) {
                    searchCallback.accept(searchField.getText().trim());
                }
            });
            searchTimer.setRepeats(false);
            searchField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    searchTimer.restart();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    searchTimer.restart();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    searchTimer.restart();
                }
            });
            setPlaceholder(searchField);
            searchPanel.add(searchLabel);
            searchPanel.add(searchField);
            add(searchPanel, BorderLayout.CENTER);
        } else {
            JPanel menuPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
            menuPanel.setOpaque(false);

            JPopupMenu menuPopup = new JPopupMenu();
            for (String feature : permissionManager.getPermissions()) {
                JMenuItem menuItem = new JMenuItem(feature);
                menuItem.setFont(new Font("Roboto", Font.PLAIN, 14));
                menuItem.addActionListener(_ -> menuCallback.accept(feature));
                menuPopup.add(menuItem);
            }

            JButton menuButton = createStyledButton("Quản lý", new Font("Roboto", Font.PLAIN, 16), Color.WHITE);
            menuButton.addActionListener(e -> menuPopup.show(menuButton, 0, menuButton.getHeight()));
            menuPanel.add(menuButton);
            add(menuPanel, BorderLayout.CENTER);
        }

        // Phải: Thông tin người dùng
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 20));
        userPanel.setOpaque(false);

        JLabel avatarLabel = new JLabel();
        try {
            ImageIcon avatarIcon = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("images/Icon/user.png")));
            Image scaledImage = avatarIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
            avatarLabel.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            avatarLabel.setText("Avatar");
            avatarLabel.setForeground(Color.WHITE);
            avatarLabel.setFont(new Font("Roboto", Font.PLAIN, 14));
        }
        userPanel.add(avatarLabel);

        JButton userButton = createStyledButton("Xin chào, " + username, new Font("Roboto", Font.PLAIN, 14), Color.WHITE);
        userButton.setToolTipText("Xem thông tin cá nhân");
        userButton.addActionListener(_ -> menuCallback.accept("Thông tin cá nhân"));
        userPanel.add(userButton);

        JButton logoutButton = createStyledButton("Đăng xuất", new Font("Roboto", Font.PLAIN, 14), new Color(255, 215, 0));
        logoutButton.setToolTipText("Đăng xuất khỏi hệ thống");
        logoutButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int confirm = JOptionPane.showConfirmDialog(
                    HeaderPanel.this,
                    "Bạn có chắc muốn đăng xuất?",
                    "Xác nhận đăng xuất",
                    JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    logoutCallback.accept(null);
                }
            }
        });
        userPanel.add(logoutButton);

        add(userPanel, BorderLayout.EAST);
    }

    private JButton createStyledButton(String text, Font font, Color foreground) {
        JButton button = new JButton(text);
        button.setForeground(foreground);
        button.setBackground(new Color(0, 48, 135));
        button.setFont(font);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(0, 85, 211));
                button.setOpaque(true);
                button.setContentAreaFilled(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(0, 48, 135));
                button.setOpaque(false);
                button.setContentAreaFilled(false);
            }
        });
        return button;
    }

    private void setPlaceholder(JTextField field) {
        field.setText("Tìm phim...");
        field.setForeground(Color.LIGHT_GRAY);
        field.setFont(new Font("Roboto", Font.ITALIC, 14));
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals("Tìm phim...")) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                    field.setFont(new Font("Roboto", Font.PLAIN, 14));
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(Color.LIGHT_GRAY);
                    field.setText("Tìm phim...");
                    field.setFont(new Font("Roboto", Font.ITALIC, 14));
                }
            }
        });
    }
}