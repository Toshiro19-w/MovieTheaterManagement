package com.cinema.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.JTextField;

public class UnderlineTextField extends JTextField {
    private Color underlineColor = new Color(180, 180, 180);
    private Color focusColor = new Color(0, 123, 255);
    private Color errorColor = new Color(200, 0, 0);
    private Color readonlyColor = new Color(200, 200, 200);
    private boolean hasError = false;
    private String placeholder = "";
    private float lineThickness = 1.5f;

    public UnderlineTextField(int columns) {
        super(columns);
        setOpaque(false);
        setBorder(null);
        setFont(new Font("Segoe UI", Font.PLAIN, 14));
    }

    public void setError(boolean error) {
        this.hasError = error;
        repaint();
    }

    public void setPlaceholder(String text) {
        this.placeholder = text;
        repaint();
    }

    public void setUnderlineColor(Color color) {
        this.underlineColor = color;
        repaint();
    }

    public void setFocusColor(Color color) {
        this.focusColor = color;
        repaint();
    }

    public void setErrorColor(Color color) {
        this.errorColor = color;
        repaint();
    }

    public void setReadonlyColor(Color color) {
        this.readonlyColor = color;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Vẽ nền với màu khác nhau dựa trên trạng thái
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Màu nền dựa trên trạng thái
        if (!isEditable()) {
            // Màu nền cho trường không thể sửa
            g2d.setColor(new Color(240, 240, 240));
        } else {
            // Màu nền cho trường có thể sửa
            g2d.setColor(new Color(255, 255, 255));
        }
        
        g2d.fillRect(0, 0, getWidth(), getHeight() - 2);
        g2d.dispose();
        
        // Tiếp tục vẽ các thành phần khác
        super.paintComponent(g);
        
        // Vẽ đường gạch chân
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int y = getHeight() - 2;

        if (!isEditable()) {
            g2.setColor(readonlyColor);
        } else if (hasError) {
            g2.setColor(errorColor);
        } else if (isFocusOwner()) {
            g2.setColor(focusColor);
        } else {
            g2.setColor(underlineColor);
        }
        g2.setStroke(new BasicStroke(lineThickness));
        g2.drawLine(0, y, getWidth(), y);
        g2.dispose();

        // Vẽ placeholder
        if (getText().isEmpty() && !placeholder.isEmpty() && isEditable()) {
            g.setColor(new Color(150, 150, 150));
            g.setFont(getFont().deriveFont(Font.ITALIC));
            Insets insets = getInsets();
            g.drawString(placeholder, insets.left + 5, getHeight() / 2 + getFont().getSize() / 2);
        }
    }

    @Override
    public Insets getInsets() {
        Insets insets = super.getInsets();
        return new Insets(insets.top, insets.left, insets.bottom + 6, insets.right);
    }
}