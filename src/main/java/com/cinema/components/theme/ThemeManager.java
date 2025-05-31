package com.cinema.components.theme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Quản lý theme cho ứng dụng
 */
public class ThemeManager {
    private static ThemeManager instance;
    private Map<String, Theme> themes;
    private Theme currentTheme;
    private List<ThemeChangeListener> listeners;
    
    private ThemeManager() {
        themes = new HashMap<>();
        listeners = new ArrayList<>();
        
        // Đăng ký các theme mặc định
        registerTheme(new LightTheme());
        registerTheme(new DarkTheme());
        
        // Mặc định sử dụng Light theme
        currentTheme = themes.get("Light");
    }
    
    /**
     * Lấy instance của ThemeManager (Singleton)
     */
    public static synchronized ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }
    
    /**
     * Đăng ký một theme mới
     */
    public void registerTheme(Theme theme) {
        themes.put(theme.getName(), theme);
    }
    
    /**
     * Lấy theme hiện tại
     */
    public Theme getCurrentTheme() {
        return currentTheme;
    }
    
    /**
     * Lấy danh sách tên các theme đã đăng ký
     */
    public List<String> getAvailableThemes() {
        return new ArrayList<>(themes.keySet());
    }
    
    /**
     * Thay đổi theme hiện tại
     */
    public void setTheme(String themeName) {
        if (themes.containsKey(themeName)) {
            Theme oldTheme = currentTheme;
            currentTheme = themes.get(themeName);
            
            // Thông báo cho các listener về sự thay đổi theme
            for (ThemeChangeListener listener : listeners) {
                listener.onThemeChanged(oldTheme, currentTheme);
            }
        } else {
            throw new IllegalArgumentException("Theme không tồn tại: " + themeName);
        }
    }
    
    /**
     * Đăng ký listener để lắng nghe sự kiện thay đổi theme
     */
    public void addThemeChangeListener(ThemeChangeListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Hủy đăng ký listener
     */
    public void removeThemeChangeListener(ThemeChangeListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Interface cho listener lắng nghe sự kiện thay đổi theme
     */
    public interface ThemeChangeListener {
        void onThemeChanged(Theme oldTheme, Theme newTheme);
    }
}