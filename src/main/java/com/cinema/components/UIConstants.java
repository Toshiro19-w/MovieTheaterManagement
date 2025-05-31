package com.cinema.components;

import java.awt.Color;
import java.awt.Font;

/**
 * Lớp chứa các hằng số UI được sử dụng xuyên suốt ứng dụng
 * Tập trung quản lý màu sắc, font và các thông số UI khác
 */
public class UIConstants {
    // Màu sắc chính
    public static final Color PRIMARY_COLOR = new Color(79, 70, 229); // Indigo
    public static final Color SECONDARY_COLOR = new Color(255, 204, 0); // Yellow
    public static final Color ACCENT_COLOR = new Color(99, 102, 241);
    
    // Màu nền
    public static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    public static final Color CARD_BACKGROUND = Color.WHITE;
    public static final Color SIDEBAR_COLOR = new Color(248, 249, 250);
    public static final Color HEADER_COLOR = Color.WHITE;
    public static final Color CARD_COLOR = Color.WHITE;
    
    // Màu văn bản
    public static final Color TEXT_COLOR = new Color(31, 41, 55);
    public static final Color LIGHT_TEXT_COLOR = new Color(107, 114, 128);
    
    // Màu trạng thái
    public static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    public static final Color ERROR_COLOR = new Color(231, 76, 60);
    public static final Color WARNING_COLOR = new Color(241, 196, 15);
    
    // Màu tương tác
    public static final Color HOVER_COLOR = new Color(224, 231, 255); // Light indigo
    public static final Color SELECTED_COLOR = PRIMARY_COLOR;
    public static final Color SHADOW_COLOR = new Color(0, 0, 0, 30);
    
    // Màu bảng
    public static final Color TABLE_GRID_COLOR = new Color(230, 230, 230);
    public static final Color TABLE_SELECTION_BG = new Color(232, 241, 249);
    
    // Font
    public static final Font PRIMARY_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font SUBHEADER_FONT = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    
    // Kích thước và khoảng cách
    public static final int BORDER_RADIUS = 20;
    public static final int BUTTON_RADIUS = 10;
    public static final int PADDING_SMALL = 5;
    public static final int PADDING_MEDIUM = 10;
    public static final int PADDING_LARGE = 20;
    
    // Kích thước cố định
    public static final int SIDEBAR_WIDTH = 240;
    public static final int HEADER_HEIGHT = 60;
    public static final int ROW_HEIGHT = 35;

    // Các màu cho Snackbar
    public static final Color SNACKBAR_SUCCESS = new Color(76, 175, 80);
    public static final Color SNACKBAR_ERROR = new Color(244, 67, 54);
    public static final Color SNACKBAR_TEXT = Color.WHITE;


    // Các hằng số UI cho dashboard
    public static final Font VALUE_FONT = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font CHANGE_FONT = new Font("Segoe UI", Font.ITALIC, 12);
    public static final Font NEWS_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font TIME_FONT = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font CLOCK_FONT = new Font("Segoe UI", Font.BOLD, 14);

    public static final Color TEXT_PRIMARY = new Color(17, 24, 39); // Gray-900
    public static final Color TEXT_SECONDARY = new Color(107, 114, 128); // Gray-500
    public static final Color BORDER_COLOR = new Color(229, 231, 235); // Gray-200
}