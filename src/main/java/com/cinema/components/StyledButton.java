package com.cinema.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;

public class StyledButton extends JButton {
    private Color backgroundColor;
    private Color hoverColor;
    private Color pressedColor;
    private int radius = 10;

    public StyledButton(String text, Color backgroundColor) {
        super(text);
        this.backgroundColor = backgroundColor;
        this.hoverColor = new Color(
            Math.max((int)(backgroundColor.getRed() * 0.8), 0),
            Math.max((int)(backgroundColor.getGreen() * 0.8), 0),
            Math.max((int)(backgroundColor.getBlue() * 0.8), 0)
        );
        this.pressedColor = new Color(
            Math.max((int)(backgroundColor.getRed() * 0.7), 0),
            Math.max((int)(backgroundColor.getGreen() * 0.7), 0),
            Math.max((int)(backgroundColor.getBlue() * 0.7), 0)
        );
        
        setForeground(Color.WHITE);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public void setRadius(int radius) {
        this.radius = radius;
        repaint();
    }

    public void setHoverColor(Color hoverColor) {
        this.hoverColor = hoverColor;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (getModel().isPressed()) {
            g2.setColor(pressedColor);
        } else if (getModel().isRollover()) {
            g2.setColor(hoverColor);
        } else {
            g2.setColor(backgroundColor);
        }
        
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        g2.dispose();
        
        super.paintComponent(g);
    }
}