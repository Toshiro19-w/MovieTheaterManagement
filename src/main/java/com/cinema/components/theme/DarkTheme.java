package com.cinema.components.theme;

import java.awt.Color;
import java.awt.Font;

/**
 * Theme tối cho ứng dụng
 */
public class DarkTheme implements Theme {
    @Override
    public String getName() {
        return "Dark";
    }

    @Override
    public Color getPrimaryColor() {
        return new Color(129, 140, 248); // Light indigo
    }

    @Override
    public Color getSecondaryColor() {
        return new Color(252, 211, 77); // Light yellow
    }

    @Override
    public Color getAccentColor() {
        return new Color(139, 92, 246); // Purple
    }

    @Override
    public Color getBackgroundColor() {
        return new Color(17, 24, 39); // Dark gray
    }

    @Override
    public Color getCardBackgroundColor() {
        return new Color(31, 41, 55); // Dark blue gray
    }

    @Override
    public Color getSidebarColor() {
        return new Color(26, 32, 44); // Dark blue gray
    }

    @Override
    public Color getHeaderColor() {
        return new Color(31, 41, 55); // Dark blue gray
    }

    @Override
    public Color getTextColor() {
        return new Color(237, 242, 247); // Very light gray
    }

    @Override
    public Color getLightTextColor() {
        return new Color(160, 174, 192); // Light gray
    }

    @Override
    public Color getSuccessColor() {
        return new Color(72, 187, 120); // Green
    }

    @Override
    public Color getErrorColor() {
        return new Color(245, 101, 101); // Red
    }

    @Override
    public Color getWarningColor() {
        return new Color(246, 173, 85); // Orange
    }

    @Override
    public Color getHoverColor() {
        return new Color(44, 55, 70); // Slightly lighter than background
    }

    @Override
    public Color getSelectedColor() {
        return new Color(76, 81, 191); // Dark indigo
    }

    @Override
    public Color getShadowColor() {
        return new Color(0, 0, 0, 50);
    }

    @Override
    public Color getTableGridColor() {
        return new Color(55, 65, 81);
    }

    @Override
    public Color getTableSelectionBackground() {
        return new Color(55, 65, 81);
    }

    @Override
    public Font getTitleFont() {
        return new Font("Segoe UI", Font.BOLD, 22);
    }

    @Override
    public Font getHeaderFont() {
        return new Font("Segoe UI", Font.BOLD, 18);
    }

    @Override
    public Font getSubheaderFont() {
        return new Font("Segoe UI", Font.BOLD, 16);
    }

    @Override
    public Font getBodyFont() {
        return new Font("Segoe UI", Font.PLAIN, 14);
    }

    @Override
    public Font getButtonFont() {
        return new Font("Segoe UI", Font.PLAIN, 14);
    }

    @Override
    public Font getSmallFont() {
        return new Font("Segoe UI", Font.PLAIN, 12);
    }

    @Override
    public int getBorderRadius() {
        return 20;
    }

    @Override
    public int getButtonRadius() {
        return 10;
    }

    @Override
    public int getPaddingSmall() {
        return 5;
    }

    @Override
    public int getPaddingMedium() {
        return 10;
    }

    @Override
    public int getPaddingLarge() {
        return 20;
    }

    @Override
    public int getSidebarWidth() {
        return 240;
    }

    @Override
    public int getHeaderHeight() {
        return 60;
    }

    @Override
    public int getRowHeight() {
        return 35;
    }
}