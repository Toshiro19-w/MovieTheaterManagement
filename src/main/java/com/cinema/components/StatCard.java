package com.cinema.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class StatCard extends RoundedPanel {
    private JLabel valueLabel;
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private Color accentColor;

    public StatCard(String title, String value, String subtitle, Color accentColor) {
        super(15, true, new Color(0, 0, 0, 20));
        this.accentColor = accentColor;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(180, 100));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Inter", Font.BOLD, 28));
        valueLabel.setForeground(accentColor);
        add(valueLabel, BorderLayout.NORTH);
        
        titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Inter", Font.BOLD, 16));
        add(titleLabel, BorderLayout.CENTER);
        
        subtitleLabel = new JLabel(subtitle, SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        subtitleLabel.setForeground(Color.GRAY);
        add(subtitleLabel, BorderLayout.SOUTH);
    }
    
    public void setValue(String value) {
        valueLabel.setText(value);
    }
    
    public void setTitle(String title) {
        titleLabel.setText(title);
    }
    
    public void setSubtitle(String subtitle) {
        subtitleLabel.setText(subtitle);
    }
    
    public void setAccentColor(Color color) {
        this.accentColor = color;
        valueLabel.setForeground(color);
    }
}