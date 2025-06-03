package com.cinema.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class PaginationPanel extends JPanel {
    private final JButton btnFirst, btnPrev, btnNext, btnLast;
    private final JLabel lblPageInfo;
    private int currentPage = 1;
    private int totalPages = 1;
    private Consumer<Integer> pageChangeListener;

    public int getCurrentPage() {
        return currentPage;
    }
    
    public int getTotalPages() {
        return totalPages;
    }

    public PaginationPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BACKGROUND_COLOR);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        buttonPanel.setOpaque(false);
        
        btnFirst = createPaginationButton("\u00AB", _ -> goToPage(1)); // Unicode for «
        btnPrev = createPaginationButton("\u2039", _ -> goToPage(currentPage - 1)); // Unicode for ‹
        btnNext = createPaginationButton("\u203A", _ -> goToPage(currentPage + 1)); // Unicode for ›
        btnLast = createPaginationButton("\u00BB", _ -> goToPage(totalPages)); // Unicode for »
        
        lblPageInfo = new JLabel("Trang 1 / 1", SwingConstants.CENTER);
        lblPageInfo.setForeground(UIConstants.TEXT_COLOR);
        lblPageInfo.setPreferredSize(new Dimension(100, 30));
        
        buttonPanel.add(btnFirst);
        buttonPanel.add(btnPrev);
        buttonPanel.add(lblPageInfo);
        buttonPanel.add(btnNext);
        buttonPanel.add(btnLast);
        
        add(buttonPanel, BorderLayout.CENTER);
        updateButtonState();
    }
    
    private JButton createPaginationButton(String text, ActionListener action) {
        JButton button = ModernUIApplier.createUnicodeButton(text, UIConstants.PRIMARY_COLOR, Color.WHITE);
        button.setPreferredSize(new Dimension(40, 30));
        button.addActionListener(action);
        return button;
    }
    
    public void setPageChangeListener(Consumer<Integer> listener) {
        this.pageChangeListener = listener;
    }
    
    public void updatePagination(int currentPage, int totalPages) {
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        lblPageInfo.setText("Trang " + currentPage + " / " + totalPages);
        updateButtonState();
    }
    
    private void updateButtonState() {
        btnFirst.setEnabled(currentPage > 1);
        btnPrev.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < totalPages);
        btnLast.setEnabled(currentPage < totalPages);
    }
    
    protected void goToPage(int page) {
        if (page < 1 || page > totalPages || page == currentPage) {
            return;
        }
        
        currentPage = page;
        if (pageChangeListener != null) {
            pageChangeListener.accept(page);
        }
    }
}