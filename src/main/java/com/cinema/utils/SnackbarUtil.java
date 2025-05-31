package com.cinema.utils;

import com.cinema.components.UIConstants;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

// Tạo một lớp tiện ích
public class SnackbarUtil {
    private static JPanel snackbarPanel;
    private static Timer fadeTimer;
    private static float opacity = 1.0f;
    private static final int RADIUS = 15; // Độ cong của góc
    private static boolean currentState;

    public static void showSnackbar(Component parentComponent, String message, boolean success) {
        if (snackbarPanel == null) {
            // Tạo panel với góc bo tròn
            currentState = success;
            snackbarPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // Tạo gradient cho background
                    Color background = currentState ? UIConstants.SUCCESS_COLOR : UIConstants.ERROR_COLOR;
                    g2.setColor(new Color(
                            background.getRed(),
                            background.getGreen(),
                            background.getBlue(),
                            (int)(opacity * 255)
                    ));

                    // Vẽ background với góc bo tròn
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), RADIUS, RADIUS);
                    g2.dispose();
                }

                @Override
                public boolean isOpaque() {
                    return false;
                }
            };

            snackbarPanel.setLayout(new BorderLayout(10, 0));
            snackbarPanel.setBounds(50, 30, 350, 45);

            // Icon thông báo
            ImageIcon icon = getIcon(success ? "/images/Icon/ticket.png" : "/images/Icon/1.svg", 20, 20);
            JLabel iconLabel = new JLabel(icon);
            iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
            snackbarPanel.add(iconLabel, BorderLayout.WEST);

            // Nội dung thông báo
            JLabel messageLabel = new JLabel(message, JLabel.LEFT);
            messageLabel.setFont(UIConstants.BODY_FONT);
            messageLabel.setForeground(Color.WHITE);
            messageLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
            snackbarPanel.add(messageLabel, BorderLayout.CENTER);

            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(parentComponent);
            frame.getLayeredPane().add(snackbarPanel, JLayeredPane.POPUP_LAYER);
        } else {
            currentState = success;
            // Cập nhật nội dung và icon
            ImageIcon icon = getIcon(success ? "/images/Icon/ticket.png" : "/images/Icon/1.svg", 20, 20);
            ((JLabel) snackbarPanel.getComponent(0)).setIcon(icon);
            ((JLabel) snackbarPanel.getComponent(1)).setText(message);
        }

        // Reset opacity và hiển thị
        opacity = 1.0f;
        snackbarPanel.setVisible(true);
        snackbarPanel.repaint();

        // Dừng timer cũ nếu đang chạy
        if (fadeTimer != null && fadeTimer.isRunning()) {
            fadeTimer.stop();
        }

        // Tạo timer mới để fade out
        fadeTimer = new Timer(2500, e -> startFadeOut());
        fadeTimer.setRepeats(false);
        fadeTimer.start();
    }

    private static void startFadeOut() {
        Timer animationTimer = new Timer(50, e -> {
            opacity -= 0.1f;
            if (opacity <= 0) {
                opacity = 0;
                snackbarPanel.setVisible(false);
                ((Timer)e.getSource()).stop();
            }
            snackbarPanel.repaint();
        });
        animationTimer.start();
    }

    private static ImageIcon getIcon(String path, int width, int height) {
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(SnackbarUtil.class.getResource(path)));
        Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }
}

