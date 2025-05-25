package com.cinema.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

public class RoundedPanel extends JPanel {
    private int radius;
    private Color shadowColor;
    private boolean hasShadow;

    public RoundedPanel(int radius) {
        this(radius, false, null);
    }

    public RoundedPanel(int radius, boolean hasShadow, Color shadowColor) {
        super();
        this.radius = radius;
        this.hasShadow = hasShadow;
        this.shadowColor = shadowColor;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (hasShadow) {
            g2d.setColor(shadowColor != null ? shadowColor : new Color(0, 0, 0, 20));
            g2d.fillRoundRect(3, 3, getWidth() - 6, getHeight() - 6, radius, radius);
        }

        g2d.setColor(getBackground());
        g2d.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 3, radius, radius);
        g2d.dispose();
    }
}