package com.cinema.components;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import com.cinema.views.MainView;

/**
 * Singleton class to manage and cache icons
 */
public class IconManager {
    private static IconManager instance;
    private final Map<String, ImageIcon> iconCache = new HashMap<>();
    
    private IconManager() {
        // Private constructor for singleton
    }
    
    public static synchronized IconManager getInstance() {
        if (instance == null) {
            instance = new IconManager();
        }
        return instance;
    }
    
    public ImageIcon getIcon(String key, String path, String fallback, int size) {
        // Check if icon is already in cache
        if (iconCache.containsKey(key)) {
            return iconCache.get(key);
        }
        
        try {
            ImageIcon icon = new ImageIcon(MainView.class.getResource(path));
            if (icon.getIconWidth() <= 0) {
                // If icon not found, use fallback text icon
                ImageIcon fallbackIcon = createTextIcon(fallback, size);
                iconCache.put(key, fallbackIcon);
                return fallbackIcon;
            } else {
                // Resize icon
                Image img = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
                ImageIcon resizedIcon = new ImageIcon(img);
                iconCache.put(key, resizedIcon);
                return resizedIcon;
            }
        } catch (Exception e) {
            // If error, use fallback text icon
            ImageIcon fallbackIcon = createTextIcon(fallback, size);
            iconCache.put(key, fallbackIcon);
            return fallbackIcon;
        }
    }
    
    private ImageIcon createTextIcon(String text, int size) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, size));
        label.setSize(size, size);
        
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        label.paint(g2d);
        g2d.dispose();
        
        return new ImageIcon(image);
    }
    
    public void clearCache() {
        iconCache.clear();
    }
}