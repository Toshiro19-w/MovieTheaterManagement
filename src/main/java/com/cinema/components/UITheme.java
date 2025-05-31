package com.cinema.components;

import java.awt.Color;
import java.awt.Font;

import com.cinema.components.theme.Theme;
import com.cinema.components.theme.ThemeManager;

/**
 * Lớp cung cấp các hằng số UI dựa trên theme hiện tại
 * Thay thế cho UIConstants để hỗ trợ thay đổi theme
 */
public class UITheme {
    // Màu sắc chính
    public static Color PRIMARY_COLOR;
    public static Color SECONDARY_COLOR;
    public static Color ACCENT_COLOR;
    
    // Màu nền
    public static Color BACKGROUND_COLOR;
    public static Color CARD_BACKGROUND;
    public static Color SIDEBAR_COLOR;
    public static Color HEADER_COLOR;
    
    // Màu văn bản
    public static Color TEXT_COLOR;
    public static Color LIGHT_TEXT_COLOR;
    
    // Màu trạng thái
    public static Color SUCCESS_COLOR;
    public static Color ERROR_COLOR;
    public static Color WARNING_COLOR;
    
    // Màu tương tác
    public static Color HOVER_COLOR;
    public static Color SELECTED_COLOR;
    public static Color SHADOW_COLOR;
    
    // Màu bảng
    public static Color TABLE_GRID_COLOR;
    public static Color TABLE_SELECTION_BG;
    
    // Font
    public static Font TITLE_FONT;
    public static Font HEADER_FONT;
    public static Font SUBHEADER_FONT;
    public static Font BODY_FONT;
    public static Font BUTTON_FONT;
    public static Font SMALL_FONT;
    
    // Kích thước và khoảng cách
    public static int BORDER_RADIUS;
    public static int BUTTON_RADIUS;
    public static int PADDING_SMALL;
    public static int PADDING_MEDIUM;
    public static int PADDING_LARGE;
    
    // Kích thước cố định
    public static int SIDEBAR_WIDTH;
    public static int HEADER_HEIGHT;
    public static int ROW_HEIGHT;
    
    // Khởi tạo các giá trị từ theme hiện tại
    static {
        updateFromCurrentTheme();
        
        // Đăng ký listener để cập nhật khi theme thay đổi
        ThemeManager.getInstance().addThemeChangeListener((oldTheme, newTheme) -> {
            updateFromCurrentTheme();
        });
    }
    
    /**
     * Cập nhật tất cả các giá trị từ theme hiện tại
     */
    public static void updateFromCurrentTheme() {
        Theme theme = ThemeManager.getInstance().getCurrentTheme();
        
        // Màu sắc chính
        PRIMARY_COLOR = theme.getPrimaryColor();
        SECONDARY_COLOR = theme.getSecondaryColor();
        ACCENT_COLOR = theme.getAccentColor();
        
        // Màu nền
        BACKGROUND_COLOR = theme.getBackgroundColor();
        CARD_BACKGROUND = theme.getCardBackgroundColor();
        SIDEBAR_COLOR = theme.getSidebarColor();
        HEADER_COLOR = theme.getHeaderColor();
        
        // Màu văn bản
        TEXT_COLOR = theme.getTextColor();
        LIGHT_TEXT_COLOR = theme.getLightTextColor();
        
        // Màu trạng thái
        SUCCESS_COLOR = theme.getSuccessColor();
        ERROR_COLOR = theme.getErrorColor();
        WARNING_COLOR = theme.getWarningColor();
        
        // Màu tương tác
        HOVER_COLOR = theme.getHoverColor();
        SELECTED_COLOR = theme.getSelectedColor();
        SHADOW_COLOR = theme.getShadowColor();
        
        // Màu bảng
        TABLE_GRID_COLOR = theme.getTableGridColor();
        TABLE_SELECTION_BG = theme.getTableSelectionBackground();
        
        // Font
        TITLE_FONT = theme.getTitleFont();
        HEADER_FONT = theme.getHeaderFont();
        SUBHEADER_FONT = theme.getSubheaderFont();
        BODY_FONT = theme.getBodyFont();
        BUTTON_FONT = theme.getButtonFont();
        SMALL_FONT = theme.getSmallFont();
        
        // Kích thước và khoảng cách
        BORDER_RADIUS = theme.getBorderRadius();
        BUTTON_RADIUS = theme.getButtonRadius();
        PADDING_SMALL = theme.getPaddingSmall();
        PADDING_MEDIUM = theme.getPaddingMedium();
        PADDING_LARGE = theme.getPaddingLarge();
        
        // Kích thước cố định
        SIDEBAR_WIDTH = theme.getSidebarWidth();
        HEADER_HEIGHT = theme.getHeaderHeight();
        ROW_HEIGHT = theme.getRowHeight();
    }
}