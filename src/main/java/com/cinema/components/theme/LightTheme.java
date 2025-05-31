package com.cinema.components.theme;

import java.awt.Color;
import java.awt.Font;

/**
 * Theme sáng mặc định cho ứng dụng
 */
public class LightTheme implements Theme {
    @Override
    public String getName() {
        return "Light";
    }

    @Override
    public Color getPrimaryColor() {
        return new Color(79, 70, 229); // Indigo
    }

    @Override
    public Color getSecondaryColor() {
        return new Color(255, 204, 0); // Yellow
    }

    @Override
    public Color getAccentColor() {
        return new Color(99, 102, 241);
    }

    @Override
    public Color getBackgroundColor() {
        return new Color(245, 245, 245);
    }

    @Override
    public Color getCardBackgroundColor() {
        return Color.WHITE;
    }

    @Override
    public Color getSidebarColor() {
        return new Color(248, 249, 250);
    }

    @Override
    public Color getHeaderColor() {
        return Color.WHITE;
    }

    @Override
    public Color getTextColor() {
        return new Color(31, 41, 55);
    }

    @Override
    public Color getLightTextColor() {
        return new Color(107, 114, 128);
    }

    @Override
    public Color getSuccessColor() {
        return new Color(46, 204, 113);
    }

    @Override
    public Color getErrorColor() {
        return new Color(231, 76, 60);
    }

    @Override
    public Color getWarningColor() {
        return new Color(241, 196, 15);
    }

    @Override
    public Color getHoverColor() {
        return new Color(224, 231, 255); // Light indigo
    }

    @Override
    public Color getSelectedColor() {
        return getPrimaryColor();
    }

    @Override
    public Color getShadowColor() {
        return new Color(0, 0, 0, 30);
    }

    @Override
    public Color getTableGridColor() {
        return new Color(230, 230, 230);
    }

    @Override
    public Color getTableSelectionBackground() {
        return new Color(232, 241, 249);
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