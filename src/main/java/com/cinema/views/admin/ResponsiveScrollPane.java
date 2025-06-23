package com.cinema.views.admin;

import com.cinema.views.common.ResizableView;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

public class ResponsiveScrollPane extends JScrollPane {
    // Các hằng số để điều chỉnh tốc độ cuộn
    private static final int UNIT_INCREMENT = 10;      // Tốc độ cuộn cơ bản
    private static final int WHEEL_MULTIPLIER = 4;      // Hệ số nhân cho cuộn chuột
    private static final double BLOCK_MULTIPLIER = 2.5; // Hệ số nhân cho cuộn trang

    private ResizableView resizableView;
    private boolean adjustingSize = false;

    public ResponsiveScrollPane() {
        super();
        initializeScrollPane();
    }

    public ResponsiveScrollPane(Component view) {
        super(view);
        initializeScrollPane();

        if (!(view instanceof Scrollable)) {
            setViewportView(new ScrollableWrapper(view));
        }
    }

    private void initializeScrollPane() {
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        setBorder(null);

        // Tăng tốc độ cuộn khi sử dụng thanh cuộn
        getVerticalScrollBar().setUnitIncrement(UNIT_INCREMENT);
        getHorizontalScrollBar().setUnitIncrement(UNIT_INCREMENT);

        // Thêm xử lý cuộn chuột
        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                // Kiểm tra xem có đang giữ phím Shift không
                if (e.isShiftDown()) {
                    // Cuộn ngang khi giữ Shift
                    int delta = e.getWheelRotation() * UNIT_INCREMENT * WHEEL_MULTIPLIER;
                    getHorizontalScrollBar().setValue(
                            getHorizontalScrollBar().getValue() + delta
                    );
                } else {
                    // Cuộn dọc bình thường với tốc độ được tăng lên
                    int delta = e.getWheelRotation() * UNIT_INCREMENT * WHEEL_MULTIPLIER;
                    getVerticalScrollBar().setValue(
                            getVerticalScrollBar().getValue() + delta
                    );
                }
                e.consume(); // Ngăn chặn xử lý mặc định
            }
        });
    }

    /**
     * Wrapper cho các component không phải là Scrollable
     */
    private static class ScrollableWrapper extends JViewport implements Scrollable {
        private final Component component;

        public ScrollableWrapper(Component component) {
            this.component = component;
            setView(component);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return component.getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(java.awt.Rectangle visibleRect,
                                              int orientation, int direction) {
            return UNIT_INCREMENT;
        }

        @Override
        public int getScrollableBlockIncrement(java.awt.Rectangle visibleRect,
                                               int orientation, int direction) {
            // Tăng tốc độ cuộn trang
            if (orientation == SwingConstants.VERTICAL) {
                return (int)(visibleRect.height * BLOCK_MULTIPLIER);
            } else {
                return (int)(visibleRect.width * BLOCK_MULTIPLIER);
            }
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    /**
     * Phương thức để cuộn đến một vị trí cụ thể
     */
    public void scrollToPosition(int x, int y) {
        getViewport().setViewPosition(new java.awt.Point(x, y));
    }

    /**
     * Phương thức để cuộn lên đầu
     */
    public void scrollToTop() {
        scrollToPosition(getViewport().getViewPosition().x, 0);
    }

    /**
     * Phương thức để cuộn xuống cuối
     */
    public void scrollToBottom() {
        int maxY = getVerticalScrollBar().getMaximum() - getViewport().getHeight();
        scrollToPosition(getViewport().getViewPosition().x, maxY);
    }

    /**
     * Cập nhật view và áp dụng các thuộc tính kích thước
     */
    public void updateView(Component view) {
        if (view instanceof ResizableView) {
            resizableView = (ResizableView) view;
            configureScrollBehavior();
        }
        setViewportView(view);
        updateScrollBars();
    }

    /**
     * Cấu hình scroll behavior dựa trên ResizableView
     */
    private void configureScrollBehavior() {
        if (resizableView == null) return;

        ResizableView.SizeMode sizeMode = resizableView.getSizeMode();
        switch (sizeMode) {
            case FIXED:
                setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
                setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_NEVER);
                break;
            case RESPONSIVE:
                setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
                setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_NEVER);
                break;
            case SCROLLABLE:
                setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_AS_NEEDED);
                setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_AS_NEEDED);
                break;
            case STRETCH:
                setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
                setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_NEVER);
                break;
        }
    }

    /**
     * Cập nhật trạng thái thanh cuộn
     */
    private void updateScrollBars() {
        if (resizableView == null) return;

        Component view = getViewport().getView();
        if (view == null) return;

        Dimension viewSize = view.getPreferredSize();
        Dimension viewportSize = getViewport().getSize();

        if (resizableView.getSizeMode() == ResizableView.SizeMode.SCROLLABLE) {
            // Chỉ hiện thanh cuộn khi nội dung vượt quá viewport
            setVerticalScrollBarPolicy(
                viewSize.height > viewportSize.height ? 
                VERTICAL_SCROLLBAR_AS_NEEDED : VERTICAL_SCROLLBAR_NEVER
            );
            setHorizontalScrollBarPolicy(
                viewSize.width > viewportSize.width ? 
                HORIZONTAL_SCROLLBAR_AS_NEEDED : HORIZONTAL_SCROLLBAR_NEVER
            );
        }
    }

    /**
     * Điều chỉnh kích thước component khi viewport thay đổi
     */
    @Override
    public void doLayout() {
        if (adjustingSize) return;
        adjustingSize = true;
        
        try {
            super.doLayout();
            
            if (resizableView != null) {
                Component view = getViewport().getView();
                if (view != null) {
                    Dimension viewportSize = getViewport().getSize();
                    ResizableView.SizeMode sizeMode = resizableView.getSizeMode();
                    
                    switch (sizeMode) {
                        case RESPONSIVE:
                        case STRETCH:
                            view.setSize(viewportSize);
                            break;
                        case FIXED:
                            Dimension preferred = resizableView.getPreferredViewSize();
                            view.setSize(preferred);
                            break;
                        case SCROLLABLE:
                            // Giữ preferred size của view
                            break;
                    }
                }
            }
            
            updateScrollBars();
        } finally {
            adjustingSize = false;
        }
    }
}
