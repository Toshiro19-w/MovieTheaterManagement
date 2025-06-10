package com.cinema.models.dto;

import com.cinema.components.ModernUIComponents;
import com.cinema.components.PaginationPanel;
import com.cinema.components.UIConstants;

import javax.swing.*;
import java.awt.*;

public class CustomPaginationPanel extends PaginationPanel {
    private JButton firstPageButton;
    private JButton prevPageButton;
    private JButton nextPageButton;
    private JButton lastPageButton;
    private JLabel pageInfoLabel;
    
    public CustomPaginationPanel() {
        super();
        initCustomComponents();
    }
    
    private void initCustomComponents() {
        // Xóa tất cả các thành phần hiện có
        removeAll();
        
        // Tạo layout mới
        setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        
        // Tạo các nút phân trang với biểu tượng Unicode
        firstPageButton = ModernUIComponents.createUnicodeButton("\u00AB", UIConstants.CARD_BACKGROUND, UIConstants.PRIMARY_COLOR);
        prevPageButton = ModernUIComponents.createUnicodeButton("\u2039", UIConstants.CARD_BACKGROUND, UIConstants.PRIMARY_COLOR);
        pageInfoLabel = new JLabel("Trang 1 / 1");
        nextPageButton = ModernUIComponents.createUnicodeButton("\u203A", UIConstants.CARD_BACKGROUND, UIConstants.PRIMARY_COLOR);
        lastPageButton = ModernUIComponents.createUnicodeButton("\u00BB", UIConstants.CARD_BACKGROUND, UIConstants.PRIMARY_COLOR);
        
        // Thiết lập kích thước và kiểu dáng
        Dimension buttonSize = new Dimension(30, 30);
        
        for (JButton button : new JButton[]{firstPageButton, prevPageButton, nextPageButton, lastPageButton}) {
            button.setPreferredSize(buttonSize);
        }
        
        pageInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pageInfoLabel.setForeground(UIConstants.TEXT_COLOR);
        pageInfoLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        
        // Thêm các thành phần vào panel
        add(firstPageButton);
        add(prevPageButton);
        add(pageInfoLabel);
        add(nextPageButton);
        add(lastPageButton);
        
        // Thêm các listener
        firstPageButton.addActionListener(_ -> goToFirstPage());
        prevPageButton.addActionListener(_ -> goToPrevPage());
        nextPageButton.addActionListener(_ -> goToNextPage());
        lastPageButton.addActionListener(_ -> goToLastPage());
        
        // Cập nhật trạng thái nút
        updateButtonStates();
    }
    
    @Override
    public void updatePagination(int currentPage, int totalPages) {
        super.updatePagination(currentPage, totalPages);
        pageInfoLabel.setText(String.format("Trang %d / %d", currentPage, totalPages));
        updateButtonStates();
    }
    
    private void updateButtonStates() {
        boolean isFirstPage = getCurrentPage() <= 1;
        boolean isLastPage = getCurrentPage() >= getTotalPages();
        
        firstPageButton.setEnabled(!isFirstPage);
        prevPageButton.setEnabled(!isFirstPage);
        nextPageButton.setEnabled(!isLastPage);
        lastPageButton.setEnabled(!isLastPage);
    }
    
    private void goToFirstPage() {
        if (getCurrentPage() > 1) {
            super.goToPage(1);
        }
    }
    
    private void goToPrevPage() {
        if (getCurrentPage() > 1) {
            super.goToPage(getCurrentPage() - 1);
        }
    }
    
    private void goToNextPage() {
        if (getCurrentPage() < getTotalPages()) {
            super.goToPage(getCurrentPage() + 1);
        }
    }
    
    private void goToLastPage() {
        if (getCurrentPage() < getTotalPages()) {
            super.goToPage(getTotalPages());
        }
    }
}