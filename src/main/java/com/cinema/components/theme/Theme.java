package com.cinema.components.theme;

import java.awt.Color;
import java.awt.Font;

/**
 * Interface định nghĩa các thuộc tính của một theme
 */
public interface Theme {
    // Tên theme
    String getName();
    
    // Màu sắc chính
    Color getPrimaryColor();
    Color getSecondaryColor();
    Color getAccentColor();
    
    // Màu nền
    Color getBackgroundColor();
    Color getCardBackgroundColor();
    Color getSidebarColor();
    Color getHeaderColor();
    
    // Màu văn bản
    Color getTextColor();
    Color getLightTextColor();
    
    // Màu trạng thái
    Color getSuccessColor();
    Color getErrorColor();
    Color getWarningColor();
    
    // Màu tương tác
    Color getHoverColor();
    Color getSelectedColor();
    Color getShadowColor();
    
    // Màu bảng
    Color getTableGridColor();
    Color getTableSelectionBackground();
    
    // Font
    Font getTitleFont();
    Font getHeaderFont();
    Font getSubheaderFont();
    Font getBodyFont();
    Font getButtonFont();
    Font getSmallFont();
    
    // Kích thước và khoảng cách
    int getBorderRadius();
    int getButtonRadius();
    int getPaddingSmall();
    int getPaddingMedium();
    int getPaddingLarge();
    
    // Kích thước cố định
    int getSidebarWidth();
    int getHeaderHeight();
    int getRowHeight();
}