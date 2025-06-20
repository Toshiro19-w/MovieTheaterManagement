package com.cinema.components;

import java.awt.Color;
import java.awt.Font;

/**
 * Lớp chứa các hằng số UI được sử dụng xuyên suốt ứng dụng
 * Tập trung quản lý màu sắc, font và các thông số UI khác
 */
public class UIConstants {
    // Màu sắc chính
    public static final Color BLUE_DARK = new Color(33, 113, 181);    // #2171B5
    public static final Color BLUE_MEDIUM = new Color(106, 190, 214); // #6ABED6
    public static final Color BLUE_LIGHT = new Color(189, 215, 231);  // #BDD7E7
    public static final Color BLUE_LIGHTEST = new Color(239, 243, 255); // #EFF3FF

    // Cập nhật các màu chính để sử dụng bảng màu mới
    public static final Color PRIMARY_COLOR = BLUE_DARK;
    public static final Color SECONDARY_COLOR = BLUE_MEDIUM;
    public static final Color ACCENT_COLOR = BLUE_LIGHT;
    public static final Color BACKGROUND_COLOR = BLUE_LIGHTEST;
    
    // Màu nền
    public static final Color CONTENT_BACKGROUND_COLOR = new Color(252, 252, 247); // Pearl color
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
    public static final Color INFO_COLOR = new Color(66, 165, 245);
    
    // Màu tương tác
    public static final Color HOVER_COLOR = new Color(224, 231, 255); // Light indigo
    public static final Color SELECTED_COLOR = PRIMARY_COLOR;
    public static final Color SHADOW_COLOR = new Color(0, 0, 0, 30);
    
    // Màu bảng
    public static final Color TABLE_GRID_COLOR = new Color(230, 230, 230);
    public static final Color TABLE_SELECTION_BG = new Color(232, 241, 249);

    // Màu nút
    public static final Color BUTTON_COLOR = PRIMARY_COLOR;
    public static final Color BUTTON_TEXT_COLOR = Color.WHITE;
    
    // Font
    public static final Font PRIMARY_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font SUBHEADER_FONT = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font ICON_FONT = new Font("Arial Unicode MS", Font.PLAIN, 18);
    
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