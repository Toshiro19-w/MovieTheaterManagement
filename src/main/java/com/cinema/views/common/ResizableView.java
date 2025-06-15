package com.cinema.views.common;

import java.awt.Dimension;

/**
 * Interface cho các view có thể tùy chỉnh kích thước khi hiển thị trong MainView
 * 
 * Mặc định kích thước là 1024x768 để phù hợp với hầu hết màn hình
 */
public interface ResizableView {
    // Kích thước mặc định
    int DEFAULT_WIDTH = 1024;
    int DEFAULT_HEIGHT = 768;
    
    // Kích thước tối thiểu
    int MIN_WIDTH = 800;
    int MIN_HEIGHT = 600;
    
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
     * Được gọi khi view được hiển thị để cập nhật giao diện
     */
    default void onViewShown() {
        // Mặc định không làm gì
    }
}