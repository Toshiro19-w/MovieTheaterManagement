package com.cinema.components;

import javax.swing.*;
import java.awt.*;

public class CustomerInfoRow extends JPanel {
    private JLabel valueLabel;

    public CustomerInfoRow(Icon icon, String label, String value) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setOpaque(false);
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        JLabel textLabel = new JLabel(label);
        textLabel.setFont(new Font("Roboto", Font.PLAIN, 13));
        textLabel.setForeground(new Color(80, 80, 80));
        valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Roboto", Font.BOLD, 13));
        valueLabel.setForeground(new Color(30, 30, 30));
        add(iconLabel);
        add(textLabel);
        add(Box.createHorizontalStrut(5));
        add(valueLabel);
        add(Box.createHorizontalGlue());
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    public String getValue() {
        return valueLabel.getText();
    }
} 