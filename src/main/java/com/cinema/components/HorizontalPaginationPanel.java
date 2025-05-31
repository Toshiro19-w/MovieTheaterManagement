package com.cinema.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class HorizontalPaginationPanel extends JPanel {
    private JButton btnPrev;
    private JButton btnNext;
    private int currentPage = 1;
    private int totalPages = 1;
    private Consumer<Integer> pageChangeListener;
    private static final Color PRIMARY_COLOR = new Color(0, 48, 135);
    private static final Color HOVER_COLOR = new Color(0, 72, 202);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color ACTIVE_COLOR = new Color(0, 123, 255);
    
    private final List<JButton> pageButtons = new ArrayList<>();
    private final JPanel pageButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));

    public HorizontalPaginationPanel() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
        setBackground(Color.WHITE);
        
        // Tạo nút trước với icon mũi tên trái
        try {
            // Sử dụng ClassLoader để tải icon
            ClassLoader classLoader = getClass().getClassLoader(); 
            URL prevUrl = classLoader.getResource("images/Icon/leftchevron.png");
            if (prevUrl != null) {
                ImageIcon prevIcon = new ImageIcon(prevUrl);
                btnPrev = createPaginationButton(prevIcon, _ -> goToPage(currentPage - 1));
                if (prevIcon.getIconWidth() <= 0) {
                    btnPrev.setText("<");
                }
            } else {
                btnPrev = createPaginationButton(null, _ -> goToPage(currentPage - 1));
                btnPrev.setText("<");
            }
        } catch (Exception e) {
            btnPrev = createPaginationButton(null, _ -> goToPage(currentPage - 1));
            btnPrev.setText("<");
        }
        
        // Panel chứa các nút số trang
        pageButtonsPanel.setBackground(Color.WHITE);
        
        // Tạo nút sau với icon mũi tên phải
        try {
            // Sử dụng ClassLoader để tải icon
            ClassLoader classLoader = getClass().getClassLoader();
            ImageIcon nextIcon = new ImageIcon(classLoader.getResource("images/Icon/rightchevron.png"));
            btnNext = createPaginationButton(nextIcon, _ -> goToPage(currentPage + 1));
            if (nextIcon.getIconWidth() <= 0) {
                btnNext.setText(">");
            }
        } catch (Exception e) {
            btnNext = createPaginationButton(null, _ -> goToPage(currentPage + 1));
            btnNext.setText(">");
        }

        btnPrev.setToolTipText("Trang trước");
        btnNext.setToolTipText("Trang sau");
        
        add(btnPrev);
        add(pageButtonsPanel);
        add(btnNext);
        
        updateButtonState();
    }
    
    private JButton createPaginationButton(ImageIcon icon, java.awt.event.ActionListener action) {
        JButton button = new JButton();
        if (icon != null) {
            // Đảm bảo icon hiển thị rõ ràng trên nền màu đậm
            button.setIcon(icon);
        }
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(TEXT_COLOR);
        button.setPreferredSize(new Dimension(40, 30));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setIconTextGap(0);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.addActionListener(action);
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(HOVER_COLOR);
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(PRIMARY_COLOR);
                }
            }
        });
        
        return button;
    }
    
    private JButton createPageButton(int pageNum) {
        JButton button = new JButton(String.valueOf(pageNum));
        button.setPreferredSize(new Dimension(35, 30));
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setMargin(new java.awt.Insets(0, 0, 0, 0));
        
        if (pageNum == currentPage) {
            button.setBackground(ACTIVE_COLOR);
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(Color.WHITE);
            button.setForeground(Color.DARK_GRAY);
            button.addActionListener(_ -> goToPage(pageNum));
            
            button.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    button.setBackground(new Color(240, 240, 240));
                }
                
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    button.setBackground(Color.WHITE);
                }
            });
        }
        
        return button;
    }
    
    public void setPageChangeListener(Consumer<Integer> listener) {
        this.pageChangeListener = listener;
    }
    
    public void updatePagination(int currentPage, int totalPages) {
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        updateButtonState();
        updatePageButtons();
    }
    
    private void updateButtonState() {
        btnPrev.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < totalPages);
    }
    
    private void updatePageButtons() {
        pageButtonsPanel.removeAll();
        pageButtons.clear();
        
        // Hiển thị tối đa 5 nút trang
        int startPage = Math.max(1, currentPage - 2);
        int endPage = Math.min(totalPages, startPage + 4);
        
        // Điều chỉnh lại startPage nếu endPage đã đạt giới hạn
        if (endPage == totalPages) {
            startPage = Math.max(1, endPage - 4);
        }
        
        for (int i = startPage; i <= endPage; i++) {
            JButton pageButton = createPageButton(i);
            pageButtons.add(pageButton);
            pageButtonsPanel.add(pageButton);
        }
        
        pageButtonsPanel.revalidate();
        pageButtonsPanel.repaint();
    }
    
    private void goToPage(int page) {
        if (page < 1 || page > totalPages || page == currentPage) {
            return;
        }
        
        currentPage = page;
        updateButtonState();
        updatePageButtons();
        
        if (pageChangeListener != null) {
            pageChangeListener.accept(page);
        }
    }
}