// MetricPanelDTO.java
package com.cinema.models.dto;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import com.cinema.components.UIConstants;

public class MetricPanel extends JPanel {
    private JLabel valueLabel;
    private JLabel changeLabel;
    private final String title;

    public MetricPanel(String title, String value, String change, boolean isPositive) {
        this.title = title;
        setupPanel(value, change, isPositive);
    }

    private void setupPanel(String value, String change, boolean isPositive) {
        setLayout(new BorderLayout());
        setBackground(UIConstants.CARD_COLOR);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UIConstants.TITLE_FONT);
        titleLabel.setForeground(UIConstants.TEXT_SECONDARY);

        // Value
        valueLabel = new JLabel(value);
        valueLabel.setFont(UIConstants.VALUE_FONT);
        valueLabel.setForeground(UIConstants.TEXT_PRIMARY);

        // Change indicator
        changeLabel = new JLabel(change);
        changeLabel.setFont(UIConstants.CHANGE_FONT);
        changeLabel.setForeground(isPositive ? UIConstants.SUCCESS_COLOR : UIConstants.ERROR_COLOR);

        // Layout
        JPanel changePanel = new JPanel(new BorderLayout());
        changePanel.setOpaque(false);
        changePanel.add(changeLabel, BorderLayout.WEST);

        add(titleLabel, BorderLayout.NORTH);
        add(valueLabel, BorderLayout.CENTER);
        add(changePanel, BorderLayout.SOUTH);

        // Hover effect
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(UIConstants.HOVER_COLOR);
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(UIConstants.CARD_COLOR);
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }

    public void updateValue(String value) {
        valueLabel.setText(value);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Subtle gradient background
        GradientPaint gradient = new GradientPaint(
            0, 0, getBackground(),
            0, getHeight(), getBackground().brighter()
        );
        g2d.setPaint(gradient);
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

        g2d.dispose();
    }

    public void updateChangeValue(String change, boolean isPositive) {
        changeLabel.setText(change);
        changeLabel.setForeground(isPositive ? UIConstants.SUCCESS_COLOR : UIConstants.ERROR_COLOR);
    }
}
