package com.cinema.utils;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Một triển khai đơn giản của DocumentListener để xử lý các thay đổi trong nội dung văn bản.
 * Gọi một hành động được cung cấp mỗi khi văn bản được chèn, xóa hoặc cập nhật.
 */
public class SimpleDocumentListener implements DocumentListener {
    private static final Logger LOGGER = Logger.getLogger(SimpleDocumentListener.class.getName());
    private final Runnable action;

    /**
     * Khởi tạo SimpleDocumentListener với một hành động để thực thi khi văn bản thay đổi.
     *
     * @param action Hành động sẽ được gọi khi có sự kiện thay đổi văn bản
     * @throws IllegalArgumentException nếu action là null
     */
    public SimpleDocumentListener(Runnable action) {
        if (action == null) {
            throw new IllegalArgumentException("Hành động không được null");
        }
        this.action = action;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        executeAction();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        executeAction();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        executeAction();
    }

    /**
     * Thực thi hành động với xử lý ngoại lệ.
     */
    private void executeAction() {
        try {
            action.run();
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Lỗi khi thực thi hành động DocumentListener", ex);
        }
    }
}