package com.cinema.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.AbstractBorder;

/**
 * Lớp cung cấp các thành phần UI hiện đại cho ứng dụng
 */
public class ModernUIComponents {
    
    // Cache cho icons
    private static final java.util.Map<String, ImageIcon> iconCache = new java.util.concurrent.ConcurrentHashMap<>();
    
    /**
     * Tạo panel với hiệu ứng đổ bóng và góc bo tròn
     */
    public static JPanel createRoundedPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, UIConstants.BORDER_RADIUS, UIConstants.BORDER_RADIUS);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new ShadowBorder(), 
            BorderFactory.createEmptyBorder(UIConstants.PADDING_SMALL, UIConstants.PADDING_SMALL, 
                                           UIConstants.PADDING_SMALL, UIConstants.PADDING_SMALL)
        ));
        return panel;
    }
    
    /**
     * Tạo nút với thiết kế hiện đại
     */
    public static JButton createButton(String text, Color bgColor, Color textColor) {
        JButton button = new JButton(text);
        button.setFont(UIConstants.BODY_FONT);
        button.setBackground(bgColor);
        button.setForeground(textColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(UIConstants.PADDING_MEDIUM, UIConstants.PADDING_LARGE, 
                                                        UIConstants.PADDING_MEDIUM, UIConstants.PADDING_LARGE));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        
        // Hiệu ứng hover
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(brighten(bgColor, 0.1f));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    /**
     * Tạo nút với biểu tượng
     */
    public static JButton createIconButton(String text, String iconPath, Color bgColor, Color textColor) {
        JButton button = createButton(text, bgColor, textColor);
        
        try {
            // Lấy icon từ cache hoặc tạo mới
            ImageIcon icon = getIcon(iconPath, 20, 20);
            if (icon != null) {
                button.setIcon(icon);
                button.setIconTextGap(10);
            }
        } catch (Exception e) {
            // Ignore if icon can't be loaded
        }
        
        return button;
    }
    
    /**
     * Lấy icon từ cache hoặc tạo mới với kích thước chỉ định
     */
    public static ImageIcon getIcon(String path, int width, int height) {
        String cacheKey = path + "_" + width + "x" + height;
        
        // Kiểm tra cache
        ImageIcon cachedIcon = iconCache.get(cacheKey);
        if (cachedIcon != null) {
            return cachedIcon;
        }
        
        // Tạo icon mới
        try {
            ImageIcon originalIcon = new ImageIcon(ModernUIComponents.class.getResource(path));
            if (originalIcon.getImage() != null) {
                // Scale icon với chất lượng cao
                Image scaledImage = originalIcon.getImage().getScaledInstance(
                    width, height, Image.SCALE_SMOOTH);
                
                ImageIcon scaledIcon = new ImageIcon(scaledImage);
                iconCache.put(cacheKey, scaledIcon);
                return scaledIcon;
            }
        } catch (Exception e) {
            // Ignore errors
        }
        
        return null;
    }
    
    /**
     * Tạo trường văn bản với thiết kế hiện đại
     */
    public static JTextField createTextField(String placeholder) {
        PlaceholderTextField textField = new PlaceholderTextField(placeholder);
        textField.setFont(UIConstants.BODY_FONT);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219)),
            BorderFactory.createEmptyBorder(UIConstants.PADDING_MEDIUM, UIConstants.PADDING_MEDIUM, 
                                           UIConstants.PADDING_MEDIUM, UIConstants.PADDING_MEDIUM)
        ));
        textField.setPreferredSize(new Dimension(200, 40));
        return textField;
    }
    
    /**
     * Tạo nhãn tiêu đề
     */
    public static JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIConstants.HEADER_FONT);
        label.setForeground(UIConstants.PRIMARY_COLOR);
        return label;
    }
    
    /**
     * Tạo nhãn thông tin
     */
    public static JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIConstants.BODY_FONT);
        label.setForeground(UIConstants.TEXT_COLOR);
        return label;
    }
    
    /**
     * Làm sáng màu
     */
    private static Color brighten(Color color, float fraction) {
        int red = Math.min(255, (int)(color.getRed() * (1 + fraction)));
        int green = Math.min(255, (int)(color.getGreen() * (1 + fraction)));
        int blue = Math.min(255, (int)(color.getBlue() * (1 + fraction)));
        return new Color(red, green, blue);
    }
    
    /**
     * Border với hiệu ứng đổ bóng
     */
    public static class ShadowBorder extends AbstractBorder {
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Vẽ đổ bóng
            g2d.setColor(UIConstants.SHADOW_COLOR);
            g2d.drawRoundRect(x + 1, y + 1, width - 2, height - 2, UIConstants.BORDER_RADIUS, UIConstants.BORDER_RADIUS);
            g2d.dispose();
        }
        
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(1, 1, 2, 2);
        }
        
        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.top = 1;
            insets.right = insets.bottom = 2;
            return insets;
        }
    }
    
    /**
     * TextField với placeholder
     */
    public static class PlaceholderTextField extends JTextField {
        private String placeholder;
        private Color placeholderColor = UIConstants.LIGHT_TEXT_COLOR;
        
        public PlaceholderTextField(String placeholder) {
            this.placeholder = placeholder;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            if (getText().isEmpty() && placeholder != null) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.setColor(placeholderColor);
                g2d.setFont(getFont());
                
                Insets insets = getInsets();
                g2d.drawString(placeholder, insets.left, getHeight() / 2 + g2d.getFontMetrics().getAscent() / 2 - 2);
                g2d.dispose();
            }
        }
    }
}