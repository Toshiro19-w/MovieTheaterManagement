package com.cinema.components;

import javax.swing.*;
import java.awt.*;

/**
 * Lớp tiện ích cung cấp các phương thức hỗ trợ UI chung
 * để tránh trùng lặp code trong các view
 */
public class UIHelper {
    
    /**
     * Tạo panel với layout BorderLayout và padding
     */
    public static JPanel createPaddedPanel(int top, int left, int bottom, int right) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
        return panel;
    }
    
    /**
     * Tạo panel với layout FlowLayout
     */
    public static JPanel createFlowPanel(int alignment, int hgap, int vgap) {
        return new JPanel(new FlowLayout(alignment, hgap, vgap));
    }
    
    /**
     * Tạo panel với layout BoxLayout theo chiều dọc
     */
    public static JPanel createVerticalBoxPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }
    
    /**
     * Tạo panel với layout BoxLayout theo chiều ngang
     */
    public static JPanel createHorizontalBoxPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        return panel;
    }
    
    /**
     * Tạo panel với layout GridBagLayout
     */
    public static JPanel createGridBagPanel() {
        return new JPanel(new GridBagLayout());
    }
    
    /**
     * Tạo GridBagConstraints với các thông số cơ bản
     */
    public static GridBagConstraints createGridBagConstraints(int gridx, int gridy, int gridwidth, int gridheight) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.gridwidth = gridwidth;
        gbc.gridheight = gridheight;
        return gbc;
    }
    
    /**
     * Tạo separator với khoảng cách
     */
    public static Component createSeparatorWithPadding(int topPadding, int bottomPadding) {
        JPanel panel = createVerticalBoxPanel();
        panel.setOpaque(false);
        panel.add(Box.createVerticalStrut(topPadding));
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(226, 232, 240));
        panel.add(separator);
        panel.add(Box.createVerticalStrut(bottomPadding));
        return panel;
    }
    
    /**
     * Tạo panel với border và tiêu đề
     */
    public static JPanel createTitledBorderPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UIConstants.PRIMARY_COLOR),
            title,
            0, 0,
            UIConstants.TITLE_FONT,
            UIConstants.PRIMARY_COLOR
        ));
        return panel;
    }
    
    /**
     * Tạo panel với đường viền
     */
    public static JPanel createBorderedPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        return panel;
    }
    
    /**
     * Tạo panel với đường viền và padding
     */
    public static JPanel createBorderedPaddedPanel(int padding) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240)),
            BorderFactory.createEmptyBorder(padding, padding, padding, padding)
        ));
        return panel;
    }
    
    /**
     * Tạo panel với hiệu ứng đổ bóng nhẹ
     */
    public static JPanel createShadowPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            new ModernUIComponents.ShadowBorder(),
            BorderFactory.createEmptyBorder(UIConstants.PADDING_MEDIUM, UIConstants.PADDING_MEDIUM, 
                                           UIConstants.PADDING_MEDIUM, UIConstants.PADDING_MEDIUM)
        ));
        return panel;
    }
    
    /**
     * Tạo panel với kích thước cố định
     */
    public static JPanel createFixedSizePanel(int width, int height) {
        JPanel panel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(width, height);
            }
            
            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }
            
            @Override
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }
        };
        return panel;
    }
}