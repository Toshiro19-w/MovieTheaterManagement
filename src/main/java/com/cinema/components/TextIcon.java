package com.cinema.components;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.Icon;
import javax.swing.JLabel;

/**
 * Lớp TextIcon để hiển thị văn bản (bao gồm Unicode) như một Icon
 */
public class TextIcon implements Icon {
    private final JLabel label;
    
    public TextIcon(JLabel label) {
        this.label = label;
    }
    
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setFont(label.getFont());
        g2d.setColor(label.getForeground());
        
        // Vẽ văn bản Unicode
        g2d.drawString(label.getText(), x, y + getIconHeight() - 4);
        g2d.dispose();
    }
    
    @Override
    public int getIconWidth() {
        return label.getFontMetrics(label.getFont()).stringWidth(label.getText()) + 4;
    }
    
    @Override
    public int getIconHeight() {
        return label.getFontMetrics(label.getFont()).getHeight();
    }
}