package com.cinema.components.theme;

/**
 * Interface cho các component có thể thay đổi theme
 */
public interface ThemeableComponent {
    /**
     * Cập nhật giao diện của component khi theme thay đổi
     */
    void updateTheme(Theme newTheme);
}