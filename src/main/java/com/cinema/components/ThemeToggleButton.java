package com.cinema.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.cinema.components.theme.ThemeManager;

/**
 * Nút chuyển đổi giữa theme sáng và tối
 */
public class ThemeToggleButton extends JComponent {
    private boolean isDarkMode = false;
    private boolean isHovered = false;
    private ThemeChangeListener themeChangeListener;
    
    public ThemeToggleButton() {
        setPreferredSize(new Dimension(50, 25));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Kiểm tra theme hiện tại
        isDarkMode = ThemeManager.getInstance().getCurrentTheme().getName().equals("Dark");
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggleTheme();
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });
    }
    
    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        ThemeManager.getInstance().setTheme(isDarkMode ? "Dark" : "Light");
        
        // Thông báo cho listener
        if (themeChangeListener != null) {
            themeChangeListener.onThemeChanged(isDarkMode);
        }
        
        // Tìm JFrame cha để cập nhật toàn bộ UI
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof JFrame) {
            JFrame frame = (JFrame) window;
            SwingUtilities.updateComponentTreeUI(frame);
            frame.repaint();
        }
        
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        int toggleSize = height - 4;
        int toggleX = isDarkMode ? width - toggleSize - 2 : 2;
        
        // Vẽ nền
        g2d.setColor(isHovered ? 
            (isDarkMode ? new Color(76, 81, 191) : new Color(99, 102, 241)) : 
            (isDarkMode ? new Color(55, 65, 81) : new Color(226, 232, 240)));
        g2d.fillRoundRect(0, 0, width, height, height, height);
        
        // Vẽ nút toggle
        g2d.setColor(isDarkMode ? new Color(129, 140, 248) : Color.WHITE);
        g2d.fillOval(toggleX, 2, toggleSize, toggleSize);
        
        // Vẽ biểu tượng mặt trời/mặt trăng
        g2d.setColor(isDarkMode ? new Color(31, 41, 55) : new Color(252, 211, 77));
        if (isDarkMode) {
            // Vẽ mặt trăng
            g2d.fillOval(toggleX + 4, 6, toggleSize - 8, toggleSize - 8);
        } else {
            // Vẽ mặt trời
            int centerX = toggleX + toggleSize / 2;
            int centerY = 2 + toggleSize / 2;
            int rayLength = 3;
            
            // Vẽ tròn ở giữa
            g2d.fillOval(toggleX + 4, 6, toggleSize - 8, toggleSize - 8);
            
            // Vẽ tia
            g2d.drawLine(centerX, centerY - rayLength - 2, centerX, centerY - 2);
            g2d.drawLine(centerX, centerY + 2, centerX, centerY + rayLength + 2);
            g2d.drawLine(centerX - rayLength - 2, centerY, centerX - 2, centerY);
            g2d.drawLine(centerX + 2, centerY, centerX + rayLength + 2, centerY);
        }
        
        g2d.dispose();
    }

    /**
     * Đặt listener để được thông báo khi theme thay đổi
     */
    public void setThemeChangeListener(ThemeChangeListener listener) {
        this.themeChangeListener = listener;
    }
    
    /**
     * Interface cho listener lắng nghe sự kiện thay đổi theme
     */
    public interface ThemeChangeListener {
        void onThemeChanged(boolean isDarkMode);
    }
}