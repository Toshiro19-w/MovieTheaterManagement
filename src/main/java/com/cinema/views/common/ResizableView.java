package com.cinema.views.common;

import java.awt.Dimension;

/**
 * Interface cho các view có thể tùy chỉnh kích thước khi hiển thị trong MainView
 * 
 * Mặc định kích thước là 1024x768 để phù hợp với hầu hết màn hình
 */
public interface ResizableView {
    // Kích thước mặc định
    int DEFAULT_WIDTH = 850;
    int DEFAULT_HEIGHT = 700;
    
    // Kích thước tối thiểu
    int MIN_WIDTH = 800;
    int MIN_HEIGHT = 600;
    
    // Kích thước tối đa
    int MAX_WIDTH = 1920;
    int MAX_HEIGHT = 1080;
    
    /**
     * Trả về kích thước ưa thích của view
     * @return Dimension chứa kích thước ưa thích
     */
    default Dimension getPreferredViewSize() {
        return new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
    
    /**
     * Trả về kích thước tối thiểu của view
     * @return Dimension chứa kích thước tối thiểu
     */
    default Dimension getMinimumViewSize() {
        return new Dimension(MIN_WIDTH, MIN_HEIGHT);
    }
    
    /**
     * Trả về kích thước tối đa của view
     * @return Dimension chứa kích thước tối đa
     */
    default Dimension getMaximumViewSize() {
        return new Dimension(MAX_WIDTH, MAX_HEIGHT);
    }
    
    /**
     * Xác định xem view có cần scroll hay không
     * @return true nếu view cần scroll, false nếu không
     */
    default boolean needsScrolling() {
        return true;
    }
    
    /**
     * Xác định xem view có cần điều chỉnh kích thước theo màn hình không
     * @return true nếu view cần điều chỉnh kích thước theo màn hình, false nếu không
     */
    default boolean isResponsive() {
        return false;
    }
    
    /**
     * Xác định xem view có cần giữ tỷ lệ khung hình không
     * @return true nếu view cần giữ tỷ lệ khung hình, false nếu không
     */
    default boolean maintainAspectRatio() {
        return false;
    }
    
    /**
     * Xác định tỷ lệ khung hình mong muốn (width:height)
     * @return tỷ lệ khung hình, ví dụ 16/9
     */
    default double getAspectRatio() {
        return (double) DEFAULT_WIDTH / DEFAULT_HEIGHT;
    }
    
    /**
     * Xác định cách view điều chỉnh kích thước
     * @return SizeMode của view
     */
    default SizeMode getSizeMode() {
        return SizeMode.FIXED;
    }
    
    /**
     * Các mode điều chỉnh kích thước
     */
    enum SizeMode {
        FIXED,          // Kích thước cố định
        RESPONSIVE,     // Tự động điều chỉnh theo container
        SCROLLABLE,     // Có scroll khi nội dung vượt quá
        STRETCH        // Kéo dãn để lấp đầy container
    }

    /**
     * Được gọi khi view được hiển thị để cập nhật giao diện
     */
    default void onViewShown() {
        // Mặc định không làm gì
    }
}